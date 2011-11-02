package org.openstack.atlas.service.domain.stub;

import org.openstack.atlas.core.api.v1.*;
import org.openstack.atlas.core.api.v1.IpVersion;
import org.openstack.atlas.core.api.v1.SessionPersistence;
import org.openstack.atlas.datamodel.CoreHealthMonitorType;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.entity.ConnectionThrottle;
import org.openstack.atlas.service.domain.entity.HealthMonitor;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.Node;
import org.openstack.atlas.service.domain.entity.VirtualIp;

import java.util.Calendar;

/*
    Used for testing purposes.
 */
public class StubFactory {
    protected static final Integer ACCOUNT_ID = 99999999;
    protected static final Integer LOAD_BALANCER_ID = 1;
    protected static final String LOAD_BALANCER_NAME = "My first load balancer";
    protected static final Integer LOAD_BALANCER_PORT = 80;
    protected static final String LOAD_BALANCER_PROTOCOL = "HTTP";
    protected static final String LOAD_BALANCER_ALGORITHM = "ROUND_ROBIN";
    protected static final String LOAD_BALANCER_STATUS = "ACTIVE";
    protected static final Integer NODE1_ID = 1;
    protected static final Integer NODE2_ID = 2;
    protected static final Integer NODE1_PORT = 80;
    protected static final Integer NODE2_PORT = 81;
    protected static final Boolean NODE1_ENABLED = true;
    protected static final Boolean NODE2_ENABLED = false;
    protected static final String NODE1_STATUS = "ONLINE";
    protected static final String NODE2_STATUS = "OFFLINE";
    protected static final String NODE1_ADDRESS = "10.1.1.1";
    protected static final String NODE2_ADDRESS = "10.1.1.2";
    protected static final Integer NODE1_WEIGHT = 1;
    protected static final Integer NODE2_WEIGHT = 2;
    protected static final Integer VIP1_ID = 1;
    protected static final Integer VIP2_ID = 2;
    protected static final String VIP1_ADDRESS = "10.10.10.1";
    protected static final String VIP2_ADDRESS = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
    protected static final String VIP1_TYPE = "PUBLIC";
    protected static final String VIP2_TYPE = "PUBLIC";
    protected static final String VIP1_VERSION = "IPV4";
    protected static final String VIP2_VERSION = "IPV6";
    protected static final Integer CONNECTION_THROTTLE_MAX_REQUEST_RATE = 100;
    protected static final Integer CONNECTION_THROTTLE_RATE_INTERVAL = 20;
    protected static final Integer HEALTH_MONITOR_ATTEMPTS_BEFORE_DEACTIVATION = 3;
    protected static final Integer HEALTH_MONITOR_DELAY = 5;
    protected static final Integer HEALTH_MONITOR_TIMEOUT = 10;
    protected static final String HEALTH_MONITOR_PATH = "/";
    protected static final String HEALTH_MONITOR_TYPE = "HTTP";
    protected static final String SESSION_PERSISTENCE_TYPE = "HTTP_COOKIE";

    public static org.openstack.atlas.core.api.v1.VirtualIp createSharedDataModelVipForPost() {
        return createMinimalDataModelVipForPost(VIP1_ID, null, null, null);
    }

    public static org.openstack.atlas.core.api.v1.VirtualIp createDataModelVipWithIpVersionForPost() {
        return createMinimalDataModelVipForPost(null, null, org.openstack.atlas.core.api.v1.IpVersion.valueOf(VIP1_VERSION), null);
    }

    public static org.openstack.atlas.core.api.v1.VirtualIp createDataModelVipWithVipTypeForPost() {
        return createMinimalDataModelVipForPost(null, null, null, VipType.valueOf(VIP1_TYPE));
    }

    public static org.openstack.atlas.core.api.v1.VirtualIp createHydratedDataModelVipForPost() {
        return createMinimalDataModelVipForPost(null, null, org.openstack.atlas.core.api.v1.IpVersion.valueOf(VIP1_VERSION), VipType.valueOf(VIP1_TYPE));
    }

