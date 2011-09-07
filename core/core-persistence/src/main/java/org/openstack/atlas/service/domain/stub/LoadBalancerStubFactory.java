package org.openstack.atlas.service.domain.stub;

import org.openstack.atlas.core.api.v1.ConnectionLogging;
import org.openstack.atlas.service.domain.entity.*;

import java.util.Calendar;

/*
    Used for testing purposes.
 */
public class LoadBalancerStubFactory {
    private static final Integer LOAD_BALANCER_ID = 1;
    private static final String LOAD_BALANCER_NAME = "My first load balancer";
    private static final Integer LOAD_BALANCER_PORT = 80;
    private static final String LOAD_BALANCER_PROTOCOL = "HTTP";
    private static final String LOAD_BALANCER_ALGORITHM = "ROUND_ROBIN";
    private static final String LOAD_BALANCER_STATUS = "ACTIVE";
    private static final Integer NODE1_ID = 1;
    private static final Integer NODE2_ID = 2;
    private static final Integer NODE1_PORT = 80;
    private static final Integer NODE2_PORT = 81;
    private static final String NODE1_CONDITION = "ENABLED";
    private static final String NODE2_CONDITION = "DISABLED";
    private static final String NODE1_STATUS = "ONLINE";
    private static final String NODE2_STATUS = "OFFLINE";
    private static final String NODE1_ADDRESS = "10.1.1.1";
    private static final String NODE2_ADDRESS = "10.1.1.2";
    private static final Integer VIP1_ID = 1;
    private static final Integer VIP2_ID = 2;
    private static final String VIP1_ADDRESS = "10.10.10.1";
    private static final String VIP2_ADDRESS = "10.10.10.2";
    private static final String VIP1_TYPE = "PUBLIC";
    private static final String VIP2_TYPE = "PUBLIC";
    private static final String VIP1_VERSION = "IPV4";
    private static final String VIP2_VERSION = "IPV4";
    private static final Integer CONNECTION_THROTTLE_MAX_REQUEST_RATE = 100;
    private static final Integer CONNECTION_THROTTLE_RATE_INTERVAL = 20;
    private static final Integer HEALTH_MONITOR_ATTEMPTS_BEFORE_DEACTIVATION = 3;
    private static final Integer HEALTH_MONITOR_DELAY = 5;
    private static final Integer HEALTH_MONITOR_TIMEOUT = 10;
    private static final String HEALTH_MONITOR_PATH = "/";
    private static final String HEALTH_MONITOR_TYPE = "CONNECT";
    private static final String SESSION_PERSISTENCE_TYPE = "HTTP_COOKIE";

    public static org.openstack.atlas.core.api.v1.LoadBalancer createMinimalDataModelLoadBalancerForPost() {
        org.openstack.atlas.core.api.v1.LoadBalancer loadBalancer = new org.openstack.atlas.core.api.v1.LoadBalancer();

        loadBalancer.setName(LOAD_BALANCER_NAME);

        org.openstack.atlas.core.api.v1.Node node1 = new org.openstack.atlas.core.api.v1.Node();
        node1.setAddress(NODE1_ADDRESS);
        node1.setPort(NODE1_PORT);

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

        loadBalancer.getNodes().get(0).setCondition(NODE1_CONDITION);

        org.openstack.atlas.core.api.v1.VirtualIp virtualIp1 = new org.openstack.atlas.core.api.v1.VirtualIp();
        virtualIp1.setType(org.openstack.atlas.core.api.v1.VipType.fromValue(VIP1_TYPE));
        virtualIp1.setIpVersion(org.openstack.atlas.core.api.v1.IpVersion.fromValue(VIP1_VERSION));
        loadBalancer.getVirtualIps().add(virtualIp1);

        org.openstack.atlas.core.api.v1.VirtualIp virtualIp2 = new org.openstack.atlas.core.api.v1.VirtualIp();
        virtualIp2.setId(VIP2_ID);

        org.openstack.atlas.core.api.v1.ConnectionThrottle throttle = new org.openstack.atlas.core.api.v1.ConnectionThrottle();
        throttle.setMaxRequestRate(CONNECTION_THROTTLE_MAX_REQUEST_RATE);
        throttle.setRateInterval(CONNECTION_THROTTLE_RATE_INTERVAL);
        loadBalancer.setConnectionThrottle(throttle);

        org.openstack.atlas.core.api.v1.HealthMonitor healthMonitor = new org.openstack.atlas.core.api.v1.HealthMonitor();
        healthMonitor.setAttemptsBeforeDeactivation(HEALTH_MONITOR_ATTEMPTS_BEFORE_DEACTIVATION);
        healthMonitor.setDelay(HEALTH_MONITOR_DELAY);
        healthMonitor.setTimeout(HEALTH_MONITOR_TIMEOUT);
        healthMonitor.setPath(HEALTH_MONITOR_PATH);
        healthMonitor.setType(HEALTH_MONITOR_TYPE);
        loadBalancer.setHealthMonitor(healthMonitor);

        org.openstack.atlas.core.api.v1.SessionPersistence sessionPersistence = new org.openstack.atlas.core.api.v1.SessionPersistence();
        sessionPersistence.setPersistenceType(SESSION_PERSISTENCE_TYPE);
        loadBalancer.setSessionPersistence(sessionPersistence);

        org.openstack.atlas.core.api.v1.ConnectionLogging connectionLogging = new ConnectionLogging();
        connectionLogging.setEnabled(true);
        loadBalancer.setConnectionLogging(connectionLogging);
        
        return loadBalancer;
    }

