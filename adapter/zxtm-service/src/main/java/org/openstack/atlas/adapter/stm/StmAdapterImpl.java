package org.openstack.atlas.adapter.stm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.adapter.helpers.*;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerStmAdapter;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.service.domain.util.Constants;
import org.openstack.atlas.service.domain.util.StringUtilities;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.bandwidth.Bandwidth;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.monitor.Monitor;
import org.rackspace.stingray.client.persistence.Persistence;
import org.rackspace.stingray.client.persistence.PersistenceBasic;
import org.rackspace.stingray.client.persistence.PersistenceProperties;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.protection.Protection;
import org.rackspace.stingray.client.ssl.keypair.Keypair;
import org.rackspace.stingray.client.traffic.ip.TrafficIp;
import org.rackspace.stingray.client.util.EnumFactory;
import org.rackspace.stingray.client.virtualserver.*;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Component
public class StmAdapterImpl implements ReverseProxyLoadBalancerStmAdapter {
    public static Log LOG = LogFactory.getLog(StmAdapterImpl.class.getName());

    public StingrayRestClient loadSTMRestClient(LoadBalancerEndpointConfiguration config) throws InsufficientRequestException {
        LOG.debug("Building new STM client using endpoint: " + config.getRestEndpoint());
        return new StingrayRestClient(config.getRestEndpoint(), config.getUsername(), config.getPassword());
    }

    /*
    Load Balancer Resources
     */