    protected static org.openstack.atlas.core.api.v1.VirtualIp createMinimalDataModelVipForPost(Integer id, String address, org.openstack.atlas.core.api.v1.IpVersion ipVersion, VipType vipType) {
        org.openstack.atlas.core.api.v1.VirtualIp virtualIp = new org.openstack.atlas.core.api.v1.VirtualIp();

        virtualIp.setId(id);
        virtualIp.setAddress(address);
        virtualIp.setIpVersion(ipVersion);
        virtualIp.setType(vipType);

        return virtualIp;
    }

    public static org.openstack.atlas.core.api.v1.Node createMinimalDataModelNodeForPost() {
        return createDataModelNodeForPost(null, NODE1_ADDRESS, NODE1_PORT, null, true, null);
    }

    public static org.openstack.atlas.core.api.v1.Node createHydratedDataModelNode() {
        return createDataModelNodeForPost(NODE1_ID, NODE1_ADDRESS, NODE1_PORT, NODE1_WEIGHT, NODE1_ENABLED, NODE1_STATUS);
    }

    protected static org.openstack.atlas.core.api.v1.Node createDataModelNodeForPost(Integer id, String address, Integer port, Integer weight, boolean enabled, String status) {
        org.openstack.atlas.core.api.v1.Node node = new org.openstack.atlas.core.api.v1.Node();

        node.setId(id);
        node.setAddress(address);
        node.setPort(port);
        node.setWeight(weight);
        node.setEnabled(enabled);
        node.setStatus(status);

        return node;
    }

    public static org.openstack.atlas.core.api.v1.LoadBalancer createMinimalDataModelLoadBalancerForPost() {
        org.openstack.atlas.core.api.v1.LoadBalancer loadBalancer = new org.openstack.atlas.core.api.v1.LoadBalancer();

        loadBalancer.setName(LOAD_BALANCER_NAME);

        org.openstack.atlas.core.api.v1.Node node1 = createDataModelNodeForPost(null, NODE1_ADDRESS, NODE1_PORT, null, true, null);
        final org.openstack.atlas.core.api.v1.Nodes nodes = new org.openstack.atlas.core.api.v1.Nodes();
        nodes.getNodes().add(node1);
        loadBalancer.getNodes().addAll(nodes.getNodes());

        return loadBalancer;
    }

    public static org.openstack.atlas.core.api.v1.LoadBalancer createHydratedDataModelLoadBalancerForPost() {
        org.openstack.atlas.core.api.v1.LoadBalancer loadBalancer = createMinimalDataModelLoadBalancerForPost();

        loadBalancer.setPort(LOAD_BALANCER_PORT);
        loadBalancer.setProtocol(LOAD_BALANCER_PROTOCOL);
        loadBalancer.setAlgorithm(LOAD_BALANCER_ALGORITHM);

        loadBalancer.getNodes().get(0).setEnabled(NODE1_ENABLED);

        org.openstack.atlas.core.api.v1.VirtualIp virtualIp1 = new org.openstack.atlas.core.api.v1.VirtualIp();
        virtualIp1.setType(org.openstack.atlas.core.api.v1.VipType.fromValue(VIP1_TYPE));
        virtualIp1.setIpVersion(org.openstack.atlas.core.api.v1.IpVersion.fromValue(VIP1_VERSION));
        loadBalancer.getVirtualIps().add(virtualIp1);

        org.openstack.atlas.core.api.v1.VirtualIp virtualIp2 = new org.openstack.atlas.core.api.v1.VirtualIp();
        virtualIp2.setId(VIP2_ID);

        loadBalancer.setConnectionThrottle(createHydratedDataModelConnectionThrottle());
        loadBalancer.setHealthMonitor(createHydratedDataModelHealthMonitor());
        loadBalancer.setSessionPersistence(createHydratedDataModelSessionPersistence());

        return loadBalancer;
    }

