package org.openstack.atlas.adapter.stm;

import org.apache.axis.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.adapter.helpers.ResourceTranslator;
import org.openstack.atlas.adapter.helpers.StmConstants;
import org.openstack.atlas.adapter.helpers.TrafficScriptHelper;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerStmAdapter;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.*;
import org.openstack.atlas.service.domain.util.Constants;
import org.openstack.atlas.service.domain.util.StringUtilities;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.bandwidth.Bandwidth;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.monitor.Monitor;
import org.rackspace.stingray.client.persistence.Persistence;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.pool.PoolBasic;
import org.rackspace.stingray.client.protection.Protection;
import org.rackspace.stingray.client.tm.TrafficManager;
import org.rackspace.stingray.client.tm.TrafficManagerTrafficIp;
import org.rackspace.stingray.client.traffic.ip.TrafficIp;
import org.rackspace.stingray.client.virtualserver.VirtualServer;
import org.rackspace.stingray.client.virtualserver.VirtualServerBasic;
import org.rackspace.stingray.client.virtualserver.VirtualServerConnectionError;
import org.rackspace.stingray.client.virtualserver.VirtualServerProperties;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.*;

public class StmAdapterImpl implements ReverseProxyLoadBalancerStmAdapter {
    public static Log LOG = LogFactory.getLog(StmAdapterImpl.class.getName());

    public StingrayRestClient loadSTMRestClient(LoadBalancerEndpointConfiguration config) throws StmRollBackException {
        StingrayRestClient client;
        try {
            client = new StingrayRestClient(new URI(config.getEndpointUrl().toString()));
        } catch (URISyntaxException e) {
            LOG.error(String.format("Configuration error, verify soapendpoint is valid! Exception %s", e));
            throw new StmRollBackException("Configuration error: ", e);
        }
        return client;
    }

    @Override
    public void createLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, StmRollBackException {
        try {
            updateLoadBalancer(config, loadBalancer);
        } catch (Exception e) {
            LOG.error(String.format("Failed to create load balancer %s, rolling back...", loadBalancer.getId()));
            deleteLoadBalancer(config, loadBalancer);
            throw new StmRollBackException(String.format("Failed to create load balancer %s", loadBalancer.getId()), e);
        }
    }

    @Override
    public void updateLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, StmRollBackException {

        StingrayRestClient client = loadSTMRestClient(config);

        ResourceTranslator translator = new ResourceTranslator();

        List<String> vsNames = new ArrayList<String>();
        if (loadBalancer.isUsingSsl()) {
            vsNames.add(ZxtmNameBuilder.genSslVSName(loadBalancer));
        }
        vsNames.add(ZxtmNameBuilder.genVSName(loadBalancer));

        for (String vsName : vsNames) {
            try {

                translator.translateLoadBalancerResource(config, vsName, loadBalancer);

                if (loadBalancer.getHealthMonitor() != null && !loadBalancer.hasSsl()) {
                    updateHealthMonitor(config, client, vsName, translator.getcMonitor());
                }

                if ((loadBalancer.getAccessLists() != null && !loadBalancer.getAccessLists().isEmpty())
                        || loadBalancer.getConnectionLimit() != null) {
                    updateProtection(config, client, loadBalancer, translator.getcProtection());
                }

                if (loadBalancer.getProtocol().equals(LoadBalancerProtocol.HTTP)) {
                    TrafficScriptHelper.addXForwardedForScriptIfNeeded(client);
                    TrafficScriptHelper.addXForwardedProtoScriptIfNeeded(client);
                }

                updateVirtualIps(config, client, vsName, translator.getcTrafficIpGroups());
                updateNodePool(config, client, vsName, translator.getcPool());
                updateVirtualServer(config, client, vsName, translator.getcVServer());
            } catch (Exception ex) {
                LOG.error(ex);
                //TODO: roll back or handle as needed.. ...
                throw new StmRollBackException("Failed to update loadbalancer, rolling back...", ex);
            }
        }
        //Finish...
    }

    @Override
    public void deleteLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);
        String vsName = ZxtmNameBuilder.genVSName(loadBalancer);

        LOG.debug(String.format("Removing loadbalancer: %s ...", vsName));
        //others...
        deleteHealthMonitor(config, client, vsName);
