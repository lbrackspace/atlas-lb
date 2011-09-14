package org.openstack.atlas.rax.domain.stub;

import org.openstack.atlas.api.v1.extensions.rax.AccessList;
import org.openstack.atlas.api.v1.extensions.rax.NetworkItem;
import org.openstack.atlas.api.v1.extensions.rax.NetworkItemType;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.stub.StubFactory;

import javax.xml.namespace.QName;
import java.util.Calendar;

public class RaxStubFactory extends StubFactory {

    public static org.openstack.atlas.core.api.v1.LoadBalancer createHydratedDataModelLoadBalancerForRaxPost() {
        org.openstack.atlas.core.api.v1.LoadBalancer loadBalancer = StubFactory.createHydratedDataModelLoadBalancerForPost();

        loadBalancer.getOtherAttributes().put(new QName("http://docs.openstack.org/atlas/api/v1.1/extensions/rax", "crazyName", "rax"), "foo");

        org.openstack.atlas.api.v1.extensions.rax.AccessList accessList = new AccessList();
        org.openstack.atlas.api.v1.extensions.rax.NetworkItem networkItem = new NetworkItem();
        networkItem.setAddress("10.1.1.1");
        networkItem.setIpVersion(org.openstack.atlas.api.v1.extensions.rax.IpVersion.IPV4);
        networkItem.setType(NetworkItemType.DENY);
        accessList.getNetworkItems().add(networkItem);

        // TODO: Figure out how to create an Element object easily
//        Element element = new ElementImpl();
//        loadBalancer.getAnies().add();

        return loadBalancer;
    }

    public static org.openstack.atlas.core.api.v1.LoadBalancer createHydratedDataModelLoadBalancer() {
        org.openstack.atlas.core.api.v1.LoadBalancer loadBalancer = StubFactory.createHydratedDataModelLoadBalancer();

        loadBalancer.getOtherAttributes().put(new QName("http://docs.openstack.org/atlas/api/v1.1/extensions/rax", "crazyName", "rax"), "foo");

        return loadBalancer;
    }


    public static RaxLoadBalancer createHydratedDomainLoadBalancer() {
        RaxLoadBalancer loadBalancer = new RaxLoadBalancer();

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

        // Rax specific values
        loadBalancer.setCrazyName("foobar");

        return loadBalancer;
    }
}