    public static org.openstack.atlas.core.api.v1.LoadBalancer createHydratedDataModelLoadBalancer() {
        // TODO: Call minimal method first and use the values from it.
        org.openstack.atlas.core.api.v1.LoadBalancer loadBalancer = new org.openstack.atlas.core.api.v1.LoadBalancer();

        loadBalancer.setId(LOAD_BALANCER_ID);
        loadBalancer.setName(LOAD_BALANCER_NAME);
        loadBalancer.setPort(LOAD_BALANCER_PORT);
        loadBalancer.setProtocol(LOAD_BALANCER_PROTOCOL);
        loadBalancer.setAlgorithm(LOAD_BALANCER_ALGORITHM);
        loadBalancer.setStatus(LOAD_BALANCER_STATUS);

        org.openstack.atlas.core.api.v1.Node node1 = new org.openstack.atlas.core.api.v1.Node();
        node1.setId(NODE1_ID);
        node1.setAddress(NODE1_ADDRESS);
        node1.setPort(NODE1_PORT);
        node1.setCondition(NODE1_CONDITION);
        node1.setStatus(NODE1_STATUS);

        org.openstack.atlas.core.api.v1.Node node2 = new org.openstack.atlas.core.api.v1.Node();
        node2.setId(NODE2_ID);
        node2.setAddress(NODE2_ADDRESS);
        node2.setPort(NODE2_PORT);
        node2.setCondition(NODE2_CONDITION);
        node2.setStatus(NODE2_STATUS);

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

        org.openstack.atlas.core.api.v1.ConnectionThrottle throttle = new org.openstack.atlas.core.api.v1.ConnectionThrottle();
        throttle.setMaxRequestRate(CONNECTION_THROTTLE_MAX_REQUEST_RATE);
        throttle.setRateInterval(CONNECTION_THROTTLE_RATE_INTERVAL);
        loadBalancer.setConnectionThrottle(throttle);

        org.openstack.atlas.core.api.v1.HealthMonitor healthMonitor = new org.openstack.atlas.core.api.v1.HealthMonitor();
        healthMonitor.setAttemptsBeforeDeactivation(HEALTH_MONITOR_ATTEMPTS_BEFORE_DEACTIVATION);
        healthMonitor.setDelay(HEALTH_MONITOR_DELAY);
        healthMonitor.setTimeout(HEALTH_MONITOR_TIMEOUT);
        healthMonitor.setPath(HEALTH_MONITOR_PATH);
        healthMonitor.setType(HEALTH_MONITOR_TYPE);
        loadBalancer.setHealthMonitor(healthMonitor);

        org.openstack.atlas.core.api.v1.SessionPersistence sessionPersistence = new org.openstack.atlas.core.api.v1.SessionPersistence();
        sessionPersistence.setPersistenceType(SESSION_PERSISTENCE_TYPE);
        loadBalancer.setSessionPersistence(sessionPersistence);

        org.openstack.atlas.core.api.v1.Created created = new org.openstack.atlas.core.api.v1.Created();
        org.openstack.atlas.core.api.v1.Updated updated = new org.openstack.atlas.core.api.v1.Updated();
        created.setTime(Calendar.getInstance());
        updated.setTime(Calendar.getInstance());
        loadBalancer.setCreated(created);
        loadBalancer.setUpdated(updated);

        return loadBalancer;
    }


