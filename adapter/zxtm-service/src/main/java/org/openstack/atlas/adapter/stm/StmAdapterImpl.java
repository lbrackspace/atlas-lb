package org.openstack.atlas.adapter.stm;

import com.zxtm.service.client.InvalidInput;
import com.zxtm.service.client.ObjectDoesNotExist;
import com.zxtm.service.client.PoolLoadBalancingAlgorithm;
import com.zxtm.service.client.PoolWeightingsDefinition;
import org.apache.axis.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.ZxtmRollBackException;
import org.openstack.atlas.adapter.helpers.IpHelper;
import org.openstack.atlas.adapter.helpers.NodeHelper;
import org.openstack.atlas.adapter.helpers.TrafficScriptHelper;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.adapter.zxtm.ZxtmServiceStubs;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.pojos.Stats;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.pool.PoolBasic;
import org.rackspace.stingray.client.pool.PoolProperties;
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

public class StmAdapterImpl implements ReverseProxyLoadBalancerAdapter {
    public static Log LOG = LogFactory.getLog(StmAdapterImpl.class.getName());

    //TODO: move to a 'constants' file...
    public static final LoadBalancerAlgorithm DEFAULT_ALGORITHM = LoadBalancerAlgorithm.RANDOM;
    public static final String XFF = "add_x_forwarded_for_header";
    public static final String XFP = "add_x_forwarded_proto";


    public StingrayRestClient getStingrayClient(LoadBalancerEndpointConfiguration config) {
        StingrayRestClient client = null;
        try {
            client = new StingrayRestClient(new URI(config.getEndpointUrl().toString()));
        } catch (URISyntaxException e) {
            LOG.error(String.format("Configuration error, verify soapendpoint is valid! Exception %s", e));
        }
        return client;
    }

    public StingrayRestClient getStingrayClient() {
        return new StingrayRestClient();
    }


    @Override public ZxtmServiceStubs getServiceStubs(LoadBalancerEndpointConfiguration config) throws AxisFault {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void createLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        final String virtualServerName = ZxtmNameBuilder.genVSName(loadBalancer);
        StingrayRestClient client = getStingrayClient(config);

        VirtualServer vs = new VirtualServer();
        VirtualServerProperties vsp = new VirtualServerProperties();
        VirtualServerBasic vsb = new VirtualServerBasic();

        try {
            //JUST for example for the time being......
            createNodePool(config, loadBalancer.getId(), loadBalancer.getAccountId(), loadBalancer.getNodes(), retrieveAlgorithm(loadBalancer));
            vsb.setPool(virtualServerName);

            //...

            //Added rules for HTTP LB
            if (loadBalancer.getProtocol().equals(LoadBalancerProtocol.HTTP)) {
                TrafficScriptHelper.addXForwardedForScriptIfNeeded(client);
                TrafficScriptHelper.addXForwardedProtoScriptIfNeeded(client);
                List<String> rules = Arrays.asList(XFF, XFP);
                vsb.setRequest_rules(rules);

//                setDefaultErrorFile(config, lb);
            }

            //...
            vsp.setBasic(vsb);
            vs.setProperties(vsp);
            //...
            client.createVirtualServer(virtualServerName, vs);
        } catch (Exception ex) {
            //TODO: roll back...
        }
        //Finish...
    }

    private LoadBalancerAlgorithm retrieveAlgorithm(LoadBalancer loadBalancer) {
        return loadBalancer.getAlgorithm() == null ? DEFAULT_ALGORITHM : loadBalancer.getAlgorithm();
    }

    private Pool createNodePool(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Collection<Node> allNodes, LoadBalancerAlgorithm algorithm) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        final String poolName = ZxtmNameBuilder.genVSName(loadBalancerId, accountId);
        StingrayRestClient client = getStingrayClient(config);

        LOG.debug(String.format("Creating pool '%s' and setting nodes...", poolName));
//        serviceStubs.getPoolBinding().addPool(new String[]{poolName}, NodeHelper.getIpAddressesFromNodes(allNodes));