//        deleteProtection(config, client, vsName);
        deleteVirtualIps(config, loadBalancer);
        deleteNodePool(config, client, vsName);
        deleteVirtualServer(config, client, vsName);
        LOG.debug(String.format("Successfully removed loadbalancer: %s from the STM service...", vsName));
    }

    /*
       Virtual Server Resources
    */
    private void updateVirtualServer(LoadBalancerEndpointConfiguration config,
                                     StingrayRestClient client, String vsName, VirtualServer virtualServer)
            throws StmRollBackException {

        LOG.debug(String.format("Updating  virtual server '%s'...", vsName));

        VirtualServer curVs = null;
        try {
            curVs = client.getVirtualServer(vsName);
        } catch (Exception e) {
            LOG.warn(String.format("Error updating virtual server: %s, attempting to recreate... ", virtualServer));
        }

        try {
            client.updateVirtualServer(vsName, virtualServer);
        } catch (Exception ex) {
            String em = String.format("Error updating virtual server: %s Attempting to RollBack... \n Exception: %s Trace: %s"
                    , vsName, ex.getCause().getMessage(), Arrays.toString(ex.getCause().getStackTrace()));

            LOG.error(em);
            if (curVs != null) {
                LOG.debug(String.format("Updating virtual server to previous configuration for rollback '%s'", vsName));
                try {
                    client.updateVirtualServer(vsName, virtualServer);
                } catch (Exception ex2) {
                    String em2 = String.format("Error updating virtual server while attempting to previous configuration" +
                            ": %s RollBack aborted \n Exception: %s Trace: %s"
                            , vsName, ex2.getCause().getMessage(), Arrays.toString(ex2.getCause().getStackTrace()));
                    LOG.error(em2);
                }
            } else {
                LOG.warn(String.format("Virtual server was not rolled back as no previous configuration was available. '%s' ", vsName));
            }
            throw new StmRollBackException(em, ex);
        }
        LOG.debug(String.format("Successfully updated virtual server '%s'...", vsName));

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
            String em = String.format("Error removing virtual server: %s Attempting to RollBack... \n Exception: %s Trace: %s"
                    , vsName, ex.getCause().getMessage(), Arrays.toString(ex.getCause().getStackTrace()));
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
            throws RemoteException, InsufficientRequestException, StmRollBackException {

        final String poolName = ZxtmNameBuilder.genVSName(loadBalancer);
        ResourceTranslator translator = new ResourceTranslator();
        StingrayRestClient client = loadSTMRestClient(config);
        translator.translateLoadBalancerResource(config, poolName, loadBalancer);
        updateNodePool(config, client, poolName, translator.getcPool());

    }

    private void updateNodePool(LoadBalancerEndpointConfiguration config,
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
            String em = String.format("Error updating node pool: %s Attempting to RollBack... \n Exception: %s Trace: %s"
                    , poolName, ex.getCause().getMessage(), Arrays.toString(ex.getCause().getStackTrace()));

            LOG.error(em);
            if (curPool != null) {
                LOG.debug(String.format("Updating pool to previous configuration for rollback '%s'", poolName));
                try {
                    client.updatePool(poolName, curPool);
                } catch (Exception ex2) {
                    String em2 = String.format("Error updating node pool while attempting to previous configuration" +
                            ": %s RollBack aborted \n Exception: %s Trace: %s"
                            , poolName, ex2.getCause().getMessage(), Arrays.toString(ex2.getCause().getStackTrace()));
                    LOG.error(em2);
                }
            } else {
                LOG.warn(String.format("Node Pool was not rolled back as no previous configuration was available. '%s' ", poolName));
            }
            throw new StmRollBackException(em, ex);
        }
    }

    private void deleteNodePool(LoadBalancerEndpointConfiguration config,
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
        } catch (StingrayRestClientObjectNotFoundException one) {
            LOG.warn(String.format("Pool object not found: %s, continue...", poolName));

        } catch (Exception ex) {
            String em = String.format("Error removing node pool: %s Attempting to RollBack... \n Exception: %s Trace: %s"
                    , poolName, ex.getCause().getMessage(), Arrays.toString(ex.getCause().getStackTrace()));

            LOG.error(em);
            if (curPool != null) {
                LOG.debug(String.format("Updating pool to previous configuration for rollback '%s'", poolName));
                try {
                    client.updatePool(poolName, curPool);
                } catch (Exception ex2) {
                    String em2 = String.format("Error updating node pool while attempting to previous configuration" +
                            ": %s RollBack aborted \n Exception: %s Trace: %s"
                            , poolName, ex2.getCause().getMessage(), Arrays.toString(ex2.getCause().getStackTrace()));
                    LOG.error(em2);
                }
            } else {
                LOG.warn(String.format("Node Pool was not rolled back as no previous configuration was available. '%s' ", poolName));
            }
            throw new StmRollBackException(em, ex);
        }
        LOG.info(String.format("Successfully removed pool '%s'...", poolName));
    }

    @Override
    public void removeNodes(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws AxisFault, InsufficientRequestException, StmRollBackException {
        final String poolName = ZxtmNameBuilder.genVSName(loadBalancer);
        ResourceTranslator translator = new ResourceTranslator();
        StingrayRestClient client = loadSTMRestClient(config);
        translator.translateLoadBalancerResource(config, poolName, loadBalancer);
        LOG.info(String.format("Removing nodes from pool '%s'", poolName));
        updateNodePool(config, client, poolName, translator.getcPool());
        LOG.info(String.format("Successfully removed nodes from pool '%s'", poolName));
    }

    @Override
    public void removeNode(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Node nodeToDelete) throws RemoteException, InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);
        String vsName = ZxtmNameBuilder.genVSName(loadBalancer);

        Pool pool = null;


        try {
            pool = client.getPool(vsName);
        } catch (Exception e) {
            LOG.error(String.format("Loading pool %s configuration failed, node %s:%d not removed...", vsName, nodeToDelete.getIpAddress(), nodeToDelete.getPort()));
            throw new StmRollBackException("Remove node request canceled.", e);
        }

        ResourceTranslator translator = new ResourceTranslator();
        loadBalancer.getNodes().remove(nodeToDelete);
        translator.translateLoadBalancerResource(config, vsName, loadBalancer);

        String nodeToRemove = nodeToDelete.getIpAddress() + ":" + Integer.toString(nodeToDelete.getPort());
        PoolBasic basic = pool.getProperties().getBasic();
        Set<String> existingNodes = basic.getNodes();
        if (existingNodes.contains(nodeToRemove)) {
            LOG.info(String.format("Removing node %s from pool...", nodeToRemove));
            existingNodes.remove(nodeToRemove);
            updateNodePool(config, client, vsName, translator.getcPool());
            LOG.info(String.format("Successfully removed node %s from pool!", nodeToRemove));
        } else {
            LOG.warn(String.format("Node '%s:%d' for pool: %s does not exist. Ignoring..", nodeToDelete.getIpAddress(), nodeToDelete.getPort(), vsName));
        }
    }


    /*
       VirtualIP Resources
    */

    private void updateVirtualIps(LoadBalancerEndpointConfiguration config,
                                  StingrayRestClient client, String vsName, Map<String, TrafficIp> tigmap)
            throws StmRollBackException {

        LOG.debug(String.format("Updating virtual ips for '%s'...", vsName));

        TrafficIp curTig = null;

        for (Map.Entry<String, TrafficIp> tm : tigmap.entrySet()) {
            try {
                curTig = client.getTrafficIp(tm.getKey());
            } catch (Exception e) {
                LOG.warn(String.format("Could not load virtual ips for: %s, attempting to recreate...", vsName));
            }

            try {
                client.updateTrafficIp(tm.getKey(), tm.getValue());
            } catch (Exception ex) {
                String em = String.format("Error updating virtual ips: %s Attempting to RollBack... \n Exception: %s Trace: %s"
                        , vsName, ex.getCause().getMessage(), Arrays.toString(ex.getCause().getStackTrace()));

                LOG.error(em);
                if (curTig != null) {
                    LOG.debug(String.format("Updating virtual ips to previous configuration for rollback '%s'", vsName));
                    try {
                        client.updateTrafficIp(vsName, curTig);
                    } catch (Exception ex2) {
                        String em2 = String.format("Error updating virtual ips while attempting to set previous configuration" +
                                ": %s RollBack aborted \n Exception: %s Trace: %s"
                                , vsName, ex2.getCause().getMessage(), Arrays.toString(ex2.getCause().getStackTrace()));
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

    @Override
    public void addVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);
        ResourceTranslator translator = new ResourceTranslator();
        String vsName = ZxtmNameBuilder.genVSName(loadBalancer);
        translator.translateLoadBalancerResource(config, vsName, loadBalancer);
        LOG.debug(String.format("Updating virtual ips for virtual server %s", vsName));
        updateVirtualIps(config, client, vsName, translator.getcTrafficIpGroups());
        LOG.debug(String.format("Updating virtual server %s for virtual ip configuration update", vsName));
        updateVirtualServer(config, client, vsName, translator.getcVServer());
        LOG.info(String.format("Successfully updated virtual ips for virtual server %s", vsName));
    }

    @Override
    public void deleteVirtualIp(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Integer vipId) throws RemoteException, InsufficientRequestException, StmRollBackException {
        //Think is was only used in old tests, do we still need it?
        List<Integer> vipIds = new ArrayList<Integer>();
        vipIds.add(vipId);
        deleteVirtualIps(config, loadBalancer, vipIds);
    }

    @Override
    public void deleteVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, List<Integer> vipIds) throws RemoteException, InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);
        ResourceTranslator translator = new ResourceTranslator();
        String vsName;
        vsName = ZxtmNameBuilder.genVSName(loadBalancer);

        translator.translateLoadBalancerResource(config, vsName, loadBalancer);
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

        String vipsToRemove = StringUtilities.DelimitString(vipIds, ",");
        translator.translateLoadBalancerResource(config, vsName, loadBalancer);
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
            String em = String.format("Error removing virtual ips for vs: %s ... \n Exception: %s Trace: %s",
                    vsName, ex.getCause().getMessage(), Arrays.toString(ex.getCause().getStackTrace()));
            LOG.error(em);
            if (!curTigMap.isEmpty()) {
                LOG.debug(String.format("Attempting to roll back to previous virtual ips: %s for virtual server %s", vipsToRemove, vsName));
                updateVirtualIps(config, client, vsName, curTigMap);
                LOG.info(String.format("Successfully rolled back to previous virtual ips: %s for virtual server %s", vipsToRemove, vsName));
            }
            throw new StmRollBackException(em, ex);
        }
    }

    public void deleteVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, StmRollBackException {
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
            throws RemoteException, InsufficientRequestException, StmRollBackException {

        final String vsName = ZxtmNameBuilder.genVSName(loadBalancer);
        ResourceTranslator translator = new ResourceTranslator();
        StingrayRestClient client = loadSTMRestClient(config);

        translator.translateLoadBalancerResource(config, vsName, loadBalancer);
        updateHealthMonitor(config, client, vsName, translator.getcMonitor());
        updateNodePool(config, client, vsName, translator.getcPool());
    }

    @Override
    public void removeHealthMonitor(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, StmRollBackException {
        final String monitorName = ZxtmNameBuilder.genVSName(loadBalancer);
        ResourceTranslator translator = new ResourceTranslator();
        StingrayRestClient client = loadSTMRestClient(config);
        translator.translateLoadBalancerResource(config, monitorName, loadBalancer);
        deleteHealthMonitor(config, client, monitorName);
    }

    private void updateHealthMonitor(LoadBalancerEndpointConfiguration config,
                                     StingrayRestClient client, String monitorName, Monitor monitor)
            throws StmRollBackException {

        LOG.debug(String.format("Update Monitor '%s' ...", monitor));

        Monitor curMon = null;
        try {
            curMon = client.getMonitor(monitorName);
        } catch (Exception e) {
            LOG.warn(String.format("Could not locate: %s, attempting to recreate...", monitorName));
        }

        try {
            client.updateMonitor(monitorName, monitor);
        } catch (Exception ex) {
            String em = String.format("Error updating virtual server: %s Attempting to RollBack... \n Exception: %s Trace: %s"
                    , monitorName, ex.getCause().getMessage(), Arrays.toString(ex.getCause().getStackTrace()));

            LOG.error(em);
            if (curMon != null) {
                LOG.debug(String.format("Updating monitor to previous configuration for rollback '%s'", monitorName));
                try {
                    client.updateMonitor(monitorName, curMon);
                } catch (Exception ex2) {
                    String em2 = String.format("Error updating monitor while attempting to set previous configuration" +
                            ": %s RollBack aborted \n Exception: %s Trace: %s"
                            , monitorName, ex2.getCause().getMessage(), Arrays.toString(ex2.getCause().getStackTrace()));
                    LOG.error(em2);
                }
            } else {
                LOG.warn(String.format("Monitor was not rolled back as no previous configuration was available. '%s' ", monitorName));
            }
            throw new StmRollBackException(em, ex);
        }
        LOG.debug(String.format("Successfully updated Monitor '%s' ...", monitor));
    }

    private void deleteHealthMonitor(LoadBalancerEndpointConfiguration config,
                                     StingrayRestClient client, String monitorName)
            throws StmRollBackException {

        LOG.info(String.format("Removing  monitor '%s'...", monitorName));

        Monitor curMon = null;
        try {
            curMon = client.getMonitor(monitorName);
            client.deleteMonitor(monitorName);
        } catch (StingrayRestClientObjectNotFoundException ex) {
            LOG.error(String.format("Cloud not locate monitor: %s, continue...", monitorName));
        } catch (StingrayRestClientException ex) {
            String em = String.format("Error removing monitor: %s Attempting to RollBack... \n Exception: %s Trace: %s"
                    , monitorName, ex.getCause().getMessage(), Arrays.toString(ex.getCause().getStackTrace()));
            LOG.error(em);
            if (curMon != null) {
                LOG.debug(String.format("Updating virtual server to set previous configuration for rollback '%s'", monitorName));
                updateHealthMonitor(config, client, monitorName, curMon);
            } else {
                LOG.warn(String.format("Monitor was not rolled back as no previous configuration was available. '%s' ", monitorName));
            }
            throw new StmRollBackException(em, ex);
        }
        LOG.info(String.format("Successfully removed monitor '%s'...", monitorName));
    }

    /*
        Protection Resources
     */

    private void rollbackProtection(StingrayRestClient client, LoadBalancer loadBalancer, Protection curProtection) throws InsufficientRequestException, StmRollBackException {
        String protectionName = ZxtmNameBuilder.genVSName(loadBalancer);
        String protectionSslName = ZxtmNameBuilder.genSslVSName(loadBalancer);
        try {
            LOG.info("Attempting to rollback " + protectionSslName + "...");
            if (curProtection == null) {
                if (loadBalancer.hasSsl()) {
                    LOG.debug(String.format("Deleting protection for rollback '%s'", protectionSslName));
                    client.deleteProtection(protectionSslName);
                }
                LOG.debug(String.format("Deleting protection for rollback '%s'", protectionName));
                client.deleteProtection(protectionName);
            }
            if (loadBalancer.hasSsl()) {
                LOG.debug(String.format("Updating protection for rollback '%s'", protectionSslName));
                client.updateProtection(protectionSslName, curProtection);
            }
            LOG.debug(String.format("Updating protection for rollback '%s'", protectionName));
            client.updateProtection(protectionName, curProtection);
        } catch (StingrayRestClientException ex) {
            LOG.error(String.format("Error updating monitor: %s Rolling back! \n Exception: %s Trace: %s"
                    , protectionName, ex.getCause().getMessage(), Arrays.toString(ex.getCause().getStackTrace())));
            throw new StmRollBackException(String.format("Error updating protection: %s Rolling back! \n Exception: %s Trace: %s"
                    , protectionName, ex.getCause().getMessage(), Arrays.toString(ex.getCause().getStackTrace())), ex);
        } catch (StingrayRestClientObjectNotFoundException ex) {
            LOG.warn(String.format("Object not found when creating protection: %s, this is expected...", protectionName));
        }
    }

    /*
        Connection Logging Resources
     */


    @Override
    public void updateConnectionLogging(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, StmRollBackException {
        String vsName = ZxtmNameBuilder.genVSName(loadBalancer);
        ResourceTranslator translator = new ResourceTranslator();
        StingrayRestClient client = loadSTMRestClient(config);
        translator.translateLoadBalancerResource(config, vsName, loadBalancer);

        updateVirtualServer(config, client, vsName, translator.getcVServer());
    }

    @Override
    public void updateProtocol(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, StmRollBackException {
        String vsName = ZxtmNameBuilder.genVSName(loadBalancer);
        ResourceTranslator translator = new ResourceTranslator();
        StingrayRestClient client = loadSTMRestClient(config);
        translator.translateLoadBalancerResource(config, vsName, loadBalancer);

        updateVirtualServer(config, client, vsName, translator.getcVServer());
    }

    @Override
    public void updateHalfClosed(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws RemoteException, InsufficientRequestException, StmRollBackException {

    }


    //TODO:
    // We can honestly get rid of alot of these one-offs by using the proper update'Resource'. example here, updateVirtualServer can handle this
    //But that means we have to update listeners and other code also..
    @Override
    public void updatePort(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Integer port) throws RemoteException, InsufficientRequestException, StmRollBackException {


        //TODO do we want to pass a loadbalancer in?

        StingrayRestClient client = loadSTMRestClient(config);
        String vsName = Integer.toString(accountId) + "_" + Integer.toString(loadBalancerId);
        try {
            LOG.debug(String.format("Updating ort to '%d' for virtual server '%s'..", port, vsName));
            VirtualServer virtualServer = client.getVirtualServer(vsName);
            virtualServer.getProperties().getBasic().setPort(port);
            client.updateVirtualServer(vsName, virtualServer);
            LOG.info(String.format("Successfully updated port for "));

        } catch (StingrayRestClientObjectNotFoundException onf) {
            LOG.error(String.format("Cannot update port for virtual server '%s' as it does not exist.", vsName), onf);
        } catch (StingrayRestClientException e) {
            throw new StmRollBackException("Update port request canceled as there was an unexpected client error...", e);
        }
    }

    @Override
    public void updateTimeout(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);
        ResourceTranslator translator = new ResourceTranslator();
        String vsName;
        if (loadBalancer.hasSsl()) {
            vsName = ZxtmNameBuilder.genSslVSName(loadBalancer);
        } else {
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);
        }

        translator.translateLoadBalancerResource(config, vsName, loadBalancer);
        Pool pool = translator.getcPool();

        try {
            client.updatePool(vsName, pool);
        } catch (StingrayRestClientObjectNotFoundException onf) {
            LOG.error(String.format("Unable to update timeout as '%s' was not found", onf));
        } catch (StingrayRestClientException e) {
            LOG.error(String.format("Unable to update timeout due to unexpected client error '%s'", e));
        }

    }

    @Override
    public void setLoadBalancingAlgorithm(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, LoadBalancerAlgorithm algorithm) throws RemoteException, InsufficientRequestException, StmRollBackException {
        //TODO: still in example phase...
//
    }

    @Override
    public void changeHostForLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Host newHost) throws RemoteException, InsufficientRequestException, StmRollBackException {

    }


    @Override
    public void setNodeWeights(LoadBalancerEndpointConfiguration config,
                               Integer loadBalancerId, Integer accountId, Collection<Node> nodes)
            throws RemoteException, InsufficientRequestException, StmRollBackException {
    }


    private boolean persistenceIsSupported(LoadBalancer loadBalancer) {
        boolean supported = false;
        String type = loadBalancer.getSessionPersistence().getSessionPersistence().getPersistenceType().value();
        if (type.equals(StmConstants.HTTP_COOKIE) || type.equals(StmConstants.SOURCE_IP)) {
            supported = true;
        }
        return supported;

    }

    //Because le interface is annoying
    public void setSessionPersistence(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, SessionPersistence mode)
            throws RemoteException, InsufficientRequestException, StmRollBackException {
    }

    public void removeSessionPersistence(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId)
            throws RemoteException, InsufficientRequestException, StmRollBackException {

    }


    @Override
    public void setSessionPersistence(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws RemoteException, InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);
        ResourceTranslator translator = new ResourceTranslator();
        String vsName;
        if (!lb.hasSsl() && lb.getSessionPersistence() != null && persistenceIsSupported(lb)) {
            vsName = ZxtmNameBuilder.genVSName(lb);
            translator.translateLoadBalancerResource(config, vsName, lb);
            Persistence persistence = translator.getcPersistence();
            String persistenceType = persistence.getProperties().getBasic().getType();
            try {
                client.createPersistence(persistenceType, persistence);
            } catch (StingrayRestClientObjectNotFoundException onf) {
                LOG.info(String.format("Did not find session persistence %s.  This is expected...", vsName));
            } catch (StingrayRestClientException ex) {
                LOG.error(String.format("Error creating session persistence: %s, Rolling back! \n Exception: %s Trace: %s",
                        persistenceType, ex.getCause().getMessage(), Arrays.toString(ex.getCause().getStackTrace())));
            }
        }
    }


    @Override
    public void removeSessionPersistence(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, StmRollBackException {

        StingrayRestClient client = loadSTMRestClient(config);
        ResourceTranslator translator = new ResourceTranslator();
        String vsName;

        if (!loadBalancer.hasSsl() && loadBalancer.getSessionPersistence() != null && persistenceIsSupported(loadBalancer)) {
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);

            Persistence persistence = translator.translatePersistenceResource(vsName, loadBalancer);
            String persistenceType = persistence.getProperties().getBasic().getType();
            try {
                client.deletePersistence(persistenceType);
                LOG.info("Successfully deleted session persistence " + persistenceType);
            } catch (StingrayRestClientObjectNotFoundException onf) {
                LOG.warn(String.format("Cannot delete persistence %s as client does not exist", persistenceType));
            } catch (StingrayRestClientException ex) {
                LOG.error("Unexpected client error when deleting session persistence.");
            }
        }
    }


    @Override
    public void updateContentCaching(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);
        ResourceTranslator rt = new ResourceTranslator();
        String vsName = ZxtmNameBuilder.genVSName(loadBalancer);
        rt.translateLoadBalancerResource(config, vsName, loadBalancer);
        VirtualServer virtualServer = rt.getcVServer();

        LOG.info(String.format("Attempting to update content cacheing for %s", vsName));
        try {
            updateVirtualServer(config, client, vsName, virtualServer);
        } catch (StmRollBackException re) {
            LOG.error(String.format("Failed to update content cacheing for %s", vsName));
            throw re;
        }
    }

    @Override
    public void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws StmRollBackException, InsufficientRequestException {
        if (loadBalancer.getConnectionLimit() != null) {
            ResourceTranslator translator = new ResourceTranslator();
            StingrayRestClient client = loadSTMRestClient(config);
            String protectionName = ZxtmNameBuilder.genVSName(loadBalancer);
            LOG.info(String.format("Updating connection throttling on %s...", protectionName));
            updateProtection(config, client, loadBalancer, translator.translateProtectionResource(protectionName, loadBalancer));
            LOG.info("Successfully created protection " + protectionName);
        }
    }

    @Override
    public void updateAccessList(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, StmRollBackException {
        if (loadBalancer.getAccessLists() != null && !loadBalancer.getAccessLists().isEmpty()) {
            ResourceTranslator translator = new ResourceTranslator();
            StingrayRestClient client = loadSTMRestClient(config);
            String name = ZxtmNameBuilder.genVSName(loadBalancer);
            LOG.info(String.format("Updating Access List on '%s'...", name));
            updateProtection(config, client, loadBalancer, translator.translateProtectionResource(name, loadBalancer));
            LOG.info(String.format("Successfully updated Access List on '%s'...", name));
        }
    }

    public void updateProtection(LoadBalancerEndpointConfiguration config, StingrayRestClient client, LoadBalancer loadBalancer, Protection protection) throws InsufficientRequestException, StmRollBackException {
        String protectionName = ZxtmNameBuilder.genVSName(loadBalancer);
        String protectionSslName = ZxtmNameBuilder.genSslVSName(loadBalancer);
        LOG.debug(String.format("Updating protection class on '%s'...", protectionName));

        Protection curProtection = null;
        try {
            curProtection = client.getProtection(protectionName);
        } catch (Exception e) {
            LOG.warn(String.format("Could not load protection class: %s, attempting to recreating...", protectionName));
        }

        try {
            if (loadBalancer.hasSsl()) {
                LOG.debug(String.format("Updating protection for %s...", protectionSslName));
                client.updateProtection(protectionSslName, protection);
            }
            LOG.debug(String.format("Updating protection for %s...", protectionName));
            client.updateProtection(protectionName, protection);
        } catch (Exception ex) {
            String em = String.format("Error updating protection: %s Attempting to RollBack... \n Exception: %s Trace: %s"
                    , protectionName, ex.getCause().getMessage(), Arrays.toString(ex.getCause().getStackTrace()));

            LOG.error(em);
            if (curProtection != null) {
                LOG.debug(String.format("Updating monitor to previous configuration for rollback '%s'", protectionName));
                try {
                    client.updateProtection(protectionName, curProtection);
                } catch (Exception ex2) {
                    String em2 = String.format("Error updating protection while attempting to set previous configuration" +
                            ": %s RollBack aborted \n Exception: %s Trace: %s"
                            , protectionName, ex2.getCause().getMessage(), Arrays.toString(ex2.getCause().getStackTrace()));
                    LOG.error(em2);
                }
            } else {
                LOG.warn(String.format("Protection was not rolled back as no previous configuration was available. '%s' ", protectionName));
            }
            throw new StmRollBackException(em, ex);
        }
        if (loadBalancer.hasSsl()) {
            LOG.debug(String.format("Successfully updated protection for %s!", protectionSslName));
        }
        LOG.debug(String.format("Successfully updated protection for %s!", protectionName));
    }

    // Also added to the ZxtmAdapter... double check this behavior
    @Override
    public void deleteAccessList(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws RemoteException, InsufficientRequestException {
    }

    @Override
    public void deleteAccessList(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws InsufficientRequestException, StmRollBackException, StingrayRestClientObjectNotFoundException, StingrayRestClientException {
        if (loadBalancer.getAccessLists() == null || loadBalancer.getAccessLists().isEmpty()) {
            StingrayRestClient client = loadSTMRestClient(config);
            String protectionName = ZxtmNameBuilder.genVSName(loadBalancer);
            String protectionSslName = ZxtmNameBuilder.genSslVSName(loadBalancer);
            Protection curProtection = null;
            try {
                curProtection = client.getProtection(protectionName);
            } catch (Exception e) {
                LOG.warn(String.format("Could not load protection class: %s.  This is intended by the request.", protectionName));
            }
            try {
                if (loadBalancer.hasSsl()) {
                    client.deleteProtection(protectionSslName);
                }
                client.deleteProtection(protectionName);
            } catch (StingrayRestClientException e) {
                String em = String.format("Error deleting protection: %s Attempting to RollBack... \n Exception: %s Trace: %s"
                        , protectionName, e.getCause().getMessage(), Arrays.toString(e.getCause().getStackTrace()));
                if (curProtection != null) {
                    LOG.error(String.format("Unexpected client error when deleting protection %s: attempting to roll-back...", protectionName));
                    if (loadBalancer.hasSsl()) {
                        client.updateProtection(protectionSslName, curProtection);
                    }
                    client.updateProtection(protectionName, curProtection);
                    LOG.error(String.format("Successfully rolled back to previous configuration."));
                } else {
                    LOG.error(String.format("Protection %s not rolled back for lack of previous configuration.", protectionName));
                }
                throw new StmRollBackException(em, e);
            } catch (StingrayRestClientObjectNotFoundException onf) {
                LOG.info(String.format("No protection with name %s found...", protectionName));
            }
            if (loadBalancer.hasSsl()) {
                LOG.info(String.format("Successfully deleted protection %s!", protectionSslName));
            }
            LOG.info(String.format("Successfully deleted protection %s!", protectionName));
        }
    }

    @Override
    public void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, StmRollBackException, StingrayRestClientObjectNotFoundException, StingrayRestClientException {
        if (loadBalancer.getConnectionLimit() != null) {
            StingrayRestClient client = loadSTMRestClient(config);
            String protectionName = ZxtmNameBuilder.genVSName(loadBalancer);
            String protectionSslName = ZxtmNameBuilder.genSslVSName(loadBalancer);
            Protection curProtection = null;
            try {
                curProtection = client.getProtection(protectionName);
            } catch (Exception e) {
                LOG.warn(String.format("Could not load protection class: %s.  This is intended by the request.", protectionName));
            }
            try {
                if (loadBalancer.hasSsl()) {
                    client.deleteProtection(protectionSslName);
                }
                client.deleteProtection(protectionName);
            } catch (StingrayRestClientException e) {
                String em = String.format("Error deleting protection: %s Attempting to RollBack... \n Exception: %s Trace: %s"
                        , protectionName, e.getCause().getMessage(), Arrays.toString(e.getCause().getStackTrace()));
                if (curProtection != null) {
                    LOG.error(String.format("Unexpected client error when deleting protection %s: attempting to roll-back...", protectionName));
                    if (loadBalancer.hasSsl()) {
                        client.updateProtection(protectionSslName, curProtection);
                    }
                    client.updateProtection(protectionName, curProtection);
                    LOG.error(String.format("Successfully rolled back to previous configuration."));
                } else {
                    LOG.error(String.format("Protection %s not rolled back for lack of previous configuration.", protectionName));
                }
                throw new StmRollBackException(em, e);
            } catch (StingrayRestClientObjectNotFoundException onf) {
                LOG.error(String.format("No protection with name %s was found...", protectionName));
            }
            if (loadBalancer.hasSsl()) {
                LOG.info(String.format("Successfully deleted protection %s!", protectionSslName));
            }
            LOG.info(String.format("Successfully deleted protection %s!", protectionName));
        }
    }

    // TODO:  In progress.  Ignore for a while.
    @Override
    public void updateSslTermination(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, ZeusSslTermination sslTermination) throws RemoteException, InsufficientRequestException, StmRollBackException {
        /*

        Create Virtual Server
        Add Virtual Ips
        Follow HTTP header validation
            Check for Session Persistence
            Check for Health Monitoring
            Check for Connection Limits
            Check for Connection Logging
            Check for Content Caching
            Check for Access Lists
            Check for Half Closed
         */

        StingrayRestClient client = loadSTMRestClient(config);

        ResourceTranslator translator = new ResourceTranslator();
        String vsName = ZxtmNameBuilder.genSslVSName(loadBalancer);

        try {
            translator.translateLoadBalancerResource(config, vsName, loadBalancer);

//            if (loadBalancer.getSessionPersistence() != null
//                    && !loadBalancer.getSessionPersistence().equals(SessionPersistence.NONE)
//                    && !loadBalancer.hasSsl()) //setSessionPersistence(config, loadBalancer);
//
            if (loadBalancer.getHealthMonitor() != null && !loadBalancer.hasSsl()) {
                updateHealthMonitor(config, client, vsName, translator.getcMonitor());
            }

            if ((loadBalancer.getAccessLists() != null && !loadBalancer.getAccessLists().isEmpty())
                    || loadBalancer.getConnectionLimit() != null) {
                updateProtection(config, client, loadBalancer, translator.getcProtection());
            }


            if (loadBalancer.getProtocol().equals(LoadBalancerProtocol.HTTP)) {
                TrafficScriptHelper.addXForwardedForScriptIfNeeded(client);
                TrafficScriptHelper.addXForwardedProtoScriptIfNeeded(client);
            }

            updateVirtualIps(config, client, vsName, translator.getcTrafficIpGroups());
            updateNodePool(config, client, vsName, translator.getcPool());
            updateVirtualServer(config, client, vsName, translator.getcVServer());
        } catch (Exception ex) {
            LOG.error(ex);
            //TODO: roll back or handle as needed.. ...
            throw new StmRollBackException("Failed to update loadbalancer, rolling back...", ex);
        }
    }

    @Override
    public void removeSslTermination(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws RemoteException, InsufficientRequestException, StmRollBackException {

    }

    @Override
    public void enableDisableSslTermination(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, boolean isSslTermination) throws RemoteException, InsufficientRequestException, StmRollBackException {

    }

    // TODO: Remove me!!
    @Override
    public void setNodesPriorities(LoadBalancerEndpointConfiguration config, String poolName, LoadBalancer lb) throws RemoteException {

    }

    //TODO: Suspension is special case, where no attribute coincides with the enabling/disabling of the VS. Will need to enable/disable the VS by grabbing the resource and setting then updating...
    @Override
    public void suspendLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws RemoteException, InsufficientRequestException {

    }

    @Override
    public void removeSuspension(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws RemoteException, InsufficientRequestException {

    }


    @Override
    public boolean isEndPointWorking(LoadBalancerEndpointConfiguration config) throws RemoteException {
        return false;
    }

    @Override
    public void deleteRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, StmRollBackException {
        deleteRateLimit(config, loadBalancer, ZxtmNameBuilder.genVSName(loadBalancer));
        if (loadBalancer.hasSsl()) {
            deleteRateLimit(config, loadBalancer, ZxtmNameBuilder.genSslVSName(loadBalancer));
        }
    }

    private void deleteRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, String vsName) throws StmRollBackException, InsufficientRequestException {
        StingrayRestClient client = loadSTMRestClient(config);

        try {
            ResourceTranslator rt = new ResourceTranslator();
            rt.translateLoadBalancerResource(config, vsName, loadBalancer);
//            Bandwidth bandwidth = rt.getcBandwidth();
            VirtualServer virtualServer = rt.getcVServer();
            VirtualServerProperties properties = virtualServer.getProperties();
            VirtualServerBasic basic = properties.getBasic();

            basic.setBandwidth_class(null);
            client.deleteBandwidth(vsName);
            updateVirtualServer(config, client, vsName, virtualServer);

        } catch (StingrayRestClientObjectNotFoundException e) {
            LOG.warn(String.format("Failed to delete rate limit for virtual server '%s' -- Object not found", vsName));
            throw new StmRollBackException("Delete rate limit request canceled.", e);
        } catch (StingrayRestClientException e) {
            LOG.error(String.format("Failed to delete rate limit for virtual server %s -- REST Client exception", vsName));
            throw new StmRollBackException("Delete rate limit request canceled.", e);
        }

    }

    @Override
    public void setRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, StmRollBackException {
        setRateLimit(config, loadBalancer, ZxtmNameBuilder.genVSName(loadBalancer));
        if (loadBalancer.hasSsl()) {
            setRateLimit(config, loadBalancer, ZxtmNameBuilder.genSslVSName(loadBalancer));
        }
    }

    public void setRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, String vsName) throws RemoteException, InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);

        try {
            LOG.debug(String.format("Adding a rate limit to load balancer...'%s'...", vsName));


            ResourceTranslator rt = new ResourceTranslator();
            rt.translateLoadBalancerResource(config, vsName, loadBalancer);
            Bandwidth bandwidth = rt.getcBandwidth();
            VirtualServer virtualServer = rt.getcVServer();
            VirtualServerProperties properties = virtualServer.getProperties();
            VirtualServerBasic basic = properties.getBasic();
            basic.setBandwidth_class(vsName);

            client.createBandwidth(vsName, bandwidth);
            updateVirtualServer(config, client, vsName, virtualServer);

            LOG.info("Successfully added a rate limit to the rate limit pool.");

            //TODO: Not sure how to replace these calls, since I'm not sure what they do (yet)
            //TODO clean up needed as well
            //TrafficScriptHelper.addRateLimitScriptsIfNeeded(serviceStubs);
            //attachRateLimitRulesToVirtualServers(serviceStubs, new String[]{vsName});

        } catch (StingrayRestClientObjectNotFoundException e) {
            LOG.error(String.format("Failed to add rate limit for virtual server '%s' -- Object not found", vsName));
            throw new StmRollBackException("Add rate limit request canceled.", e);
        } catch (StingrayRestClientException e) {
            LOG.error(String.format("Failed to add rate limit for virtual server %s -- REST Client exception", vsName));
            throw new StmRollBackException("Add rate limit request canceled.", e);
        }
    }

    @Override
    public void updateRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, StmRollBackException {
        updateRateLimit(config, loadBalancer, ZxtmNameBuilder.genVSName(loadBalancer));
        if (loadBalancer.hasSsl()) {
            updateRateLimit(config, loadBalancer, ZxtmNameBuilder.genSslVSName(loadBalancer));
        }
    }

    public void updateRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, String vsName) throws InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);

        try {
            LOG.debug(String.format("Updating the rate limit for load balancer...'%s'...", vsName));

            ResourceTranslator rt = new ResourceTranslator();
            rt.translateLoadBalancerResource(config, vsName, loadBalancer);
            Bandwidth bandwidth = rt.getcBandwidth();
            VirtualServer virtualServer = rt.getcVServer();

            client.updateBandwidth(vsName, bandwidth);

            LOG.info(String.format("Successfully updated the rate limit for load balancer...'%s'...", vsName));

            //TODO: Not sure how to replace these calls, since I'm not sure what they do (yet)
            //TrafficScriptHelper.addRateLimitScriptsIfNeeded(serviceStubs);
            //attachRateLimitRulesToVirtualServers(serviceStubs, new String[]{vsName});

        } catch (StingrayRestClientObjectNotFoundException e) {
            LOG.error(String.format("Failed to update rate limit for virtual server %s -- REST Client exception", vsName));
            throw new StmRollBackException("Update rate limit request canceled.", e);
        } catch (StingrayRestClientException e) {
            LOG.error(String.format("Failed to update rate limit for virtual server %s -- REST Client exception", vsName));
            throw new StmRollBackException("Update rate limit request canceled.", e);
        }

    }

    @Override
    public void removeAndSetDefaultErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, StmRollBackException {
        deleteErrorFile(config, loadBalancer);
        //setDefaultErrorFile(config, loadBalancer); //This isn't necessary anymore, since deleteErrorFile already ran an update
    }

    @Override
    public void setDefaultErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, RemoteException, StmRollBackException {
        setDefaultErrorFile(config, loadBalancer, ZxtmNameBuilder.genVSName(loadBalancer));
        if (loadBalancer.hasSsl()) {
            setDefaultErrorFile(config, loadBalancer, ZxtmNameBuilder.genSslVSName(loadBalancer));
        }
    }

    public void setDefaultErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, String vsName)
            throws InsufficientRequestException, RemoteException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);

        ResourceTranslator rt = new ResourceTranslator();
        rt.translateVirtualServerResource(config, vsName, loadBalancer);
        VirtualServer vs = rt.getcVServer();
        LOG.debug(String.format("Attempting to set the default error file for %s", vsName));
        try {
            // Update client with new properties
            updateVirtualServer(config, client, vsName, vs);

            LOG.info(String.format("Successfully set the default error file for: %s", vsName));
        } catch (StmRollBackException re) {
            LOG.error(String.format("Failed to set the default error file for: %s", vsName));
            throw re;
        }
    }

    @Override
    public void uploadDefaultErrorFile(LoadBalancerEndpointConfiguration config, String content)
            throws RemoteException, InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);

        LOG.debug("Attempting to upload the default error file...");
        try {
            client.createExtraFile(Constants.DEFAULT_ERRORFILE, getFileWithContent(content));
            LOG.info("Successfully uploaded the default error file...");
        } catch (IOException e) {
            LOG.error(String.format("Failed to upload default ErrorFile for %s -- IO exception", config.getEndpointUrl()));
        } catch (StingrayRestClientException ce) {
            LOG.error(String.format("Failed to upload default ErrorFile for %s -- REST Client exception", config.getEndpointUrl()));
        } catch (StingrayRestClientObjectNotFoundException onf) {
            LOG.error(String.format("Failed to upload default ErrorFile for %s -- Object not found", config.getEndpointUrl()));
        }
    }

    @Override
    public void deleteErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, StmRollBackException {

        deleteErrorFile(config, loadBalancer, ZxtmNameBuilder.genVSName(loadBalancer));
        if (loadBalancer.hasSsl()) {
            deleteErrorFile(config, loadBalancer, ZxtmNameBuilder.genSslVSName(loadBalancer));
        }
    }

    public void deleteErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, String vsName)
            throws InsufficientRequestException, StmRollBackException {

        StingrayRestClient client = loadSTMRestClient(config);

        ResourceTranslator rt = new ResourceTranslator();
        rt.translateVirtualServerResource(config, vsName, loadBalancer);
        VirtualServer vs = rt.getcVServer();
        String fileToDelete = getErrorFileName(vsName);
        try {
            LOG.debug(String.format("Attempting to delete a custom error file for %s (%s)", vsName, fileToDelete));

            // Update client with new properties
            VirtualServerProperties properties = vs.getProperties();
            properties.setConnection_errors(new VirtualServerConnectionError()); // this will set the default error page
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
    public void setErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, String content) throws RemoteException, InsufficientRequestException, StmRollBackException {

        setErrorFile(config, loadBalancer, ZxtmNameBuilder.genVSName(loadBalancer), content);
        if (loadBalancer.hasSsl()) {
            setErrorFile(config, loadBalancer, ZxtmNameBuilder.genSslVSName(loadBalancer), content);
        }
    }

    public void setErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, String vsName, String content) throws RemoteException, InsufficientRequestException, StmRollBackException {
        StingrayRestClient client = loadSTMRestClient(config);

        ResourceTranslator rt = new ResourceTranslator();
        rt.translateVirtualServerResource(config, vsName, loadBalancer);
        VirtualServer vs = rt.getcVServer();
        String errorFileName = getErrorFileName(vsName);
        try {
            LOG.debug(String.format("Attempting to upload the error file for %s (%s)", vsName, errorFileName));
            client.createExtraFile(errorFileName, getFileWithContent(content));
            LOG.info(String.format("Successfully uploaded the error file for %s (%s)", vsName, errorFileName));
        } catch (IOException ioe) {
            // Failed to create file, use "Default"
            LOG.error(String.format("Failed to set ErrorFile for %s (%s) -- IO exception", vsName, errorFileName));
            errorFileName = "Default";
        } catch (StingrayRestClientException ce) {
            // Failed to upload file, use "Default"
            LOG.error(String.format("Failed to set ErrorFile for %s (%s) -- REST Client exception", vsName, errorFileName));
            errorFileName = "Default";
        } catch (StingrayRestClientObjectNotFoundException onf) {
            // Failed to create file, use "Default"
            LOG.error(String.format("Failed to set ErrorFile for %s (%s) -- Object not found", vsName, errorFileName));
            errorFileName = "Default";
        }

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

    private String getErrorFileName(String vsName) {
        return String.format("%s_error.html", vsName);
    }

    private File getFileWithContent(String content) throws IOException {
        File file = File.createTempFile("StmAdapterImpl_", ".err");
        file.deleteOnExit();
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write(content);
        out.close();
        return file;
    }


    /**
     * Deprecating these(SubnetMapping calls) as per ops. Unused call that is difficult to test, may support in future if needed... *
     */

    @Override
    public void setSubnetMappings(LoadBalancerEndpointConfiguration config, Hostssubnet hostssubnet) throws RemoteException {
        StingrayRestClient client;
        try {
            client = loadSTMRestClient(config);
            List<Hostsubnet> subnetList = hostssubnet.getHostsubnets();

            //Loop over Hosts ("dev1.lbaas.mysite.com", "dev2.lbaas.mysite.com", etc)
            for (Hostsubnet hostsubnet : subnetList) {
                String hsName = hostsubnet.getName();
                TrafficManager trafficManager = client.getTrafficManager(hsName);
                List<TrafficManagerTrafficIp> trafficManagerTrafficIpList = new ArrayList<TrafficManagerTrafficIp>();
                List<NetInterface> interfaceList = hostsubnet.getNetInterfaces();

                //Loop over interfaces (eth0, eth1, etc)
                for (NetInterface netInterface : interfaceList) {
                    List<Cidr> cidrList = netInterface.getCidrs();
                    TrafficManagerTrafficIp trafficManagerTrafficIp = new TrafficManagerTrafficIp();
                    Set<String> networkList = new HashSet<String>();

                    // Loop over Cidr list which contains one subnet per Cidr
                    for (Cidr cidr : cidrList) {
                        networkList.add(cidr.getBlock());
                    }

                    trafficManagerTrafficIp.setName(netInterface.getName());
                    trafficManagerTrafficIp.setNetworks(networkList);
                    trafficManagerTrafficIpList.add(trafficManagerTrafficIp);
                }
                trafficManager.getProperties().getBasic().setTrafficip(trafficManagerTrafficIpList);
                client.updateTrafficManager(hsName, trafficManager);
            }
        } catch (StmRollBackException e) {
            e.printStackTrace();
        } catch (StingrayRestClientObjectNotFoundException e) {
            e.printStackTrace();
        } catch (StingrayRestClientException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteSubnetMappings(LoadBalancerEndpointConfiguration config, Hostssubnet hostssubnet) throws RemoteException {
        StingrayRestClient client;
        try {
            client = loadSTMRestClient(config);
            List<Hostsubnet> subnetList = hostssubnet.getHostsubnets();

            //Loop over Hosts ("dev1.lbaas.mysite.com", "dev2.lbaas.mysite.com", etc)
            for (Hostsubnet hostsubnet : subnetList) {
                String hsName = hostsubnet.getName();       // This name is of the form "dev1.lbaas.mysite.com"
                TrafficManager trafficManager = client.getTrafficManager(hsName);
                List<NetInterface> netInterfaceList = hostsubnet.getNetInterfaces();
                //trafficManagerTrafficIpList is the current list of TrafficIPs for the host
                List<TrafficManagerTrafficIp> trafficManagerTrafficIpList = trafficManager.getProperties().getBasic().getTrafficip();
                Map<String, TrafficManagerTrafficIp> tipsMap = new HashMap<String, TrafficManagerTrafficIp>();

                //Loop over tips to compile an indexed list by name
                for (TrafficManagerTrafficIp trafficManagerTrafficIp : trafficManagerTrafficIpList) {
                    tipsMap.put(trafficManagerTrafficIp.getName(), trafficManagerTrafficIp);
                }

                //Loop over interfaces (eth0, eth1, etc)
                for (NetInterface netInterface : netInterfaceList) {
                    String netInterfaceName = netInterface.getName(); //This name is of the form "eth0"

                    if (tipsMap.containsKey(netInterfaceName)) {
                        TrafficManagerTrafficIp tip = tipsMap.get(netInterfaceName);
                        Set<String> networkSet = tip.getNetworks();
                        List<Cidr> cidrList = netInterface.getCidrs(); //This is the list of objects containing subnet strings

                        // Loop over Cidr list which contains one subnet per Cidr
                        for (Cidr cidr : cidrList) {
                            networkSet.remove(cidr.getBlock()); //Remove the subnet if it exists
                        }
                    }
                }
                client.updateTrafficManager(hsName, trafficManager);
            }
        } catch (StmRollBackException e) {
            e.printStackTrace();
        } catch (StingrayRestClientObjectNotFoundException e) {
            e.printStackTrace();
        } catch (StingrayRestClientException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Hostssubnet getSubnetMappings(LoadBalancerEndpointConfiguration config, String host) throws RemoteException {
        StingrayRestClient client;
        Hostssubnet ret = new Hostssubnet();
        try {
            client = loadSTMRestClient(config);
            TrafficManager trafficManager = client.getTrafficManager(host);
            //trafficManagerTrafficIpList is the current list of TrafficIPs for the host
            List<TrafficManagerTrafficIp> trafficManagerTrafficIpList = trafficManager.getProperties().getBasic().getTrafficip();
            List<Hostsubnet> subnetList = new ArrayList<Hostsubnet>();
            Hostsubnet hostsubnet = new Hostsubnet();
            hostsubnet.setName(host);

            //Loop over trafficIPs (== interfaces) (eth0, eth1, etc)
            for (TrafficManagerTrafficIp trafficManagerTrafficIp : trafficManagerTrafficIpList) {
                Set<String> networkSet = trafficManagerTrafficIp.getNetworks();
                NetInterface netInterface = new NetInterface();
                List<Cidr> cidrs = new ArrayList<Cidr>();

                //Loop over networks (== cidr blocks)
                for (String block : networkSet) {
                    Cidr cidr = new Cidr();
                    cidr.setBlock(block);
                    cidrs.add(cidr);
                }

                netInterface.setName(trafficManagerTrafficIp.getName());
                netInterface.setCidrs(cidrs);
                hostsubnet.getNetInterfaces().add(netInterface);
            }
            subnetList.add(hostsubnet);
            ret.setHostsubnets(subnetList);
        } catch (StingrayRestClientObjectNotFoundException e) {
            e.printStackTrace();
        } catch (StmRollBackException e) {
            e.printStackTrace();
        } catch (StingrayRestClientException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * TODO: for the unsupported methods in rest we will probably have to still use soap :(
     */


    //Unsupported in STM Rest
    @Override
    public List<String> getStatsSystemLoadBalancerNames(LoadBalancerEndpointConfiguration config) throws RemoteException {
        return null;
    }

    //Unsupported in STM Rest
    @Override
    public Map<String, Integer> getLoadBalancerCurrentConnections(LoadBalancerEndpointConfiguration config, List<String> names) throws RemoteException {
        return null;
    }

    //Unsupported in STM Rest
    @Override
    public Integer getLoadBalancerCurrentConnections(LoadBalancerEndpointConfiguration config, Integer accountId, Integer loadBalancerId, boolean isSsl) throws RemoteException, InsufficientRequestException {
        return null;
    }

    //Unsupported in STM Rest
    @Override
    public int getTotalCurrentConnectionsForHost(LoadBalancerEndpointConfiguration config) throws RemoteException {
        return 0;
    }

    //Unsupported in STM Rest
    @Override
    public Stats getLoadBalancerStats(LoadBalancerEndpointConfiguration config, Integer loadbalancerId, Integer accountId) throws RemoteException, InsufficientRequestException {
        return null;
    }

    //Unsupported in STM Rest
    @Override
    public Map<String, Long> getLoadBalancerBytesIn(LoadBalancerEndpointConfiguration config, List<String> names) throws RemoteException {
        return null;
    }

    //Unsupported in STM Rest
    @Override
    public Long getLoadBalancerBytesIn(LoadBalancerEndpointConfiguration config, Integer accountId, Integer loadBalancerId, boolean isSsl) throws RemoteException, InsufficientRequestException {
        return null;
    }

    //Unsupported in STM Rest
    @Override
    public Map<String, Long> getLoadBalancerBytesOut(LoadBalancerEndpointConfiguration config, List<String> names) throws RemoteException {
        return null;
    }

    //Unsupported in STM Rest
    @Override
    public Long getLoadBalancerBytesOut(LoadBalancerEndpointConfiguration config, Integer accountId, Integer loadBalancerId, boolean isSsl) throws RemoteException, InsufficientRequestException {
        return null;
    }

    //Unsupported in STM Rest
    @Override
    public Long getHostBytesIn(LoadBalancerEndpointConfiguration config) throws RemoteException {
        return null;
    }

    //Unsupported in STM Rest
    @Override
    public Long getHostBytesOut(LoadBalancerEndpointConfiguration config) throws RemoteException {
        return null;
    }

    //Unsupported in STM Rest
    @Override
    public void createHostBackup(LoadBalancerEndpointConfiguration config, String backupName) throws RemoteException {

    }

    //Unsupported in STM Rest
    @Override
    public void deleteHostBackup(LoadBalancerEndpointConfiguration config, String backupName) throws RemoteException {

    }

    //Unsupported in STM Rest
    @Override
    public void restoreHostBackup(LoadBalancerEndpointConfiguration config, String backupName) throws RemoteException {

    }
}
