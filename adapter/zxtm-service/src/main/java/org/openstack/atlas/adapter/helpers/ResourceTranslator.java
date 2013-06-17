package org.openstack.atlas.adapter.helpers;

import com.zxtm.service.client.PoolLoadBalancingAlgorithm;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.stm.StmAdapterImpl;
import org.openstack.atlas.service.domain.entities.*;
import org.rackspace.stingray.client.monitor.Monitor;
import org.rackspace.stingray.client.monitor.MonitorBasic;
import org.rackspace.stingray.client.monitor.MonitorProperties;
import org.rackspace.stingray.client.persistence.Persistence;
import org.rackspace.stingray.client.persistence.PersistenceBasic;
import org.rackspace.stingray.client.persistence.PersistenceProperties;
import org.rackspace.stingray.client.pool.*;
import org.rackspace.stingray.client.protection.Protection;
import org.rackspace.stingray.client.protection.ProtectionBasic;
import org.rackspace.stingray.client.protection.ProtectionProperties;
import org.rackspace.stingray.client.virtualserver.VirtualServer;
import org.rackspace.stingray.client.virtualserver.VirtualServerBasic;
import org.rackspace.stingray.client.virtualserver.VirtualServerConnectionError;
import org.rackspace.stingray.client.virtualserver.VirtualServerProperties;

import java.util.*;

public class ResourceTranslator {
    public Pool cPool;
    public Monitor cMonitor;
    public VirtualServer cVServer;
    public Protection cProtection;
    public Persistence cPersistence;


    public VirtualServer translateLoadBalancerResource(LoadBalancer loadBalancer) throws InsufficientRequestException {
        final String virtualServerName = ZxtmNameBuilder.genVSName(loadBalancer);

        VirtualServer virtualServer = new VirtualServer();
        VirtualServerBasic basic = new VirtualServerBasic();
        VirtualServerProperties properties = new VirtualServerProperties();
        VirtualServerConnectionError ce = new VirtualServerConnectionError();

        translatePoolResource(loadBalancer);
        basic.setPool(virtualServerName);

        translateProtectionResource(loadBalancer);
        basic.setProtection_class(virtualServerName);

        ce.setError_file(loadBalancer.getUserPages().getErrorpage());
        properties.setConnection_errors(ce);

        List<String> rules = Arrays.asList(StmAdapterImpl.XFF, StmAdapterImpl.XFP);
        basic.setRequest_rules(rules);

        properties.setBasic(basic);
        virtualServer.setProperties(properties);

        cVServer = virtualServer;
        return null;
    }

    public Pool translatePoolResource(LoadBalancer loadBalancer) throws InsufficientRequestException {
        final String virtualServerName = ZxtmNameBuilder.genVSName(loadBalancer);
        Set<Node> nodes = loadBalancer.getNodes();

        Pool pool = new Pool();
        PoolProperties properties = new PoolProperties();
        PoolBasic basic = new PoolBasic();
        List<PoolNodeWeight> weights = new ArrayList<PoolNodeWeight>();
        PoolLoadbalancing poollb = new PoolLoadbalancing();
        PoolConnection connection = new PoolConnection();


        if (loadBalancer.getAlgorithm().name().equals(PoolLoadBalancingAlgorithm.wroundrobin.getValue())
                || loadBalancer.getAlgorithm().name().equals(PoolLoadBalancingAlgorithm.wconnections.getValue())) {
            PoolNodeWeight nw;
            for (Node n : nodes) {
                nw = new PoolNodeWeight();
                nw.setNode(n.getIpAddress());
                nw.setWeight(n.getWeight());
                weights.add(nw);
            }
            poollb.setNode_weighting(weights);
        }

        ZeusNodePriorityContainer znpc = new ZeusNodePriorityContainer(loadBalancer.getNodes());
        poollb.setPriority_enabled(znpc.hasSecondary());
        poollb.setPriority_values(znpc.getPriorityValuesSet());
        poollb.setAlgorithm(loadBalancer.getAlgorithm().name());

        basic.setDraining(getNodesWithCondition(nodes, NodeCondition.DRAINING));
        basic.setDisabled(getNodesWithCondition(nodes, NodeCondition.DISABLED));
        basic.setPassive_monitoring(false);

        connection.setMax_reply_time(loadBalancer.getTimeout());

        translateMonitorResource(loadBalancer.getHealthMonitor());
        basic.setMonitors(new HashSet<String>(Arrays.asList(virtualServerName)));

        properties.setBasic(basic);
        properties.setLoad_balancing(poollb);
        properties.setConnection(connection);
        pool.setProperties(properties);


        cPool = pool;
        return pool;
    }

    public Monitor translateMonitorResource(HealthMonitor healthMonitor) {
        Monitor monitor = new Monitor();
        MonitorProperties properties = new MonitorProperties();
        MonitorBasic basic = new MonitorBasic();

        properties.setBasic(basic);
        monitor.setProperties(properties);

        cMonitor = monitor;
        return null;
    }

    public Protection translateProtectionResource(LoadBalancer loadBalancer) {
        Protection protection = new Protection();
        ProtectionBasic basic = new ProtectionBasic();
        ProtectionProperties properties = new ProtectionProperties();

        ConnectionLimit limits = loadBalancer.getConnectionLimit();
        Set<AccessList> accessList = loadBalancer.getAccessLists();

        properties.setBasic(basic);
        protection.setProperties(properties);

        cProtection = protection;
        return null;
    }

    public Persistence translatePersistenceResource(LoadBalancer loadBalancer) {
        Persistence persistence = new Persistence();
        PersistenceBasic basic = new PersistenceBasic();
        PersistenceProperties properties = new PersistenceProperties();

        properties.setBasic(basic);
        persistence.setProperties(properties);

        cPersistence = persistence;
        return null;
    }

    private Set<String> getNodesWithCondition(Collection<Node> nodes, NodeCondition nodeCondition) {
        Set<String> nodesWithCondition = new HashSet<String>();
        for (Node node : nodes) {
            if (node.getCondition().equals(nodeCondition)) {
                nodesWithCondition.add(String.format("%s:%d", node.getIpAddress(), node.getPort()));
            }
        }
        return nodesWithCondition;
    }

    public Pool getcPool() {
        return cPool;
    }

    public void setcPool(Pool cPool) {
        this.cPool = cPool;
    }

    public Monitor getcMonitor() {
        return cMonitor;
    }

    public void setcMonitor(Monitor cMonitor) {
        this.cMonitor = cMonitor;
    }

    public VirtualServer getcVServer() {
        return cVServer;
    }

    public void setcVServer(VirtualServer cVServer) {
        this.cVServer = cVServer;
    }

    public Protection getcProtection() {
        return cProtection;
    }

    public void setcProtection(Protection cProtection) {
        this.cProtection = cProtection;
    }

    public Persistence getcPersistence() {
        return cPersistence;
    }

    public void setcPersistence(Persistence cPersistence) {
        this.cPersistence = cPersistence;
    }
}