        Pool cpool = null;
        Pool p = null;
        try {
            cpool = client.getPool(poolName);


            //Should still be null since we are ccreating new pool..
            p = new Pool();
            PoolProperties pp = new PoolProperties();
            PoolBasic pb = new PoolBasic();
            Set<String> nodes = new HashSet<String>();
            nodes.add("10.12.4.4");
            pb.setNodes(nodes);
            pp.setBasic(pb);
            p.setProperties(pp);

            cpool = client.createPool(poolName, p);
        } catch (StingrayRestClientException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (StingrayRestClientObjectNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        LOG.debug("CREATED POOL! ::" + p);
        LOG.debug("CREATED POOL! stringed ::" + p.toString());

        //SET algo on pool object..
//        setLoadBalancingAlgorithm(config, loadBalancerId, accountId, algorithm);

//        setDisabledNodes(config, poolName, getNodesWithCondition(allNodes, NodeCondition.DISABLED));
//        setDrainingNodes(config, poolName, getNodesWithCondition(allNodes, NodeCondition.DRAINING));
//        setNodeWeights(config, loadBalancerId, accountId, allNodes);
        //This is set on the POOL object...
//        serviceStubs.getPoolBinding().setPassiveMonitoring(new String[]{poolName}, new boolean[]{false});
        return cpool;
    }

    private List<Node> getNodesWithCondition(Collection<Node> nodes, NodeCondition nodeCondition) {
        List<Node> nodesWithCondition = new ArrayList<Node>();
        for (Node node : nodes) {
            if (node.getCondition().equals(nodeCondition)) {
                nodesWithCondition.add(node);
            }
        }
        return nodesWithCondition;
    }

    private void setDisabledNodes(LoadBalancerEndpointConfiguration config, String poolName, List<Node> nodesToDisable) throws RemoteException {
        if (nodesToDisable == null || nodesToDisable.isEmpty()) {
            return;
        }

        ZxtmServiceStubs serviceStubs = getServiceStubs(config);

        LOG.debug(String.format("Setting disabled nodes for pool '%s'", poolName));
        serviceStubs.getPoolBinding().setDisabledNodes(new String[]{poolName}, NodeHelper.getIpAddressesFromNodes(nodesToDisable));
        LOG.debug(String.format("Successfully set disabled nodes for pool '%s'", poolName));
    }

    private void setDrainingNodes(LoadBalancerEndpointConfiguration config, String poolName, List<Node> nodesToDrain) throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        String[][] currentDrainingNodes = serviceStubs.getPoolBinding().getDrainingNodes(new String[]{poolName});

        if (nodesToDrain != null && !nodesToDrain.isEmpty()) {
            LOG.debug(String.format("Setting draining nodes for pool '%s'", poolName));
            serviceStubs.getPoolBinding().setDrainingNodes(new String[]{poolName}, NodeHelper.getIpAddressesFromNodes(nodesToDrain));
            LOG.debug(String.format("Successfully set draining nodes for pool '%s'", poolName));
        } else if (currentDrainingNodes[0].length > 0) {
            LOG.debug(String.format("Removing draining nodes for pool '%s'", poolName));
            serviceStubs.getPoolBinding().removeDrainingNodes(new String[]{poolName}, currentDrainingNodes);
            LOG.debug(String.format("Successfully removed draining nodes for pool '%s'", poolName));
        }
    }