    public static org.openstack.atlas.core.api.v1.LoadBalancer createHydratedDataModelLoadBalancer() throws Exception {
        // TODO: Call minimal method first and use the values from it.
        org.openstack.atlas.core.api.v1.LoadBalancer loadBalancer = new org.openstack.atlas.core.api.v1.LoadBalancer();

        loadBalancer.setId(LOAD_BALANCER_ID);
        loadBalancer.setName(LOAD_BALANCER_NAME);
        loadBalancer.setPort(LOAD_BALANCER_PORT);
        loadBalancer.setProtocol(LOAD_BALANCER_PROTOCOL);
        loadBalancer.setAlgorithm(LOAD_BALANCER_ALGORITHM);
        loadBalancer.setStatus(LOAD_BALANCER_STATUS);

        org.openstack.atlas.core.api.v1.Node node1 = createDataModelNodeForPost(NODE1_ID, NODE1_ADDRESS, NODE1_PORT, NODE1_WEIGHT, NODE1_ENABLED, NODE1_STATUS);
        org.openstack.atlas.core.api.v1.Node node2 = createDataModelNodeForPost(NODE2_ID, NODE2_ADDRESS, NODE2_PORT, NODE2_WEIGHT, NODE2_ENABLED, NODE2_STATUS);
        final org.openstack.atlas.core.api.v1.Nodes nodes = new org.openstack.atlas.core.api.v1.Nodes();
        nodes.getNodes().add(node1);
        nodes.getNodes().add(node2);
        loadBalancer.getNodes().addAll(nodes.getNodes());

        org.openstack.atlas.core.api.v1.VirtualIp virtualIp1 = new org.openstack.atlas.core.api.v1.VirtualIp();
        virtualIp1.setId(VIP1_ID);
        virtualIp1.setAddress(VIP1_ADDRESS);
        virtualIp1.setType(org.openstack.atlas.core.api.v1.VipType.fromValue(VIP1_TYPE));
        virtualIp1.setIpVersion(org.openstack.atlas.core.api.v1.IpVersion.fromValue(VIP1_VERSION));
        loadBalancer.getVirtualIps().add(virtualIp1);

        loadBalancer.setConnectionThrottle(createHydratedDataModelConnectionThrottle());
        loadBalancer.setHealthMonitor(createHydratedDataModelHealthMonitor());
        loadBalancer.setSessionPersistence(createHydratedDataModelSessionPersistence());
        loadBalancer.setCreated(Calendar.getInstance());
        loadBalancer.setUpdated(Calendar.getInstance());

        return loadBalancer;
    }


    public static LoadBalancer createHydratedDomainLoadBalancer() {
        LoadBalancer loadBalancer = new LoadBalancer();

        loadBalancer.setAccountId(ACCOUNT_ID);
        loadBalancer.setId(LOAD_BALANCER_ID);
        loadBalancer.setName(LOAD_BALANCER_NAME);
        loadBalancer.setPort(LOAD_BALANCER_PORT);
        loadBalancer.setProtocol(LOAD_BALANCER_PROTOCOL);
        loadBalancer.setAlgorithm(LOAD_BALANCER_ALGORITHM);
        loadBalancer.setStatus(LOAD_BALANCER_STATUS);

        Node node1 = new Node();
        node1.setId(NODE1_ID);
        node1.setAddress(NODE1_ADDRESS);
        node1.setPort(NODE1_PORT);
        node1.setEnabled(NODE1_ENABLED);
        node1.setStatus(NODE1_STATUS);
        loadBalancer.getNodes().add(node1);

        Node node2 = new Node();
        node2.setId(NODE2_ID);
        node2.setAddress(NODE2_ADDRESS);
        node2.setPort(NODE2_PORT);
        node2.setEnabled(NODE2_ENABLED);
        node2.setStatus(NODE2_STATUS);
        loadBalancer.getNodes().add(node2);

        VirtualIp virtualIp1 = new VirtualIp();
        virtualIp1.setId(VIP1_ID);
        virtualIp1.setAddress(VIP1_ADDRESS);
        virtualIp1.setVipType(org.openstack.atlas.service.domain.entity.VirtualIpType.valueOf(VIP1_TYPE));

        LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip(LOAD_BALANCER_PORT, loadBalancer, virtualIp1);
        loadBalancer.getLoadBalancerJoinVipSet().add(loadBalancerJoinVip);

        loadBalancer.setConnectionThrottle(createHydratedDomainConnectionThrottle());
        loadBalancer.setHealthMonitor(createHydratedDomainHealthMonitor());
        loadBalancer.setSessionPersistence(createHydratedDomainSessionPersistence());
        loadBalancer.setCreated(Calendar.getInstance());
        loadBalancer.setUpdated(Calendar.getInstance());

        return loadBalancer;
    }

