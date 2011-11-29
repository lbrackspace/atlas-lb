package org.openstack.atlas.rax.domain.stub;

import org.openstack.atlas.rax.datamodel.XmlHelper;
import org.openstack.atlas.rax.domain.entity.RaxAccessListType;
import org.openstack.atlas.rax.domain.entity.RaxAccessList;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.rax.domain.helper.ExtensionConverter;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.stub.StubFactory;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class RaxStubFactory extends StubFactory {
    protected static final Integer NETWORK_ITEM1_ID = 1;
    protected static final String NETWORK_ITEM1_ADDRESS = "1.1.1.1";
    protected static final String NETWORK_ITEM1_IP_VERSION = "IPV4";
    protected static final String NETWORK_ITEM1_TYPE = "DENY";

    public static org.openstack.atlas.core.api.v1.LoadBalancer createHydratedDataModelLoadBalancerForRaxPost() throws JAXBException, ParserConfigurationException {
        org.openstack.atlas.core.api.v1.LoadBalancer loadBalancer = StubFactory.createHydratedDataModelLoadBalancerForPost();

        loadBalancer.getOtherAttributes().put(new QName("http://docs.openstack.org/atlas/api/v1.1/extensions/rax", "crazyName", "rax"), "foo");

        RaxAccessList accessList = new RaxAccessList();
        accessList.setIpAddress(NETWORK_ITEM1_ADDRESS);
        accessList.setIpVersion(IpVersion.valueOf(NETWORK_ITEM1_IP_VERSION));
        accessList.setType(RaxAccessListType.valueOf(NETWORK_ITEM1_TYPE));
        Set<RaxAccessList> accessListSet = new HashSet<RaxAccessList>();
        accessListSet.add(accessList);

        org.openstack.atlas.api.v1.extensions.rax.AccessList dataModelAccessList = ExtensionConverter.convertAccessList(accessListSet);
        loadBalancer.getAnies().add(XmlHelper.marshall(dataModelAccessList));

        return loadBalancer;
    }

    public static org.openstack.atlas.core.api.v1.LoadBalancer createHydratedDataModelLoadBalancer() throws Exception {
        org.openstack.atlas.core.api.v1.LoadBalancer loadBalancer = StubFactory.createHydratedDataModelLoadBalancer();

        loadBalancer.getOtherAttributes().put(new QName("http://docs.openstack.org/atlas/api/v1.1/extensions/rax", "crazyName", "rax"), "foo");

        RaxAccessList accessList = new RaxAccessList();
        accessList.setIpAddress(NETWORK_ITEM1_ADDRESS);
        accessList.setIpVersion(IpVersion.valueOf(NETWORK_ITEM1_IP_VERSION));
        accessList.setType(RaxAccessListType.valueOf(NETWORK_ITEM1_TYPE));
        Set<RaxAccessList> accessListSet = new HashSet<RaxAccessList>();
        accessListSet.add(accessList);

        org.openstack.atlas.api.v1.extensions.rax.AccessList dataModelAccessList = ExtensionConverter.convertAccessList(accessListSet);
        loadBalancer.getAnies().add(XmlHelper.marshall(dataModelAccessList));

        return loadBalancer;
    }


    public static RaxLoadBalancer createHydratedDomainLoadBalancer() {
        RaxLoadBalancer loadBalancer = new RaxLoadBalancer();

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

        ConnectionThrottle throttle = new ConnectionThrottle();
        throttle.setMaxRequestRate(CONNECTION_THROTTLE_MAX_REQUEST_RATE);
        throttle.setRateInterval(CONNECTION_THROTTLE_RATE_INTERVAL);
        loadBalancer.setConnectionThrottle(throttle);

        HealthMonitor healthMonitor = new HealthMonitor();
        healthMonitor.setAttemptsBeforeDeactivation(HEALTH_MONITOR_ATTEMPTS_BEFORE_DEACTIVATION);
        healthMonitor.setDelay(HEALTH_MONITOR_DELAY);
        healthMonitor.setTimeout(HEALTH_MONITOR_TIMEOUT);
        healthMonitor.setPath(HEALTH_MONITOR_PATH);
        healthMonitor.setType(HEALTH_MONITOR_TYPE);
        loadBalancer.setHealthMonitor(healthMonitor);

        loadBalancer.setSessionPersistence(createHydratedDomainSessionPersistence());

        loadBalancer.setCreated(Calendar.getInstance());
        loadBalancer.setUpdated(Calendar.getInstance());

        // RAX SPECIFIC SETTINGS
        loadBalancer.setCrazyName("foobar");

        RaxAccessList accessList = new RaxAccessList();
        accessList.setId(NETWORK_ITEM1_ID);
        accessList.setIpAddress(NETWORK_ITEM1_ADDRESS);
        accessList.setIpVersion(IpVersion.valueOf(NETWORK_ITEM1_IP_VERSION));
        accessList.setType(RaxAccessListType.valueOf(NETWORK_ITEM1_TYPE));
        Set<RaxAccessList> accessLists = new HashSet<RaxAccessList>();
        accessLists.add(accessList);
        loadBalancer.setAccessLists(accessLists);

        return loadBalancer;
    }
}