    @Override
    public void createLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);
        String vsName = ZxtmNameBuilder.genVSName(loadBalancer);

        ResourceTranslator translator = new ResourceTranslator();
        translator.translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer);

        try {
            if (loadBalancer.getProtocol() == LoadBalancerProtocol.HTTP) {
                TrafficScriptHelper.addXForwardedForScriptIfNeeded(client);
                TrafficScriptHelper.addXForwardedProtoScriptIfNeeded(client);
            }

            createPersistentClasses(config);

            if (loadBalancer.getHealthMonitor() != null && !loadBalancer.hasSsl()) {
                updateHealthMonitor(config, client, vsName, translator.getcMonitor());
            }

            if ((loadBalancer.getAccessLists() != null && !loadBalancer.getAccessLists().isEmpty())
                    || loadBalancer.getConnectionLimit() != null) {
                updateProtection(config, client, vsName, translator.getcProtection());
            }

            updateVirtualIps(config, client, vsName, translator.getcTrafficIpGroups());
            updatePool(config, client, vsName, translator.getcPool());
            updateVirtualServer(config, client, vsName, translator.getcVServer());

            client.destroy();
        } catch (Exception e) {
            LOG.error(String.format("Failed to create load balancer %s, rolling back...", loadBalancer.getId()));
            deleteLoadBalancer(config, loadBalancer);
            throw new StmRollBackException(String.format("Failed to create load balancer %s", loadBalancer.getId()), e);
        }
    }

    @Override
    public void updateLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, LoadBalancer queLb)
            throws InsufficientRequestException, StmRollBackException {

        StingrayRestClient client = loadSTMRestClient(config);

        ResourceTranslator translator = new ResourceTranslator();

        String vsName = ZxtmNameBuilder.genVSName(loadBalancer);

        try {

            translator.translateLoadBalancerResource(config, vsName, loadBalancer, queLb);

            if (queLb.getHealthMonitor() != null && !loadBalancer.hasSsl()) {
                updateHealthMonitor(config, client, vsName, translator.getcMonitor());
            }

            if ((queLb.getAccessLists() != null && !queLb.getAccessLists().isEmpty())
                    || queLb.getConnectionLimit() != null) {
                updateProtection(config, client, vsName, translator.getcProtection());
            }

            if ((queLb.getLoadBalancerJoinVip6Set() != null && !queLb.getLoadBalancerJoinVip6Set().isEmpty())
                    || (queLb.getLoadBalancerJoinVipSet() != null && !queLb.getLoadBalancerJoinVipSet().isEmpty())) {
                updateVirtualIps(config, client, vsName, translator.getcTrafficIpGroups());
            }


            UserPages userPages = queLb.getUserPages();
            if (userPages != null) {
                if (userPages.getErrorpage() != null) {
                    setErrorFile(config, loadBalancer, loadBalancer.getUserPages().getErrorpage());
                }
            }

            updatePool(config, client, vsName, translator.getcPool());
            updateVirtualServer(config, client, vsName, translator.getcVServer());

            if (loadBalancer.isUsingSsl()) {
                String secureVsName = ZxtmNameBuilder.genSslVSName(loadBalancer);
                translator.translateLoadBalancerResource(config, secureVsName, loadBalancer, queLb);
                updateVirtualServer(config, client, secureVsName, translator.getcVServer());
            }

        } catch (Exception ex) {
            client.destroy();
            LOG.error("Exception updating load balancer: " + ex);
            throw new StmRollBackException("Failed to update loadbalancer", ex);
        }
        client.destroy();
    }

    @Override
    public void deleteLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);
        String vsName = ZxtmNameBuilder.genVSName(loadBalancer);

        LOG.debug(String.format("Removing loadbalancer: %s ...", vsName));
        deleteRateLimit(config, loadBalancer, vsName);
        deleteHealthMonitor(config, client, vsName);
        deleteProtection(config, client, vsName);
        deleteVirtualIps(config, loadBalancer);
        deletePool(config, client, vsName);
        deleteVirtualServer(config, client, vsName);
        if (loadBalancer.hasSsl()) {
            deleteVirtualServer(config, client, ZxtmNameBuilder.genSslVSName(loadBalancer));
        }
        client.destroy();
        LOG.debug(String.format("Successfully removed loadbalancer: %s from the STM service...", vsName));
    }

    /*
       Virtual Server Resources
    */
    private void updateVirtualServer(LoadBalancerEndpointConfiguration config,
                                     StingrayRestClient client, String vsName, VirtualServer virtualServer)
            throws StmRollBackException {


        LOG.debug(String.format("Updating virtual server '%s'...", vsName));

        VirtualServer curVs = null;
        try {
            curVs = client.getVirtualServer(vsName);
        } catch (Exception e) {
            LOG.warn(String.format("Error retrieving virtual server: %s, attempting to recreate... ", vsName));
        }

        try {
            client.updateVirtualServer(vsName, virtualServer);
        } catch (Exception ex) {
            String em = String.format("Error updating virtual server: %s Attempting to RollBack... \n Exception: %s ",
                    vsName, ex);

            LOG.error(em);
            if (curVs != null) {
                LOG.debug(String.format("Updating virtual server to previous configuration for rollback '%s'...", vsName));
                try {
                    client.updateVirtualServer(vsName, virtualServer);
                } catch (Exception ex2) {
                    String em2 = String.format("Error updating virtual server while attempting to previous configuration" +
                            ": %s RollBack aborted \n Exception: %s "
                            , vsName, ex2);
                    LOG.error(em2);
                }
            } else {
                LOG.warn(String.format("Virtual server was not rolled back as no previous configuration was available. '%s' ", vsName));
            }
            throw new StmRollBackException(em, ex);
        }
        LOG.debug(String.format("Successfully updated virtual server '%s'...", vsName));

    }

    private void updateKeypair(LoadBalancerEndpointConfiguration config, StingrayRestClient client, String vsName, Keypair keypair) throws StmRollBackException {
        LOG.debug(String.format("Updating keypair '%s'...", vsName));

        Keypair curKeypair = null;
        try {
            keypair = client.getKeypair(vsName);
        } catch (Exception e) {
            LOG.warn(String.format("Error retrieving keypair: %s, attempting to update...", vsName));
        }
        try {
            client.updateKeypair(vsName, keypair);
        } catch (Exception ex) {
            String em = String.format("Error updating keypair: %s Attempting to roll back... \n Exception: %s ",
                    vsName, ex);
            LOG.error(em);
            if (keypair != null) {
                LOG.debug(String.format("Updating keypair to previous configuration for rollback '%s'...", vsName));
                try {
                    client.updateKeypair(vsName, curKeypair);
                } catch (Exception ex2) {
                    String em2 = String.format("Error updating keypair '%s' to previous configuration, Roll back aborted \n Exception: %s ",
                            vsName, ex2);
                    LOG.error(em2);
                }
            } else {
                LOG.warn(String.format("Keypair '%s' was not rolled back since no previous configuration was available.", vsName));
            }
            throw new StmRollBackException(em, ex);
        }
        LOG.debug(String.format("Successfully updated keypair '%s'", vsName));
    }

    private void deleteKeypair(LoadBalancerEndpointConfiguration config, StingrayRestClient client, String vsName) throws StmRollBackException {
        LOG.info(String.format("Removing the keypair of SSL cert used on virtual server '%s'...", vsName));
        Keypair keypair = null;

        try {
            keypair = client.getKeypair(vsName);
            client.deleteKeypair(vsName);
        } catch (StingrayRestClientObjectNotFoundException notFoundException) {
            LOG.error(String.format("Keypair '%s' not found during deletion attempt, continue...", vsName));
        } catch (StingrayRestClientException clientException) {
            String em = String.format("Error removing keypair '%s', Attempting to roll back... \n Exception: %s ",
                    vsName, clientException);
            LOG.error(em);
            if (keypair != null) {
                LOG.debug(String.format("Updating Keypair to keep previous configuration for '%s'...", vsName));
                updateKeypair(config, client, vsName, keypair);
            } else {
                LOG.warn(String.format("Keypair was not rolled back as keypair '%s' was not retrieved.", vsName));
            }
            throw new StmRollBackException(em, clientException);
        }
    }

    private void deleteVirtualServer(LoadBalancerEndpointConfiguration config,
                                     StingrayRestClient client, String vsName)
            throws StmRollBackException {

        LOG.info(String.format("Removing  virtual server '%s'...", vsName));

        VirtualServer curVs = null;
        try {
            curVs = client.getVirtualServer(vsName);
            client.deleteVirtualServer(vsName);
        } catch (StingrayRestClientObjectNotFoundException ex) {
            LOG.error(String.format("Object not found when removing virtual server: %s, continue...", vsName));
        } catch (StingrayRestClientException ex) {
            String em = String.format("Error removing virtual server: %s Attempting to RollBack... \n Exception: %s "
                    , vsName, ex);
            LOG.error(em);
            if (curVs != null) {
                LOG.debug(String.format("Updating virtual server to set previous configuration for rollback '%s'", vsName));
                updateVirtualServer(config, client, vsName, curVs);
            } else {
                LOG.warn(String.format("Virtual server was not rolled back as no previous configuration was available. '%s' ", vsName));
            }
            throw new StmRollBackException(em, ex);
        }
        LOG.info(String.format("Successfully removed virtual server '%s'...", vsName));
    }


    /*
       Pool Resources
    */


    @Override
    public void setNodes(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, StmRollBackException {

        String poolName = ZxtmNameBuilder.genVSName(loadBalancer);
        ResourceTranslator translator = new ResourceTranslator();
        StingrayRestClient client = loadSTMRestClient(config);
        translator.translatePoolResource(poolName, loadBalancer, loadBalancer);
        LOG.info(String.format("Setting nodes to pool '%s'", poolName));
        updatePool(config, client, poolName, translator.getcPool());
        LOG.info(String.format("Successfully added nodes to pool '%s'", poolName));
        client.destroy();
    }

    @Override
    public void removeNodes(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, List<Node> doomedNodes) throws InsufficientRequestException, StmRollBackException {
        String poolName = ZxtmNameBuilder.genVSName(loadBalancer);
        StingrayRestClient client = loadSTMRestClient(config);
        ResourceTranslator translator = new ResourceTranslator();
        Set<Node> currentNodes = loadBalancer.getNodes();
        Map<Integer, Node> nodesMap = new HashMap<Integer, Node>();

        for (Node currentNode : currentNodes) {
            nodesMap.put(currentNode.getId(), currentNode);
        }

        for (Node doomedNode : doomedNodes) {
            Integer id = doomedNode.getId();
            if (nodesMap.containsKey(id)) {
                currentNodes.remove(nodesMap.get(id));
            }
        }
        loadBalancer.setNodes(currentNodes);
        translator.translatePoolResource(poolName, loadBalancer, loadBalancer);

        LOG.info(String.format("Removing nodes from pool '%s'", poolName));
        updatePool(config, client, poolName, translator.getcPool());
        LOG.info(String.format("Successfully removed nodes from pool '%s'", poolName));
        client.destroy();
    }

    @Override
    public void removeNode(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Node nodeToDelete) throws InsufficientRequestException, StmRollBackException {
        List<Node> doomedNodes = new ArrayList<Node>();
        doomedNodes.add(nodeToDelete);
        removeNodes(config, loadBalancer, doomedNodes);
    }

    private void updatePool(LoadBalancerEndpointConfiguration config,
                            StingrayRestClient client, String poolName, Pool pool)
            throws StmRollBackException {

        LOG.debug(String.format("Updating pool '%s' and setting nodes...", poolName));

        Pool curPool = null;
        try {
            curPool = client.getPool(poolName);
        } catch (Exception e) {
            LOG.warn(String.format("Could not load pool: %s, attempting to recreate...", poolName));
        }

        try {
            client.updatePool(poolName, pool);
            LOG.debug(String.format("Successfully updated pool '%s'...", poolName));

        } catch (Exception ex) {

            String em = String.format("Error updating node pool: %s Attempting to RollBack... \n Exception: %s", poolName, ex);

            LOG.error(em);
            if (curPool != null) {
                LOG.debug(String.format("Updating pool to previous configuration for rollback '%s'", poolName));
                try {
                    client.updatePool(poolName, curPool);
                } catch (Exception ex2) {
                    String em2 = String.format("Error updating node pool while attempting to previous configuration" +
                            ": %s RollBack aborted \n Exception: %s "
                            , poolName, ex2);
                    LOG.error(em2);
                }
            } else {
                LOG.warn(String.format("Node Pool was not rolled back as no previous configuration was available. '%s' ", poolName));
            }
            throw new StmRollBackException(em, ex);
        }
    }

    private void deletePool(LoadBalancerEndpointConfiguration config,
                            StingrayRestClient client, String poolName)
            throws StmRollBackException {

        LOG.debug(String.format("Attempting to remove pool '%s'...", poolName));

        Pool curPool = null;
        try {
            curPool = client.getPool(poolName);
        } catch (Exception e) {
            LOG.warn(String.format("Could not load current pool: %s, continuing...", poolName));

        }

        try {
            client.deletePool(poolName);
            LOG.info(String.format("Successfully removed pool '%s'...", poolName));
        } catch (StingrayRestClientObjectNotFoundException one) {
            LOG.warn(String.format("Pool object already removed: %s, continue...", poolName));

        } catch (Exception ex) {
            String em = String.format("Error removing node pool: %s Attempting to RollBack... \n Exception: %s ", poolName, ex);
            LOG.error(em);
            if (curPool != null) {
                LOG.debug(String.format("Updating pool to previous configuration for rollback '%s'", poolName));
                try {
                    client.updatePool(poolName, curPool);
                } catch (Exception ex2) {
                    String em2 = String.format("Error updating node pool while attempting to previous configuration" +
                            ": %s RollBack aborted \n Exception: %s "
                            , poolName, ex2);
                    LOG.error(em2);
                }
            } else {
                LOG.warn(String.format("Node Pool was not rolled back as no previous configuration was available. '%s' ", poolName));
            }
            throw new StmRollBackException(em, ex);
        }
    }


    /*
       VirtualIP Resources
    */
    @Override
    public void updateVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);
        ResourceTranslator translator = new ResourceTranslator();
        String vsName = ZxtmNameBuilder.genVSName(loadBalancer);
        translator.translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer);
        LOG.debug(String.format("Updating virtual ips for virtual server %s", vsName));
        updateVirtualIps(config, client, vsName, translator.getcTrafficIpGroups());
        LOG.debug(String.format("Updating virtual server %s for virtual ip configuration update", vsName));
        updateVirtualServer(config, client, vsName, translator.getcVServer());
        LOG.info(String.format("Successfully updated virtual ips for virtual server %s", vsName));
        client.destroy();
    }

    @Override
    public void deleteVirtualIp(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Integer vipId) throws InsufficientRequestException, StmRollBackException {
        //Think is was only used in old tests, do we still need it?
        List<Integer> vipIds = new ArrayList<Integer>();
        vipIds.add(vipId);
        deleteVirtualIps(config, loadBalancer, vipIds);
    }

    @Override
    public void deleteVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, List<Integer> vipIds) throws InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);
        ResourceTranslator translator = new ResourceTranslator();
        String vsName;
        vsName = ZxtmNameBuilder.genVSName(loadBalancer);

        translator.translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer);
        Map<String, TrafficIp> curTigMap = translator.getcTrafficIpGroups();

        Set<LoadBalancerJoinVip> jvipsToRemove = new HashSet<LoadBalancerJoinVip>();
        Set<LoadBalancerJoinVip6> jvips6ToRemove = new HashSet<LoadBalancerJoinVip6>();

        //Remove vips to remove from lb and translate...
        for (int id : vipIds) {
            for (LoadBalancerJoinVip jvip : loadBalancer.getLoadBalancerJoinVipSet()) {
                if (jvip.getVirtualIp().getId() == id) {
                    jvipsToRemove.add(jvip);
                }
            }

            for (LoadBalancerJoinVip6 jvip : loadBalancer.getLoadBalancerJoinVip6Set()) {
                if (jvip.getVirtualIp().getId() == id) {
                    jvips6ToRemove.add(jvip);
                }
            }
        }

        if (!jvipsToRemove.isEmpty()) loadBalancer.getLoadBalancerJoinVipSet().removeAll(jvipsToRemove);
        if (!jvips6ToRemove.isEmpty()) loadBalancer.getLoadBalancerJoinVip6Set().removeAll(jvips6ToRemove);
        translator.translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer);
        // After translating, we need to add the vips back into the LB object so that the Listener can properly remove them from the DB
        if (!jvipsToRemove.isEmpty()) loadBalancer.getLoadBalancerJoinVipSet().addAll(jvipsToRemove);
        if (!jvips6ToRemove.isEmpty()) loadBalancer.getLoadBalancerJoinVip6Set().addAll(jvips6ToRemove);

        String vipsToRemove = StringUtilities.DelimitString(vipIds, ",");
        Map<String, TrafficIp> removeTigMap = translator.getcTrafficIpGroups();

        Set<String> tigsToRemove = new HashSet<String>(curTigMap.keySet());
        boolean values2 = tigsToRemove.removeAll(removeTigMap.keySet());

        if (tigsToRemove.isEmpty()) {
            LOG.debug(String.format("Could not remove vip(s) %s for loadbalancer %s assuming vip is already deleted...", vipsToRemove, loadBalancer.getId()));
            return;
        }

        String tname = null;
        try {
            LOG.debug(String.format("Attempting to update traffic ip configuration and remove vips %s for virtual server %s", vipsToRemove, vsName));
            tname = null;
            for (String tigname : tigsToRemove) {
                tname = tigname;
                LOG.debug(String.format("Removing virtual ip %s...", tigname));
                client.deleteTrafficIp(tigname);
                LOG.info(String.format("Successfully removed virtual ip %s...", tigname));

            }
            LOG.debug(String.format("Updating virtual server %s for updated virtual ip configuration..", vsName));
            updateVirtualServer(config, client, vsName, translator.getcVServer());
            LOG.info(String.format("Successfully updated traffic ip configuration and removed vips %s for virtual server %s", vipsToRemove, vsName));
        } catch (StingrayRestClientObjectNotFoundException e) {
            LOG.error(String.format("Object not found when removing virtual ip: %s for virtual server %s, continue...", tname, vsName));
        } catch (Exception ex) {
            String em = String.format("Error removing virtual ips for vs: %s ... \n Exception: %s",
                    vsName, ex);
            LOG.error(em);
            if (!curTigMap.isEmpty()) {
                LOG.debug(String.format("Attempting to roll back to previous virtual ips: %s for virtual server %s", vipsToRemove, vsName));
                updateVirtualIps(config, client, vsName, curTigMap);
                LOG.info(String.format("Successfully rolled back to previous virtual ips: %s for virtual server %s", vipsToRemove, vsName));
            }
            throw new StmRollBackException(em, ex);
        }
        client.destroy();
    }

    private void updateVirtualIps(LoadBalancerEndpointConfiguration config,
                                  StingrayRestClient client, String vsName, Map<String, TrafficIp> tigmap)
            throws StmRollBackException {

        LOG.debug(String.format("Updating virtual ips for '%s'...", vsName));

        TrafficIp curTig = null;

        for (Map.Entry<String, TrafficIp> tm : tigmap.entrySet()) {
            try {
                curTig = client.getTrafficIp(tm.getKey());
                vsName = tm.getKey();
            } catch (Exception e) {
                LOG.warn(String.format("Could not load virtual ips for: %s, attempting to recreate...", vsName));
            }

            try {
                client.updateTrafficIp(tm.getKey(), tm.getValue());
            } catch (Exception ex) {
                String em = String.format("Error updating virtual ips: %s Attempting to RollBack... \n Exception: %s "
                        , vsName, ex);

                LOG.error(em);
                if (curTig != null) {
                    LOG.debug(String.format("Updating virtual ips to previous configuration for rollback '%s'", vsName));
                    try {
                        client.updateTrafficIp(vsName, curTig);
                    } catch (Exception ex2) {
                        String em2 = String.format("Error updating virtual ips while attempting to set previous configuration" +
                                ": %s RollBack aborted \n Exception: %s"
                                , vsName, ex2);
                        LOG.error(em2);
                    }
                } else {
                    LOG.warn(String.format("Virtual ips was not rolled back as no previous configuration was available. '%s' ", vsName));
                }
                throw new StmRollBackException(em, ex);
            }
            LOG.debug(String.format("Successfully updated virtual ips for '%s'...", vsName));

        }
    }

    private void deleteVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws InsufficientRequestException, StmRollBackException {
        List<Integer> vipIds = new ArrayList<Integer>();
        Set<LoadBalancerJoinVip> jvipset = loadBalancer.getLoadBalancerJoinVipSet();
        Set<LoadBalancerJoinVip6> jvip6set = loadBalancer.getLoadBalancerJoinVip6Set();

        for (LoadBalancerJoinVip jv : jvipset) {
            vipIds.add(jv.getVirtualIp().getId());
        }

        for (LoadBalancerJoinVip6 jv : jvip6set) {
            vipIds.add(jv.getVirtualIp().getId());
        }

        deleteVirtualIps(config, loadBalancer, vipIds);
    }

    /*
        Monitor Resources
     */

    @Override
    public void updateHealthMonitor(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, StmRollBackException {

        String vsName = ZxtmNameBuilder.genVSName(loadBalancer);
        ResourceTranslator translator = new ResourceTranslator();
        StingrayRestClient client = loadSTMRestClient(config);

        translator.translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer);
        updateHealthMonitor(config, client, vsName, translator.getcMonitor());
        updatePool(config, client, vsName, translator.getcPool());
        client.destroy();
    }

    @Override
    public void deleteHealthMonitor(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws InsufficientRequestException, StmRollBackException {
        final String monitorName = ZxtmNameBuilder.genVSName(loadBalancer);
        StingrayRestClient client = loadSTMRestClient(config);
        deleteHealthMonitor(config, client, monitorName);
        client.destroy();
    }

    private void updateHealthMonitor(LoadBalancerEndpointConfiguration config,
                                     StingrayRestClient client, String monitorName, Monitor monitor)
            throws StmRollBackException {

        LOG.debug(String.format("Update Monitor '%s' ...", monitorName));

        Monitor curMon = null;
        try {
            curMon = client.getMonitor(monitorName);
        } catch (Exception e) {
            LOG.warn(String.format("Could not locate: %s, attempting to recreate...", monitorName));
        }

        try {
            client.updateMonitor(monitorName, monitor);
        } catch (Exception ex) {
            String em = String.format("Error updating monitor: %s Attempting to RollBack... \n Exception: %s ", monitorName, ex);
            LOG.error(em);
            if (curMon != null) {
                LOG.debug(String.format("Updating monitor to previous configuration for rollback '%s'", monitorName));
                try {
                    client.updateMonitor(monitorName, curMon);
                } catch (Exception ex2) {
                    String em2 = String.format("Error updating monitor while attempting to set previous configuration" +
                            ": %s RollBack aborted \n Exception: %s", monitorName, ex2);
                    LOG.error(em2);
                }
            } else {
                LOG.warn(String.format("Monitor was not rolled back as no previous configuration was available. '%s' ", monitorName));
            }
            throw new StmRollBackException(em, ex);
        }
        LOG.debug(String.format("Successfully updated Monitor '%s' ...", monitorName));
    }

    private void deleteHealthMonitor(LoadBalancerEndpointConfiguration config,
                                     StingrayRestClient client, String monitorName)
            throws StmRollBackException {

        LOG.info(String.format("Removing  monitor '%s'...", monitorName));

        Monitor curMon = null;
        try {
            curMon = client.getMonitor(monitorName);
            client.deleteMonitor(monitorName);
            LOG.info(String.format("Successfully removed monitor '%s'...", monitorName));
        } catch (StingrayRestClientObjectNotFoundException ex) {
            LOG.error(String.format("Monitor already removed: %s, continue...", monitorName));
        } catch (StingrayRestClientException ex) {
            String em = String.format("Error removing monitor: %s Attempting to RollBack... \n Exception: %s "
                    , monitorName, ex);
            LOG.error(em);
            if (curMon != null) {
                LOG.debug(String.format("Updating virtual server to set previous configuration for rollback '%s'", monitorName));
                updateHealthMonitor(config, client, monitorName, curMon);
            } else {
                LOG.warn(String.format("Monitor was not rolled back as no previous configuration was available. '%s' ", monitorName));
            }
            throw new StmRollBackException(em, ex);
        }
    }

    /*
        Protection Resources
     */

    @Override
    public void updateProtection(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws StmRollBackException, InsufficientRequestException {
        ResourceTranslator translator = new ResourceTranslator();
        StingrayRestClient client = loadSTMRestClient(config);
        String protectionName = ZxtmNameBuilder.genVSName(loadBalancer);
        LOG.info(String.format("Updating protection on %s...", protectionName));
        updateProtection(config, client, protectionName, translator.translateProtectionResource(protectionName, loadBalancer));
        LOG.info(String.format("Successfully created protection %s", protectionName));
        client.destroy();
    }

    @Override
    public void deleteProtection(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws InsufficientRequestException, StmRollBackException {

        StingrayRestClient client = loadSTMRestClient(config);
        String protectionName = ZxtmNameBuilder.genVSName(loadBalancer);
        LOG.info(String.format("Deleting protection on %s...", protectionName));
        deleteProtection(config, client, protectionName);
        LOG.info(String.format("Successfully deleted protection %s!", protectionName));
        client.destroy();
    }

    public void updateProtection(LoadBalancerEndpointConfiguration config, StingrayRestClient client, String protectionName, Protection protection) throws InsufficientRequestException, StmRollBackException {
        LOG.debug(String.format("Updating protection class on '%s'...", protectionName));

        Protection curProtection = null;
        try {
            curProtection = client.getProtection(protectionName);
        } catch (Exception e) {
            LOG.warn(String.format("Could not load protection class: %s, attempting to recreate...", protectionName));
        }

        try {
            LOG.debug(String.format("Updating protection for %s...", protectionName));
            client.updateProtection(protectionName, protection);
        } catch (Exception ex) {
            String em = String.format("Error updating protection: %s Attempting to RollBack... \n Exception: %s"
                    , protectionName, ex);

            LOG.error(em);
            if (curProtection != null) {
                LOG.debug(String.format("Updating monitor to previous configuration for rollback '%s'", protectionName));
                try {
                    client.updateProtection(protectionName, curProtection);
                } catch (Exception ex2) {
                    String em2 = String.format("Error updating protection while attempting to set previous configuration" +
                            ": %s RollBack aborted \n Exception: %s"
                            , protectionName, ex2);
                    LOG.error(em2);
                }
            } else {
                LOG.warn(String.format("Protection was not rolled back as no previous configuration was available. '%s' ", protectionName));
            }
            throw new StmRollBackException(em, ex);
        }

        LOG.debug(String.format("Successfully updated protection for %s!", protectionName));
    }

    private void deleteProtection(LoadBalancerEndpointConfiguration config,
                                  StingrayRestClient client, String protectionName)
            throws StmRollBackException, InsufficientRequestException {

        LOG.info(String.format("Removing  protection class '%s'...", protectionName));

        Protection curPro = null;
        try {
            curPro = client.getProtection(protectionName);
            client.deleteProtection(protectionName);
            LOG.info(String.format("Successfully removed protection class '%s'...", protectionName));
        } catch (StingrayRestClientObjectNotFoundException ex) {
            LOG.error(String.format("Protection already removed: %s, continue...", protectionName));
        } catch (StingrayRestClientException ex) {
            String em = String.format("Error removing protection: %s. Attempting to RollBack... \n Exception: %s "
                    , protectionName, ex);
            LOG.error(em);
            if (curPro != null) {
                LOG.debug(String.format("Updating virtual server to set previous configuration for rollback '%s'", protectionName));
                updateProtection(config, client, protectionName, curPro);
            } else {
                LOG.warn(String.format("Protection was not rolled back as no previous configuration was available. '%s' ", protectionName));
            }
            throw new StmRollBackException(em, ex);
        }
    }


    @Override
    public void updateAccessList(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws InsufficientRequestException, StmRollBackException {
        if (loadBalancer.getAccessLists() != null && !loadBalancer.getAccessLists().isEmpty()) {
            String name = ZxtmNameBuilder.genVSName(loadBalancer);
            LOG.info(String.format("Updating Access List on '%s'...", name));
            updateProtection(config, loadBalancer);
            LOG.info(String.format("Successfully updated Access List on '%s'...", name));
        }
    }

    @Override
    public void deleteAccessList(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, List<Integer> accessListToDelete)
            throws InsufficientRequestException, StmRollBackException {
        Set<AccessList> saveItems = new HashSet<AccessList>();
        for (AccessList item : loadBalancer.getAccessLists()) {
            if (!accessListToDelete.contains(item.getId())) {
                saveItems.add(item);
            }
        }
        loadBalancer.setAccessLists(saveItems);
        String name = ZxtmNameBuilder.genVSName(loadBalancer);
        LOG.info(String.format("Deleting Access List on '%s'...", name));
        if (loadBalancer.getConnectionLimit() != null || !loadBalancer.getAccessLists().isEmpty()) {
            updateProtection(config, loadBalancer);
        } else {
            deleteProtection(config, loadBalancer);
        }
        LOG.info(String.format("Successfully deleted Access List on '%s'.", name));
    }

    @Override
    public void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws InsufficientRequestException, StmRollBackException {
        if (loadBalancer.getConnectionLimit() != null) {
            String name = ZxtmNameBuilder.genVSName(loadBalancer);
            LOG.info(String.format("Updating Connection Throttling on '%s'...", name));
            updateProtection(config, loadBalancer);
            LOG.info(String.format("Successfully updated Connection Throttling on '%s'.", name));
        }
    }

    @Override
    public void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws InsufficientRequestException, StmRollBackException {
        String name = ZxtmNameBuilder.genVSName(loadBalancer);

        LOG.info(String.format("Deleting Connection Throttling on '%s'...", name));
        if (loadBalancer.getAccessLists() != null && !loadBalancer.getAccessLists().isEmpty()) {
            ConnectionLimit nullConnectionLimit = new ConnectionLimit();
            nullConnectionLimit.setRateInterval(0);
            nullConnectionLimit.setMaxConnections(0);
            nullConnectionLimit.setMinConnections(0);
            nullConnectionLimit.setMaxConnectionRate(0);
            loadBalancer.setConnectionLimit(nullConnectionLimit);
            updateProtection(config, loadBalancer);
        } else {
            deleteProtection(config, loadBalancer);
        }
        LOG.info(String.format("Successfully deleted Connection Throttling on '%s'.", name));
    }

    /*
   Persistence Resources
    */

    private void createPersistentClasses(LoadBalancerEndpointConfiguration config) throws InsufficientRequestException {
        StingrayRestClient client = loadSTMRestClient(config);
        try {
            client.getPersistence(StmConstants.HTTP_COOKIE);
        } catch (StingrayRestClientException clientException) {
            LOG.error(String.format("Error retrieving Persistence Class '%s'.\n%s", StmConstants.HTTP_COOKIE,
                    Arrays.toString(clientException.getStackTrace())));
        } catch (StingrayRestClientObjectNotFoundException notFoundException) {
            Persistence persistence = new Persistence();
            PersistenceProperties properties = new PersistenceProperties();
            PersistenceBasic basic = new PersistenceBasic();

            basic.setType(StmConstants.HTTP_COOKIE);
            properties.setBasic(basic);
            persistence.setProperties(properties);
            try {
                LOG.info(String.format("Updating Persistence type %s...", StmConstants.HTTP_COOKIE));
                client.createPersistence(StmConstants.HTTP_COOKIE, persistence);
                LOG.info(String.format("Successfully updated Persistence type %s.", StmConstants.HTTP_COOKIE));
            } catch (Exception ex) {
                LOG.error(String.format("Error creating Persistence Class '%s'.\n%s", StmConstants.HTTP_COOKIE,
                        Arrays.toString(ex.getStackTrace())));
            }
        }

        try {
            client.getPersistence(StmConstants.SOURCE_IP);
        } catch (StingrayRestClientException clientException) {
            LOG.error(String.format("Error retrieving Persistence Class '%s'.\n%s", StmConstants.SOURCE_IP,
                    Arrays.toString(clientException.getStackTrace())));
        } catch (StingrayRestClientObjectNotFoundException notFoundException) {
            Persistence persistence = new Persistence();
            PersistenceProperties properties = new PersistenceProperties();
            PersistenceBasic basic = new PersistenceBasic();

            basic.setType(StmConstants.SOURCE_IP);
            properties.setBasic(basic);
            persistence.setProperties(properties);
            try {
                LOG.info(String.format("Updating Persistence type %s...", StmConstants.SOURCE_IP));
                client.createPersistence(StmConstants.SOURCE_IP, persistence);
                LOG.info(String.format("Successfully updated Persistence type %s.", StmConstants.SOURCE_IP));
            } catch (Exception ex) {
                LOG.error(String.format("Error creating Persistence Class '%s'.\n%s", StmConstants.SOURCE_IP,
                        Arrays.toString(ex.getStackTrace())));
            }
        }
        client.destroy();
    }


    /*
   SSL Termination Resources
    */

    @Override
    public void updateSslTermination(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, ZeusSslTermination sslTermination) throws InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);
        String vsName = ZxtmNameBuilder.genVSName(loadBalancer);
        String sslVsName = ZxtmNameBuilder.genSslVSName(loadBalancer);
        ResourceTranslator translator = new ResourceTranslator();
        translator.translateVirtualServerResource(config, sslVsName, loadBalancer);
        translator.translateKeypairResource(config, loadBalancer);
        VirtualServer createdServer = translator.getcVServer();
        VirtualServerHttp http = new VirtualServerHttp();
        http.setLocation_rewrite(EnumFactory.Accept_from.NEVER.toString());
        createdServer.getProperties().setHttp(http);
        if (loadBalancer.isSecureOnly()) {
            VirtualServer virtualServer = null;
            try {
                virtualServer = client.getVirtualServer(vsName);
            } catch (Exception e) {
                LOG.error(String.format("Error retrieving non-secure virtual server.\n%s", e.getStackTrace()));
                throw new StmRollBackException("Error retrieving non-secure virtual server.", e);
            }
            if (virtualServer != null) {
                virtualServer.getProperties().getBasic().setEnabled(false);
                updateVirtualServer(config, client, vsName, virtualServer);
            }
        }

        Keypair keypair = translator.getcKeypair();
        LOG.info(String.format("Updating certificate for load balancer: %s", loadBalancer.getId()));
        updateKeypair(config, client, sslVsName, keypair);

        try {
            translator.translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer);
            translator.translateLoadBalancerResource(config, sslVsName, loadBalancer, loadBalancer);

            if ((loadBalancer.getAccessLists() != null && !loadBalancer.getAccessLists().isEmpty())
                    || loadBalancer.getConnectionLimit() != null) {
                updateProtection(config, client, vsName, translator.getcProtection());
            }

            if (loadBalancer.getProtocol().equals(LoadBalancerProtocol.HTTP)) {
                TrafficScriptHelper.addXForwardedForScriptIfNeeded(client);
                TrafficScriptHelper.addXForwardedProtoScriptIfNeeded(client);
            }

            updateVirtualIps(config, client, sslVsName, translator.getcTrafficIpGroups());
            updateVirtualServer(config, client, sslVsName, createdServer);

        } catch (Exception ex) {
            LOG.error(ex);
            throw new StmRollBackException("Failed to update loadbalancer, rolling back...", ex);
        }
        client.destroy();
    }

    @Override
    public void removeSslTermination(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);
        String vsName = ZxtmNameBuilder.genSslVSName(loadBalancer);

        LOG.debug(String.format("Removing ssl from loadbalancer: %s ...", vsName));
        deleteKeypair(config, client, vsName);
        deleteVirtualServer(config, client, vsName);
        LOG.debug(String.format("Successfully removed ssl from loadbalancer: %s from the STM service...", vsName));
        client.destroy();
    }

    @Override
    public void addSuspension(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = null;
        boolean isEnabled = false;
        client = loadSTMRestClient(config);
        ResourceTranslator translator = new ResourceTranslator();
        String vsName = ZxtmNameBuilder.genVSName(lb);
        try {
            LOG.info(String.format("Attempting to disable virtual server %s", vsName));
            VirtualServer virtualServer = client.getVirtualServer(vsName);
            virtualServer.getProperties().getBasic().setEnabled(isEnabled);
            updateVirtualServer(config, client, vsName, virtualServer);
            LOG.info(String.format("Successfully disabled virtual server %s", vsName));
            if (lb.hasSsl()) {
                LOG.info(String.format("Attempting to disable virtual server %s", vsName));
                vsName = ZxtmNameBuilder.genSslVSName(lb);
                VirtualServer secureServer = client.getVirtualServer(vsName);
                secureServer.getProperties().getBasic().setEnabled(isEnabled);
                updateVirtualServer(config, client, vsName, secureServer);
                LOG.info(String.format("Successfully disabled virtual server %s", vsName));
            }
            LOG.info(String.format("Attempting to disable Traffic Ip Groups"));
            translator.translateTrafficIpGroupsResource(config, lb, isEnabled);
            updateVirtualIps(config, client, vsName, translator.getcTrafficIpGroups());
            LOG.info(String.format("Traffic Ip Groups disabled"));
            LOG.info("Successfully added load balancer suspension");
        } catch (Exception e) {
            LOG.error(String.format("Failed to suspend load balancer operation for load balancer: %s Exception: %s", lb.getId(), e));
            throw new StmRollBackException("Failed to suspend loadbalancer", e);
        }
        client.destroy();
    }

    @Override
    public void removeSuspension(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws InsufficientRequestException, RollBackException {
        StingrayRestClient client = null;
        client = loadSTMRestClient(config);
        boolean isEnabled = true;
        ResourceTranslator translator = new ResourceTranslator();
        String vsName = ZxtmNameBuilder.genVSName(lb);
        try {
            LOG.info(String.format("Attempting to enable virtual server %s", vsName));
            VirtualServer virtualServer = client.getVirtualServer(vsName);
            virtualServer.getProperties().getBasic().setEnabled(isEnabled);
            updateVirtualServer(config, client, vsName, virtualServer);
            LOG.info(String.format("Successfully enabled virtual server %s", vsName));
            if (lb.hasSsl()) {
                LOG.info(String.format("Attempting to enable virtual server %s", vsName));
                vsName = ZxtmNameBuilder.genSslVSName(lb);
                VirtualServer secureServer = client.getVirtualServer(vsName);
                secureServer.getProperties().getBasic().setEnabled(isEnabled);
                updateVirtualServer(config, client, vsName, secureServer);
                LOG.info(String.format("Successfully enabled virtual server %s", vsName));
            }
            LOG.info(String.format("Attempting to enable Traffic Ip Groups"));
            translator.translateTrafficIpGroupsResource(config, lb, isEnabled);
            updateVirtualIps(config, client, vsName, translator.getcTrafficIpGroups());
            LOG.info(String.format("Successfully enabled Traffic Ip Groups"));
            LOG.info("Successfully removed load balancer suspension");
        } catch (Exception e) {
            LOG.error(String.format("Failed to remove load balancer suspension for load balancer: %s. Exception: %s", lb.getId(), e));
            throw new StmRollBackException("Failed to remove loadbalancer suspension ", e);
        }
        client.destroy();
    }

    @Override
    public boolean isEndPointWorking(LoadBalancerEndpointConfiguration config) throws StmRollBackException {
        try {
            StingrayRestClient client = loadSTMRestClient(config);
            client.getPools();
            client.destroy();
            return true;
        } catch (Exception e) {
            if (IpHelper.isNetworkConnectionException(e)) {
                return false;
            }
            throw new StmRollBackException("Failed verifying endpoint", e);
        }
    }

    @Override
    public void changeHostForLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Host newHost) throws InsufficientRequestException, RollBackException {

    }

    /*
    Rate Limit (Bandwidth) Resources
     */

    @Override
    public void setRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, RateLimit rateLimit) throws InsufficientRequestException, StmRollBackException {
        loadBalancer.setRateLimit(rateLimit);
        setRateLimit(config, loadBalancer, ZxtmNameBuilder.genVSName(loadBalancer));
    }

    @Override
    public void updateRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, RateLimit rateLimit) throws InsufficientRequestException, StmRollBackException {
        loadBalancer.setRateLimit(rateLimit);
        updateRateLimit(config, loadBalancer, ZxtmNameBuilder.genVSName(loadBalancer));
    }

    @Override
    public void deleteRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws InsufficientRequestException, StmRollBackException {
        deleteRateLimit(config, loadBalancer, ZxtmNameBuilder.genVSName(loadBalancer));

    }


    private void setRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, String vsName) throws InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);
        ResourceTranslator rt = new ResourceTranslator();

        try {
            LOG.debug(String.format("Adding a rate limit to load balancer...'%s'...", vsName));

            rt.translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer);
            Bandwidth bandwidth = rt.getcBandwidth();
            VirtualServer virtualServer = rt.getcVServer();
            virtualServer.getProperties().getBasic().setBandwidth_class(vsName);

            client.createBandwidth(vsName, bandwidth);
            TrafficScriptHelper.addRateLimitScriptsIfNeeded(client);
            updateVirtualServer(config, client, vsName, virtualServer);


            LOG.info("Successfully added a rate limit to the rate limit pool.");
        } catch (StingrayRestClientObjectNotFoundException e) {
            LOG.error(String.format("Failed to add rate limit for virtual server '%s' -- Object not found", vsName));
            throw new StmRollBackException("Add rate limit request canceled.", e);
        } catch (StingrayRestClientException e) {
            LOG.error(String.format("Failed to add rate limit for virtual server %s -- REST Client exception", vsName));
            throw new StmRollBackException("Add rate limit request canceled.", e);
        } catch (IOException e) {
            LOG.error(String.format("Failed to add rate limit for virtual server %s -- IOException", vsName));
            throw new StmRollBackException("Add rate limit request canceled.", e);
        }
        client.destroy();
    }


    public void updateRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, String vsName) throws InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);
        ResourceTranslator rt = new ResourceTranslator();

        try {
            LOG.debug(String.format("Updating the rate limit for load balancer...'%s'...", vsName));

            TrafficScriptHelper.addRateLimitScriptsIfNeeded(client);

            rt.translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer);
            Bandwidth bandwidth = rt.getcBandwidth();

            client.updateBandwidth(vsName, bandwidth);

            LOG.info(String.format("Successfully updated the rate limit for load balancer...'%s'...", vsName));
        } catch (StingrayRestClientObjectNotFoundException e) {
            LOG.error(String.format("Failed to update rate limit for virtual server %s -- REST Client exception", vsName));
            throw new StmRollBackException("Update rate limit request canceled.", e);
        } catch (StingrayRestClientException e) {
            LOG.error(String.format("Failed to update rate limit for virtual server %s -- REST Client exception", vsName));
            throw new StmRollBackException("Update rate limit request canceled.", e);
        } catch (IOException e) {
            LOG.error(String.format("Failed to update rate limit for virtual server %s -- IOException", vsName));
            throw new StmRollBackException("Update rate limit request canceled.", e);
        }
        client.destroy();
    }


    private void deleteRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, String vsName) throws StmRollBackException, InsufficientRequestException {
        StingrayRestClient client = loadSTMRestClient(config);

        try {
            ResourceTranslator rt = new ResourceTranslator();
            rt.translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer);
            VirtualServer virtualServer = rt.getcVServer();
            VirtualServerProperties properties = virtualServer.getProperties();
            VirtualServerBasic basic = properties.getBasic();

            basic.setBandwidth_class("");
            client.deleteBandwidth(vsName);
            updateVirtualServer(config, client, vsName, virtualServer);

        } catch (StingrayRestClientObjectNotFoundException e) {
            LOG.warn(String.format("Cannot delete rate limit '%s', it does not exist. Ignoring...", vsName));
        } catch (StingrayRestClientException e) {
            LOG.error(String.format("Failed to delete rate limit for virtual server %s -- REST Client exception", vsName));
            throw new StmRollBackException("Delete rate limit request canceled.", e);
        }
        client.destroy();
    }

    /*
    Error File Resources
     */


    @Override
    public void uploadDefaultErrorFile(LoadBalancerEndpointConfiguration config, String content)
            throws InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);
        File errorFile = null;

        try {
            LOG.debug("Attempting to upload the default error file...");
            errorFile = getFileWithContent(content);
            client.createExtraFile(Constants.DEFAULT_ERRORFILE, errorFile);
            LOG.info("Successfully uploaded the default error file...");
        } catch (IOException e) {
            LOG.error(String.format("Failed to upload default ErrorFile for %s -- IO exception", config.getEndpointUrl()));
        } catch (StingrayRestClientException ce) {
            LOG.error(String.format("Failed to upload default ErrorFile for %s -- REST Client exception", config.getEndpointUrl()));
        } catch (StingrayRestClientObjectNotFoundException onf) {
            LOG.error(String.format("Failed to upload default ErrorFile for %s -- Object not found", config.getEndpointUrl()));
        }

        if (errorFile != null) errorFile.delete();
        client.destroy();
    }

    @Override
    public void deleteErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);
        deleteErrorFile(config, client, loadBalancer, ZxtmNameBuilder.genVSName(loadBalancer));
        client.destroy();
    }

    private void deleteErrorFile(LoadBalancerEndpointConfiguration config, StingrayRestClient client, LoadBalancer loadBalancer, String vsName)
            throws InsufficientRequestException, StmRollBackException {

        ResourceTranslator rt = new ResourceTranslator();
        rt.translateVirtualServerResource(config, vsName, loadBalancer);
        VirtualServer vs = rt.getcVServer();
        String fileToDelete = ZxtmNameBuilder.generateErrorPageName(vsName);
        try {
            LOG.debug(String.format("Attempting to delete a custom error file for %s (%s)", vsName, fileToDelete));

            // Update client with new properties
            VirtualServerProperties properties = vs.getProperties();
            VirtualServerConnectionError ce = new VirtualServerConnectionError();
            ce.setError_file("Default");
            properties.setConnection_errors(ce); // this will set the default error page
            updateVirtualServer(config, client, vsName, vs);

            // Delete the old error file
            client.deleteExtraFile(fileToDelete);

            LOG.info(String.format("Successfully deleted a custom error file for %s (%s)", vsName, fileToDelete));
        } catch (StingrayRestClientObjectNotFoundException onf) {
            LOG.warn(String.format("Cannot delete custom error page as, %s, it does not exist. Ignoring...", fileToDelete));
        } catch (StmRollBackException re) {
            LOG.error(String.format("Failed deleting the error file for: %s Exception: %s", vsName, re.getMessage()));
            throw re;
        } catch (StingrayRestClientException e) {
            LOG.error(String.format("There was a unexpected error deleting the error file for: %s Exception: %s", vsName, e.getMessage()));
            throw new StmRollBackException("Deleting error file cancelled.", e);
        }
    }

    @Override
    public void setErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, String content) throws InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);
        setErrorFile(config, client, loadBalancer, ZxtmNameBuilder.genVSName(loadBalancer), content);
        client.destroy();
    }

    private void setErrorFile(LoadBalancerEndpointConfiguration config, StingrayRestClient client, LoadBalancer loadBalancer, String vsName, String content) throws InsufficientRequestException, StmRollBackException {
        File errorFile = null;
        String errorFileName = ZxtmNameBuilder.generateErrorPageName(vsName);

        ResourceTranslator rt = new ResourceTranslator();
        rt.translateVirtualServerResource(config, vsName, loadBalancer);
        VirtualServer vs = rt.getcVServer();

        //todo: ?rollback
        try {
            LOG.debug(String.format("Attempting to upload the error file for %s (%s)", vsName, errorFileName));
            errorFile = getFileWithContent(content);
            client.createExtraFile(errorFileName, errorFile);
            LOG.info(String.format("Successfully uploaded the error file for %s (%s)", vsName, errorFileName));
        } catch (Exception e) {
            // Failed to create file, use "Default"
            // Failed to create error file, error out..
            LOG.error(String.format("Failed to set ErrorFile for %s (%s) Exception: %s -- exception", vsName, errorFileName, e));
            errorFileName = "Default";
            throw new StmRollBackException(String.format("Failed creating error page %s for: %s.", errorFileName, vsName), e);
        }

        if (errorFile != null) errorFile.delete();

        try {
            LOG.debug(String.format("Attempting to set the error file for %s (%s)", vsName, errorFileName));
            // Update client with new properties
            updateVirtualServer(config, client, vsName, vs);

            LOG.info(String.format("Successfully set the error file for %s (%s)", vsName, errorFileName));
        } catch (StmRollBackException re) {
            // REST failure...
            LOG.error(String.format("Failed to set ErrorFile for %s (%s) -- Rolling back.", vsName, errorFileName));
            throw re;
        }

    }

    private File getFileWithContent(String content) throws IOException {
        File file = File.createTempFile("StmAdapterImpl_", ".err");
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write('\n' + content);
        out.close();
        return file;
    }


    /**
     * Deprecating these(SubnetMapping calls) as per ops. Unused call that is difficult to test, may support in future if needed... *
     */