    public static LoadBalancer createHydratedDomainLoadBalancer() {
        LoadBalancer loadBalancer = new LoadBalancer();

        loadBalancer.setId(LOAD_BALANCER_ID);
        loadBalancer.setName(LOAD_BALANCER_NAME);
        loadBalancer.setPort(LOAD_BALANCER_PORT);
        loadBalancer.setProtocol(org.openstack.atlas.service.domain.entity.LoadBalancerProtocol.valueOf(LOAD_BALANCER_PROTOCOL));
        loadBalancer.setAlgorithm(org.openstack.atlas.service.domain.entity.LoadBalancerAlgorithm.valueOf(LOAD_BALANCER_ALGORITHM));
        loadBalancer.setStatus(org.openstack.atlas.service.domain.entity.LoadBalancerStatus.valueOf(LOAD_BALANCER_STATUS));

        Node node1 = new Node();
        node1.setId(NODE1_ID);
        node1.setAddress(NODE1_ADDRESS);
        node1.setPort(NODE1_PORT);
        node1.setCondition(org.openstack.atlas.service.domain.entity.NodeCondition.valueOf(NODE1_CONDITION));
        node1.setStatus(org.openstack.atlas.service.domain.entity.NodeStatus.valueOf(NODE1_STATUS));
        loadBalancer.getNodes().add(node1);

        Node node2 = new Node();
        node2.setId(NODE2_ID);
        node2.setAddress(NODE2_ADDRESS);
        node2.setPort(NODE2_PORT);
        node2.setCondition(org.openstack.atlas.service.domain.entity.NodeCondition.valueOf(NODE2_CONDITION));
        node2.setStatus(org.openstack.atlas.service.domain.entity.NodeStatus.valueOf(NODE2_STATUS));
        loadBalancer.getNodes().add(node2);

        VirtualIp virtualIp1 = new VirtualIp();
        virtualIp1.setId(VIP1_ID);
        virtualIp1.setAddress(VIP1_ADDRESS);
        virtualIp1.setVipType(org.openstack.atlas.service.domain.entity.VirtualIpType.valueOf(VIP1_TYPE));
        virtualIp1.setIpVersion(org.openstack.atlas.service.domain.entity.IpVersion.valueOf(VIP1_VERSION));

        LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip(LOAD_BALANCER_PORT, loadBalancer, virtualIp1);
        loadBalancer.getLoadBalancerJoinVipSet().add(loadBalancerJoinVip);

        ConnectionThrottle throttle = new ConnectionThrottle();
        throttle.setMaxRequestRate(CONNECTION_THROTTLE_MAX_REQUEST_RATE);
        throttle.setRateInterval(CONNECTION_THROTTLE_RATE_INTERVAL);
        loadBalancer.setConnectionThrottle(throttle);

        HealthMonitor healthMonitor = new HealthMonitor();
        healthMonitor.setAttemptsBeforeDeactivation(HEALTH_MONITOR_ATTEMPTS_BEFORE_DEACTIVATION);
        healthMonitor.setDelay(HEALTH_MONITOR_DELAY);
        healthMonitor.setTimeout(HEALTH_MONITOR_TIMEOUT);
        healthMonitor.setPath(HEALTH_MONITOR_PATH);
        healthMonitor.setType(org.openstack.atlas.service.domain.entity.HealthMonitorType.valueOf(HEALTH_MONITOR_TYPE));
        loadBalancer.setHealthMonitor(healthMonitor);

        loadBalancer.setSessionPersistence(org.openstack.atlas.service.domain.entity.SessionPersistence.valueOf(SESSION_PERSISTENCE_TYPE));

        loadBalancer.setCreated(Calendar.getInstance());
        loadBalancer.setUpdated(Calendar.getInstance());

        return loadBalancer;
    }
}