    @Override public void deleteLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void updateProtocol(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void updateHalfClosed(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void updatePort(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Integer port) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void updateTimeout(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void setLoadBalancingAlgorithm(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, LoadBalancerAlgorithm algorithm) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //TODO: still in example phase...
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.genVSName(loadBalancerId, accountId);

        try {
            LOG.debug(String.format("Setting load balancing algorithm to '%s' for node pool '%s'...", algorithm.name(), poolName));
            //TODO: this will be its own call still, but for create pool this can be done in that method, but still want this ...
//            serviceStubs.getPoolBinding().setLoadBalancingAlgorithm(new String[]{poolName}, new PoolLoadBalancingAlgorithm[]{ZxtmConversionUtils.mapAlgorithm(algorithm)});
            LOG.info(String.format("Load balancing algorithm successfully set for node pool '%s'...", poolName));
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Cannot update algorithm for node pool '%s' as it does not exist.", poolName), e);
            }
            throw new ZxtmRollBackException("Update algorithm request canceled.", e);
        }
    }

    @Override public void addVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void deleteVirtualIp(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Integer vipId) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void deleteVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, List<Integer> vipId) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void changeHostForLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Host newHost) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void setNodes(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void removeNodes(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Collection<Node> nodes) throws AxisFault, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void removeNode(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, String ipAddress, Integer port) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void setNodeWeights(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Collection<Node> nodes) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        final String poolName = ZxtmNameBuilder.genVSName(loadBalancerId, accountId);
        final String rollBackMessage = "Update node weights request canceled.";

        try {
            final PoolLoadBalancingAlgorithm[] loadBalancingAlgorithm = serviceStubs.getPoolBinding().getLoadBalancingAlgorithm(new String[]{poolName});
            if (loadBalancingAlgorithm[0].equals(PoolLoadBalancingAlgorithm.wroundrobin) || loadBalancingAlgorithm[0].equals(PoolLoadBalancingAlgorithm.wconnections)) {
                LOG.debug(String.format("Setting node weights for pool '%s'...", poolName));
                serviceStubs.getPoolBinding().setNodesWeightings(new String[]{poolName}, buildPoolWeightingsDefinition(nodes));
                LOG.info(String.format("Node weights successfully set for pool '%s'.", poolName));
            }
        } catch (Exception e) {
            if (e instanceof ObjectDoesNotExist) {
                LOG.error(String.format("Node pool '%s' does not exist. Cannot update node weights", poolName), e);
            }
            if (e instanceof InvalidInput) {
                LOG.error(String.format("Node weights are out of range for node pool '%s'. Cannot update node weights", poolName), e);
            }
            throw new ZxtmRollBackException(rollBackMessage, e);
        }
    }

    private PoolWeightingsDefinition[][] buildPoolWeightingsDefinition(Collection<Node> nodes) {
        final PoolWeightingsDefinition[][] poolWeightings = new PoolWeightingsDefinition[1][nodes.size()];
        final Integer DEFAULT_NODE_WEIGHT = 1;

        int i = 0;
        for (Node node : nodes) {
            Integer nodeWeight = node.getWeight() == null ? DEFAULT_NODE_WEIGHT : node.getWeight();
            poolWeightings[0][i] = new PoolWeightingsDefinition(IpHelper.createZeusIpString(node.getIpAddress(), node.getPort()), nodeWeight);
            i++;
        }

        return poolWeightings;
    }

    @Override public void setSessionPersistence(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, SessionPersistence mode) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void removeSessionPersistence(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void updateConnectionLogging(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void updateContentCaching(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void updateHealthMonitor(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, HealthMonitor healthMonitor) throws RemoteException, InsufficientRequestException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void removeHealthMonitor(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void updateAccessList(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void deleteAccessList(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws RemoteException, InsufficientRequestException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void suspendLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws RemoteException, InsufficientRequestException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void removeSuspension(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws RemoteException, InsufficientRequestException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void createHostBackup(LoadBalancerEndpointConfiguration config, String backupName) throws RemoteException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void deleteHostBackup(LoadBalancerEndpointConfiguration config, String backupName) throws RemoteException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void restoreHostBackup(LoadBalancerEndpointConfiguration config, String backupName) throws RemoteException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void setSubnetMappings(LoadBalancerEndpointConfiguration config, Hostssubnet hostssubnet) throws RemoteException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void deleteSubnetMappings(LoadBalancerEndpointConfiguration config, Hostssubnet hostssubnet) throws RemoteException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public Hostssubnet getSubnetMappings(LoadBalancerEndpointConfiguration config, String host) throws RemoteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public List<String> getStatsSystemLoadBalancerNames(LoadBalancerEndpointConfiguration config) throws RemoteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public Map<String, Integer> getLoadBalancerCurrentConnections(LoadBalancerEndpointConfiguration config, List<String> names) throws RemoteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public Integer getLoadBalancerCurrentConnections(LoadBalancerEndpointConfiguration config, Integer accountId, Integer loadBalancerId, boolean isSsl) throws RemoteException, InsufficientRequestException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public int getTotalCurrentConnectionsForHost(LoadBalancerEndpointConfiguration config) throws RemoteException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public Stats getLoadBalancerStats(LoadBalancerEndpointConfiguration config, Integer loadbalancerId, Integer accountId) throws RemoteException, InsufficientRequestException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public Map<String, Long> getLoadBalancerBytesIn(LoadBalancerEndpointConfiguration config, List<String> names) throws RemoteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public Long getLoadBalancerBytesIn(LoadBalancerEndpointConfiguration config, Integer accountId, Integer loadBalancerId, boolean isSsl) throws RemoteException, InsufficientRequestException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public Map<String, Long> getLoadBalancerBytesOut(LoadBalancerEndpointConfiguration config, List<String> names) throws RemoteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public Long getLoadBalancerBytesOut(LoadBalancerEndpointConfiguration config, Integer accountId, Integer loadBalancerId, boolean isSsl) throws RemoteException, InsufficientRequestException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public Long getHostBytesIn(LoadBalancerEndpointConfiguration config) throws RemoteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public Long getHostBytesOut(LoadBalancerEndpointConfiguration config) throws RemoteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public boolean isEndPointWorking(LoadBalancerEndpointConfiguration config) throws RemoteException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void deleteRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void setRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void updateRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void removeAndSetDefaultErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void setDefaultErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void uploadDefaultErrorFile(LoadBalancerEndpointConfiguration config, String content) throws RemoteException, InsufficientRequestException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void deleteErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws AxisFault, InsufficientRequestException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void setErrorFile(LoadBalancerEndpointConfiguration conf, LoadBalancer loadBalancer, String content) throws RemoteException, InsufficientRequestException {
        setErrorFile(conf, ZxtmNameBuilder.genVSName(loadBalancer), content);
        if (loadBalancer.hasSsl()) {
            setErrorFile(conf, ZxtmNameBuilder.genSslVSName(loadBalancer), content);
        }
    }

    public void setErrorFile(LoadBalancerEndpointConfiguration conf, String vsName, String content) throws RemoteException {
        // ** START Temporary for testing purposes
        StingrayRestClient client = null;
        if (conf == null)
            client = getStingrayClient();
        else
            client = getStingrayClient(conf);
        // ** END Temporary for testing purposes

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

            // Get server configs
            VirtualServer vs = client.getVirtualServer(vsName);
            VirtualServerProperties vsp = vs.getProperties();
            VirtualServerConnectionError ce = vsp.getConnection_errors();

            // Set error file and propagate upwards
            ce.setError_file(errorFileName);
            vsp.setConnection_errors(ce);
            vs.setProperties(vsp);

            // Update client with new properties
            client.updateVirtualServer(vsName, vs);

            LOG.info(String.format("Successfully set the error file for %s (%s)", vsName, errorFileName));
        } catch (StingrayRestClientException ce) {
            // REST failure...
            LOG.error(String.format("Failed to set ErrorFile for %s (%s) -- REST Client exception", vsName, errorFileName));
        } catch (StingrayRestClientObjectNotFoundException onf) {
            // The file we uploaded wasn't there? Not good -- leave the object as it was before?
            LOG.error(String.format("Failed to set ErrorFile for %s (%s) -- Object not found", vsName, errorFileName));
        }
    }

    private String getErrorFileName(String vsName) {
        String msg = String.format("%s_error.html", vsName);
        return msg;
    }

    private File getFileWithContent(String content) throws IOException {
        File file = File.createTempFile("StmAdapterImpl_",".err");
        file.deleteOnExit();
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write(content);
        out.close();
        return file;
    }

    @Override public void updateSslTermination(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, ZeusSslTermination sslTermination) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void removeSslTermination(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void enableDisableSslTermination(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, boolean isSslTermination) throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public void setNodesPriorities(LoadBalancerEndpointConfiguration config, String poolName, LoadBalancer lb) throws RemoteException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