    public static LoadBalancer createMinimalDomainLoadBalancer() {
        LoadBalancer loadBalancer = new LoadBalancer();

        loadBalancer.setAccountId(ACCOUNT_ID);
        loadBalancer.setName(LOAD_BALANCER_NAME);

        Node node1 = new Node();
        node1.setAddress(NODE1_ADDRESS);
        node1.setPort(NODE1_PORT);
        loadBalancer.getNodes().add(node1);

        return loadBalancer;
    }

    public static Nodes createMinimalDataModelNodesForPost() {
        Nodes nodes = new Nodes();

        nodes.getNodes().add(createDataModelNodeForPost(null, NODE1_ADDRESS, NODE1_PORT, null, true, null));

        return nodes;
    }

    public static Nodes createHydratedDataModelNodesForPost() {
        Nodes nodes = new Nodes();

        nodes.getNodes().add(createDataModelNodeForPost(null, NODE1_ADDRESS, NODE1_PORT, NODE1_WEIGHT, NODE1_ENABLED, null));
        nodes.getNodes().add(createDataModelNodeForPost(null, NODE2_ADDRESS, NODE2_PORT, NODE2_WEIGHT, NODE2_ENABLED, null));

        return nodes;
    }

    public static org.openstack.atlas.core.api.v1.HealthMonitor createHydratedDataModelConnectMonitorForPut() {
        org.openstack.atlas.core.api.v1.HealthMonitor monitor = new org.openstack.atlas.core.api.v1.HealthMonitor();

        monitor.setType(CoreHealthMonitorType.CONNECT);
        monitor.setDelay(HEALTH_MONITOR_DELAY);
        monitor.setTimeout(HEALTH_MONITOR_TIMEOUT);
        monitor.setAttemptsBeforeDeactivation(HEALTH_MONITOR_ATTEMPTS_BEFORE_DEACTIVATION);

        return monitor;
    }

    public static org.openstack.atlas.core.api.v1.HealthMonitor createHydratedDataModelHttpMonitorForPut() {
        org.openstack.atlas.core.api.v1.HealthMonitor monitor = createHydratedDataModelConnectMonitorForPut();

        monitor.setType(CoreHealthMonitorType.HTTP);
        monitor.setPath(HEALTH_MONITOR_PATH);

        return monitor;
    }

    public static org.openstack.atlas.core.api.v1.ConnectionThrottle createHydratedDataModelConnectionThrottle() {
        org.openstack.atlas.core.api.v1.ConnectionThrottle throttle = new org.openstack.atlas.core.api.v1.ConnectionThrottle();

        throttle.setMaxRequestRate(CONNECTION_THROTTLE_MAX_REQUEST_RATE);
        throttle.setRateInterval(CONNECTION_THROTTLE_RATE_INTERVAL);

        return throttle;
    }

    public static ConnectionThrottle createHydratedDomainConnectionThrottle() {
        ConnectionThrottle connectionThrottle = new ConnectionThrottle();

        connectionThrottle.setMaxRequestRate(CONNECTION_THROTTLE_MAX_REQUEST_RATE);
        connectionThrottle.setRateInterval(CONNECTION_THROTTLE_RATE_INTERVAL);

        return connectionThrottle;
    }

    public static SessionPersistence createHydratedDataModelSessionPersistence() {
        org.openstack.atlas.core.api.v1.SessionPersistence sessionPersistence = new org.openstack.atlas.core.api.v1.SessionPersistence();
        sessionPersistence.setPersistenceType(SESSION_PERSISTENCE_TYPE);
        return sessionPersistence;
    }

    public static org.openstack.atlas.service.domain.entity.SessionPersistence createHydratedDomainSessionPersistence() {
        org.openstack.atlas.service.domain.entity.SessionPersistence sessionPersistence = new org.openstack.atlas.service.domain.entity.SessionPersistence();

        sessionPersistence.setPersistenceType(SESSION_PERSISTENCE_TYPE);

        return sessionPersistence;
    }

    public static Node createHydratedDomainNode() {
        Node node = new Node();

        node.setId(NODE1_ID);
        node.setAddress(NODE1_ADDRESS);
        node.setPort(NODE1_PORT);
        node.setWeight(NODE1_WEIGHT);
        node.setEnabled(NODE1_ENABLED);
        node.setStatus(NODE1_STATUS);

        return node;
    }

