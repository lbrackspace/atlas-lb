package org.openstack.atlas.adapter.helpers;

import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.service.domain.entities.*;

import java.util.HashSet;
import java.util.Set;

import static org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm.ROUND_ROBIN;
import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTP;
import static org.openstack.atlas.service.domain.entities.NodeCondition.DISABLED;
import static org.openstack.atlas.service.domain.entities.NodeCondition.ENABLED;

public class VTMTestBase {
    public static final Integer SLEEP_TIME_BETWEEN_TESTS = 500;

    public static final Integer TEST_ACCOUNT_ID = 999998;
    public static final Integer TEST_LOADBALANCER_ID = 999998;
    public static final Integer TEST_VIP_ID = 1000041;
    public static final Integer TEST_IPV6_VIP_ID = 1000061;
    public static final Integer ADDITIONAL_VIP_ID = 88887;
    public static final Integer ADDITIONAL_IPV6_VIP_ID = 88885;

    protected static LoadBalancer lb;
    protected static VirtualIp vip1;
    protected static VirtualIp vip2;
    protected static Node node1;
    protected static Node node2;

    protected static void setupIvars() {
        Set<LoadBalancerJoinVip> vipList = new HashSet<LoadBalancerJoinVip>();
        vip1 = new VirtualIp();
        vip1.setId(TEST_VIP_ID);
        vip1.setIpAddress("10.69.0.59");
        vip2 = new VirtualIp();
        vip2.setId(TEST_VIP_ID+1);
        vip2.setIpAddress("10.69.0.60");
        LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
        loadBalancerJoinVip.setVirtualIp(vip1);
        vipList.add(loadBalancerJoinVip);
        loadBalancerJoinVip = new LoadBalancerJoinVip();
        loadBalancerJoinVip.setVirtualIp(vip2);
        vipList.add(loadBalancerJoinVip);

        Set<Node> nodeList = new HashSet<Node>();
        node1 = new Node();
        node2 = new Node();
        node1.setIpAddress("127.0.0.1");
        node2.setIpAddress("127.0.0.2");
        node1.setPort(80);
        node2.setPort(80);
        node1.setCondition(ENABLED);
        node2.setCondition(DISABLED);
        node1.setWeight(1);
        node2.setWeight(1);
        nodeList.add(node1);
        nodeList.add(node2);

        LoadBalancer lb = new LoadBalancer();
        lb.setId(TEST_LOADBALANCER_ID);
        lb.setAccountId(TEST_ACCOUNT_ID);
        lb.setPort(80);
        lb.setAlgorithm(ROUND_ROBIN);
        lb.setName("VTM-TESTER");
        lb.setProtocol(HTTP);
        lb.setNodes(nodeList);
        lb.setLoadBalancerJoinVipSet(vipList);

        lb.setUserPages(new UserPages());

        VTMTestBase.lb = lb;
    }

    protected static String loadBalancerName() throws InsufficientRequestException {
        return VTMNameBuilder.genVSName(lb);
    }

    protected static String secureLoadBalancerName() throws InsufficientRequestException {
        return VTMNameBuilder.genSslVSName(lb);
    }

    protected static String redirectLoadBalancerName() throws InsufficientRequestException {
        return VTMNameBuilder.genRedirectVSName(lb);
    }

    protected static String poolName() throws InsufficientRequestException {
        return VTMNameBuilder.genVSName(lb);
    }

    protected static String protectionClassName() throws InsufficientRequestException {
        return VTMNameBuilder.genVSName(lb);
    }

    protected static String secureProtectionClassName() throws InsufficientRequestException {
        return VTMNameBuilder.genSslVSName(lb);
    }

    protected static String trafficIpGroupName(VirtualIp vip) throws InsufficientRequestException {
        return VTMNameBuilder.generateTrafficIpGroupName(lb, vip);
    }

    protected static String trafficIpGroupName(VirtualIpv6 ipv6Vip) throws InsufficientRequestException {
        return VTMNameBuilder.generateTrafficIpGroupName(lb, ipv6Vip);
    }

    protected static String rateLimitName() throws InsufficientRequestException {
        return VTMNameBuilder.genVSName(lb);
    }

    protected static String errorFileName() throws InsufficientRequestException {
        return VTMNameBuilder.generateErrorPageName(lb.getId(), lb.getAccountId());
    }

    protected static String monitorName() throws InsufficientRequestException {
        return VTMNameBuilder.genVSName(lb);
    }
}