//    @Override
//    public void setSubnetMappings(LoadBalancerEndpointConfiguration config, Hostssubnet hostssubnet) throws StmRollBackException {
//        StingrayRestClient client;
//        try {
//            client = loadSTMRestClient(config);
//            List<Hostsubnet> subnetList = hostssubnet.getHostsubnets();
//
//            //Loop over Hosts ("dev1.lbaas.mysite.com", "dev2.lbaas.mysite.com", etc)
//            for (Hostsubnet hostsubnet : subnetList) {
//                String hsName = hostsubnet.getName();
//                TrafficManager trafficManager = client.getTrafficManager(hsName);
//                List<TrafficManagerTrafficIp> trafficManagerTrafficIpList = new ArrayList<TrafficManagerTrafficIp>();
//                List<NetInterface> interfaceList = hostsubnet.getNetInterfaces();
//
//                //Loop over interfaces (eth0, eth1, etc)
//                for (NetInterface netInterface : interfaceList) {
//                    List<Cidr> cidrList = netInterface.getCidrs();
//                    TrafficManagerTrafficIp trafficManagerTrafficIp = new TrafficManagerTrafficIp();
//                    Set<String> networkList = new HashSet<String>();
//
//                    // Loop over Cidr list which contains one subnet per Cidr
//                    for (Cidr cidr : cidrList) {
//                        networkList.add(cidr.getBlock());
//                    }
//
//                    trafficManagerTrafficIp.setName(netInterface.getName());
//                    trafficManagerTrafficIp.setNetworks(networkList);
//                    trafficManagerTrafficIpList.add(trafficManagerTrafficIp);
//                }
//                trafficManager.getProperties().getBasic().setTrafficip(trafficManagerTrafficIpList);
//                client.updateTrafficManager(hsName, trafficManager);
//            }
//        } catch (StingrayRestClientObjectNotFoundException e) {
//            throw new StmRollBackException("Failed updating subnet mappings", e);
//        } catch (StingrayRestClientException e) {
//            throw new StmRollBackException("Failed updating subnet mappings", e);
//        }
//    }
//
//    @Override
//    public void deleteSubnetMappings(LoadBalancerEndpointConfiguration config, Hostssubnet hostssubnet) throws StmRollBackException {
//        StingrayRestClient client;
//        try {
//            client = loadSTMRestClient(config);
//            List<Hostsubnet> subnetList = hostssubnet.getHostsubnets();
//
//            //Loop over Hosts ("dev1.lbaas.mysite.com", "dev2.lbaas.mysite.com", etc)
//            for (Hostsubnet hostsubnet : subnetList) {
//                String hsName = hostsubnet.getName();       // This name is of the form "dev1.lbaas.mysite.com"
//                TrafficManager trafficManager = client.getTrafficManager(hsName);
//                List<NetInterface> netInterfaceList = hostsubnet.getNetInterfaces();
//                //trafficManagerTrafficIpList is the current list of TrafficIPs for the host
//                List<TrafficManagerTrafficIp> trafficManagerTrafficIpList = trafficManager.getProperties().getBasic().getTrafficip();
//                Map<String, TrafficManagerTrafficIp> tipsMap = new HashMap<String, TrafficManagerTrafficIp>();
//
//                //Loop over tips to compile an indexed list by name
//                for (TrafficManagerTrafficIp trafficManagerTrafficIp : trafficManagerTrafficIpList) {
//                    tipsMap.put(trafficManagerTrafficIp.getName(), trafficManagerTrafficIp);
//                }
//
//                //Loop over interfaces (eth0, eth1, etc)
//                for (NetInterface netInterface : netInterfaceList) {
//                    String netInterfaceName = netInterface.getName(); //This name is of the form "eth0"
//
//                    if (tipsMap.containsKey(netInterfaceName)) {
//                        TrafficManagerTrafficIp tip = tipsMap.get(netInterfaceName);
//                        Set<String> networkSet = tip.getNetworks();
//                        List<Cidr> cidrList = netInterface.getCidrs(); //This is the list of objects containing subnet strings
//
//                        // Loop over Cidr list which contains one subnet per Cidr
//                        for (Cidr cidr : cidrList) {
//                            networkSet.remove(cidr.getBlock()); //Remove the subnet if it exists
//                        }
//                    }
//                }
//                client.updateTrafficManager(hsName, trafficManager);
//            }
//        } catch (StingrayRestClientObjectNotFoundException e) {
//            throw new StmRollBackException("Failed removing subnet mappings", e);
//        } catch (StingrayRestClientException e) {
//            throw new StmRollBackException("Failed removing subnet mappings", e);
//        }
//    }
//
//    @Override
//    public Hostssubnet getSubnetMappings(LoadBalancerEndpointConfiguration config, String host) throws StmRollBackException {
//        StingrayRestClient client;
//        Hostssubnet ret = new Hostssubnet();
//        try {
//            client = loadSTMRestClient(config);
//            TrafficManager trafficManager = client.getTrafficManager(host);
//            //trafficManagerTrafficIpList is the current list of TrafficIPs for the host
//            List<TrafficManagerTrafficIp> trafficManagerTrafficIpList = trafficManager.getProperties().getBasic().getTrafficip();
//            List<Hostsubnet> subnetList = new ArrayList<Hostsubnet>();
//            Hostsubnet hostsubnet = new Hostsubnet();
//            hostsubnet.setName(host);
//
//            //Loop over trafficIPs (== interfaces) (eth0, eth1, etc)
//            for (TrafficManagerTrafficIp trafficManagerTrafficIp : trafficManagerTrafficIpList) {
//                Set<String> networkSet = trafficManagerTrafficIp.getNetworks();
//                NetInterface netInterface = new NetInterface();
//                List<Cidr> cidrs = new ArrayList<Cidr>();
//
//                //Loop over networks (== cidr blocks)
//                for (String block : networkSet) {
//                    Cidr cidr = new Cidr();
//                    cidr.setBlock(block);
//                    cidrs.add(cidr);
//                }
//
//                netInterface.setName(trafficManagerTrafficIp.getName());
//                netInterface.setCidrs(cidrs);
//                hostsubnet.getNetInterfaces().add(netInterface);
//            }
//            subnetList.add(hostsubnet);
//            ret.setHostsubnets(subnetList);
//        } catch (StingrayRestClientObjectNotFoundException e) {
//            throw new StmRollBackException("Failed retrieving subnet mappings", e);
//        } catch (StingrayRestClientException e) {
//            throw new StmRollBackException("Failed retrieving subnet mappings", e);
//        }
//        return ret;
//    }

}