    public static org.openstack.atlas.core.api.v1.VirtualIp createHydratedDataModelVirtualIp() {
        org.openstack.atlas.core.api.v1.VirtualIp virtualIp = new org.openstack.atlas.core.api.v1.VirtualIp();

        virtualIp.setId(VIP1_ID);
        virtualIp.setAddress(VIP1_ADDRESS);
        virtualIp.setType(VipType.fromValue(VIP1_TYPE));
        virtualIp.setIpVersion(IpVersion.fromValue(VIP1_VERSION));

        return virtualIp;
    }

    public static LoadBalancerJoinVip6 createHydratedDomainVirtualIpv6() {
        LoadBalancer loadBalancer = createHydratedDomainLoadBalancer();
        VirtualIpv6 virtualIpv6 = new VirtualIpv6();
        virtualIpv6.setAccountId(loadBalancer.getAccountId());
        virtualIpv6.setVipOctets(1);
        virtualIpv6.setCluster(createdHydratedCluster());

        return new LoadBalancerJoinVip6(LOAD_BALANCER_PORT, loadBalancer, virtualIpv6);
    }

    public static Cluster createdHydratedCluster() {
        Cluster cluster = new Cluster();
        cluster.setId(1);
        cluster.setName("test");
        cluster.setDescription("test");
        cluster.setClusterIpv6Cidr("fd24:f480:ce44:91bc::/64");
        cluster.setUsername("username");
        cluster.setPassword("0d1131d28d76ba0a72f42f819d207c94");

        return cluster;
    }

    public static LoadBalancerJoinVip createHydratedLoadBalancerJoinVip() {
        LoadBalancer loadBalancer = createHydratedDomainLoadBalancer();
        VirtualIp virtualIp = createHydratedDomainVirtualIp();
        return new LoadBalancerJoinVip(LOAD_BALANCER_PORT, loadBalancer, virtualIp);
    }

    public static VirtualIp createHydratedDomainVirtualIp() {
        VirtualIp virtualIp = new VirtualIp();
        virtualIp.setId(VIP1_ID);
        virtualIp.setAddress(VIP1_ADDRESS);
        virtualIp.setCluster(createdHydratedCluster());
        virtualIp.setVipType(VirtualIpType.valueOf(VIP1_TYPE));
        return virtualIp;
    }

    public static org.openstack.atlas.core.api.v1.HealthMonitor createHydratedDataModelHealthMonitor() {
        org.openstack.atlas.core.api.v1.HealthMonitor healthMonitor = new org.openstack.atlas.core.api.v1.HealthMonitor();

        healthMonitor.setType(HEALTH_MONITOR_TYPE);
        healthMonitor.setDelay(HEALTH_MONITOR_DELAY);
        healthMonitor.setTimeout(HEALTH_MONITOR_TIMEOUT);
        healthMonitor.setAttemptsBeforeDeactivation(HEALTH_MONITOR_ATTEMPTS_BEFORE_DEACTIVATION);
        healthMonitor.setPath(HEALTH_MONITOR_PATH);

        return healthMonitor;
    }

    public static HealthMonitor createHydratedDomainHealthMonitor() {
        HealthMonitor healthMonitor = new HealthMonitor();

        healthMonitor.setType(HEALTH_MONITOR_TYPE);
        healthMonitor.setDelay(HEALTH_MONITOR_DELAY);
        healthMonitor.setTimeout(HEALTH_MONITOR_TIMEOUT);
        healthMonitor.setAttemptsBeforeDeactivation(HEALTH_MONITOR_ATTEMPTS_BEFORE_DEACTIVATION);
        healthMonitor.setPath(HEALTH_MONITOR_PATH);

        return healthMonitor;
    }

    public static UsageRecord createHydratedDomainUsageRecord() {
        UsageRecord usageRecord = new UsageRecord();

        usageRecord.setId(1);
        usageRecord.setTransferBytesIn(1024l);
        usageRecord.setTransferBytesOut(1024l);
        usageRecord.setLastBytesInCount(1024l);
        usageRecord.setLastBytesOutCount(1024l);
        usageRecord.setStartTime(Calendar.getInstance());
        usageRecord.setEndTime(Calendar.getInstance());

        return usageRecord;
    }
}
