package org.openstack.atlas.adapter.zxtm;

import com.zxtm.service.client.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.ZxtmRollBackException;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.Cidr;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.pojos.Hostsubnet;
import org.openstack.atlas.service.domain.pojos.NetInterface;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.openstack.atlas.service.domain.entities.AccessListType.ALLOW;
import static org.openstack.atlas.service.domain.entities.AccessListType.DENY;
import static org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm.ROUND_ROBIN;
import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTP;
import static org.openstack.atlas.service.domain.entities.NodeCondition.DISABLED;
import static org.openstack.atlas.service.domain.entities.NodeCondition.ENABLED;
import static org.openstack.atlas.service.domain.entities.SessionPersistence.HTTP_COOKIE;

@RunWith(Enclosed.class)
public class ZxtmAdapterImplTest {

    public static class WhenTestingOrderOfZeusApiCalls {
        private LoadBalancerEndpointConfiguration dummyConfig;
        private ReverseProxyLoadBalancerAdapter adapterSpy;
        private ZxtmServiceStubs serviceStubs;
        private PoolBindingStub poolStub;
        private VirtualServerBindingStub vsStub;
        private TrafficIPGroupsBindingStub trafficIpGroupStub;
        private CatalogProtectionBindingStub protectionStub;
        private CatalogPersistenceBindingStub persistenceStub;
        private CatalogMonitorBindingStub monitorStub;
        private CatalogRuleBindingStub ruleStub;
        private CatalogRateBindingStub rateStub;
        private LoadBalancer lb;
        private static final String ZXTM_USERNAME = "mocked_username";
        private static final String ZXTM_PASSWORD = "mocked_password";
        private static final String ZXTM_ENDPOINT_URI = "https://mock.endpoint.uri:9090/soap";
        private static final String TARGET_HOST = "ztm-n01.mock.endpoint.uri";
        private static final String FAILOVER_HOST_1 = "ztm-n03.mock.endpoint.uri";
        private static final String FAILOVER_HOST_2 = "ztm-n04.mock.endpoint.uri";

        @Before
        public void standUp() throws RemoteException, MalformedURLException {
            List<String> targetFailoverHosts = new ArrayList<String>();
            targetFailoverHosts.add(FAILOVER_HOST_1);
            targetFailoverHosts.add(FAILOVER_HOST_2);
            Host soapEndpointHost = new Host();
            soapEndpointHost.setEndpoint(ZXTM_ENDPOINT_URI);
            Host trafficManagerHost = new Host();
            trafficManagerHost.setTrafficManagerName(TARGET_HOST);
            dummyConfig = new LoadBalancerEndpointConfiguration(soapEndpointHost, ZXTM_USERNAME, ZXTM_PASSWORD, trafficManagerHost, targetFailoverHosts);

            adapterSpy = spy(new ZxtmAdapterImpl());
            serviceStubs = mock(ZxtmServiceStubs.class);
            doReturn(serviceStubs).when(adapterSpy).getServiceStubs(Matchers.<LoadBalancerEndpointConfiguration>anyObject());

            poolStub = mock(PoolBindingStub.class);
            vsStub = mock(VirtualServerBindingStub.class);
            trafficIpGroupStub = mock(TrafficIPGroupsBindingStub.class);
            protectionStub = mock(CatalogProtectionBindingStub.class);
            persistenceStub = mock(CatalogPersistenceBindingStub.class);
            monitorStub = mock(CatalogMonitorBindingStub.class);
            ruleStub = mock(CatalogRuleBindingStub.class);
            rateStub = mock(CatalogRateBindingStub.class);
            when(serviceStubs.getPoolBinding()).thenReturn(poolStub);
            when(serviceStubs.getVirtualServerBinding()).thenReturn(vsStub);
            when(serviceStubs.getTrafficIpGroupBinding()).thenReturn(trafficIpGroupStub);
            when(serviceStubs.getProtectionBinding()).thenReturn(protectionStub);
            when(serviceStubs.getPersistenceBinding()).thenReturn(persistenceStub);
            when(serviceStubs.getMonitorBinding()).thenReturn(monitorStub);
            when(serviceStubs.getZxtmRuleCatalogService()).thenReturn(ruleStub);
            when(serviceStubs.getZxtmRateCatalogService()).thenReturn(rateStub);

            when(serviceStubs.getVirtualServerBinding().getRules(Matchers.<String[]>any())).thenReturn(new VirtualServerRule[][]{{}});
            when(serviceStubs.getVirtualServerBinding().getListenOnAllAddresses(Matchers.<String[]>any())).thenReturn(new boolean[]{false});
            when(serviceStubs.getVirtualServerBinding().getProtocol(Matchers.<String[]>any())).thenReturn(new VirtualServerProtocol[]{VirtualServerProtocol.fromValue(VirtualServerProtocol._http)});
            when(serviceStubs.getZxtmRuleCatalogService().getRuleNames()).thenReturn(new String[]{});
        }

        @Before
        public void setupSimpleLoadBalancer() {
            Set<LoadBalancerJoinVip> vipList = new HashSet<LoadBalancerJoinVip>();
            VirtualIp vip = new VirtualIp();
            vip.setId(1234);
            vip.setIpAddress("10.69.0.60");
            LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
            loadBalancerJoinVip.setVirtualIp(vip);
            vipList.add(loadBalancerJoinVip);

            Set<Node> nodeList = new HashSet<Node>();
            Node node1 = new Node();
            Node node2 = new Node();
            node1.setIpAddress("127.0.0.1");
            node2.setIpAddress("127.0.0.2");
            node1.setPort(80);
            node2.setPort(80);
            node1.setCondition(ENABLED);
            node2.setCondition(DISABLED);
            node1.setWeight(5);
            node2.setWeight(10);
            nodeList.add(node1);
            nodeList.add(node2);

            lb = new LoadBalancer();
            lb.setId(1234);
            lb.setAccountId(1234);
            lb.setPort(80);
            lb.setAlgorithm(ROUND_ROBIN);
            lb.setName("integration_test_lb");
            lb.setProtocol(HTTP);
            lb.setNodes(nodeList);
            lb.setLoadBalancerJoinVipSet(vipList);
        }

        @Test
        public void shouldRunInOrderWhenCreatingASimpleLoadBalancer() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
            InOrder inOrder = inOrder(poolStub, vsStub, trafficIpGroupStub, ruleStub, rateStub);
            adapterSpy.createLoadBalancer(dummyConfig, lb);
            inOrder.verify(poolStub).addPool(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
            inOrder.verify(poolStub).setDisabledNodes(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
            inOrder.verify(poolStub).setDrainingNodes(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
            inOrder.verify(poolStub).setNodesWeightings(Matchers.<String[]>anyObject(), Matchers.<PoolWeightingsDefinition[][]>anyObject());
            inOrder.verify(vsStub).addVirtualServer(Matchers.<String[]>anyObject(), Matchers.<VirtualServerBasicInfo[]>anyObject());
            inOrder.verify(trafficIpGroupStub).addTrafficIPGroup(Matchers.<String[]>anyObject(), Matchers.<TrafficIPGroupsDetails[]>anyObject());
            inOrder.verify(vsStub).setListenTrafficIPGroups(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
            inOrder.verify(poolStub).setLoadBalancingAlgorithm(Matchers.<String[]>anyObject(), Matchers.<PoolLoadBalancingAlgorithm[]>anyObject());
//            inOrder.verify(ruleStub).getRuleNames();
//            inOrder.verify(vsStub).getProtocol(Matchers.<String[]>any());
        }

        @Test
        public void shouldRunInOrderWhenCreatingAFullyConfiguredLoadBalancer() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
            lb.setAlgorithm(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN);
            lb.setSessionPersistence(HTTP_COOKIE);

            HealthMonitor monitor = new HealthMonitor();
            monitor.setType(HealthMonitorType.CONNECT);
            monitor.setDelay(10);
            monitor.setTimeout(20);
            monitor.setAttemptsBeforeDeactivation(3);
            lb.setHealthMonitor(monitor);

            ConnectionLimit limit = new ConnectionLimit();
            limit.setMaxConnections(50);
            limit.setRateInterval(10);
            limit.setMaxConnectionRate(10);
            limit.setMinConnections(1);
            lb.setConnectionLimit(limit);

            lb.setConnectionLogging(true);

            Set<AccessList> networkItems = new HashSet<AccessList>();
            AccessList item1 = new AccessList();
            AccessList item2 = new AccessList();
            item1.setIpAddress("0.0.0.0/0");
            item2.setIpAddress("127.0.0.1");
            item1.setType(DENY);
            item2.setType(ALLOW);
            networkItems.add(item1);
            networkItems.add(item2);

            lb.setAccessLists(networkItems);

            InOrder inOrder = inOrder(poolStub, vsStub, trafficIpGroupStub, monitorStub, protectionStub);
            adapterSpy.createLoadBalancer(dummyConfig, lb);
            inOrder.verify(poolStub).addPool(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
            inOrder.verify(poolStub).setDisabledNodes(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
            inOrder.verify(poolStub).setDrainingNodes(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
            inOrder.verify(poolStub).setNodesWeightings(Matchers.<String[]>anyObject(), Matchers.<PoolWeightingsDefinition[][]>anyObject());
            inOrder.verify(vsStub).addVirtualServer(Matchers.<String[]>anyObject(), Matchers.<VirtualServerBasicInfo[]>anyObject());
            inOrder.verify(trafficIpGroupStub).addTrafficIPGroup(Matchers.<String[]>anyObject(), Matchers.<TrafficIPGroupsDetails[]>anyObject());
            inOrder.verify(vsStub).setListenTrafficIPGroups(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
            inOrder.verify(poolStub).setLoadBalancingAlgorithm(Matchers.<String[]>anyObject(), Matchers.<PoolLoadBalancingAlgorithm[]>anyObject());
            inOrder.verify(poolStub).setPersistence(Matchers.<String[]>anyObject(), Matchers.<String[]>anyObject());
            inOrder.verify(monitorStub).setType(Matchers.<String[]>anyObject(), Matchers.<CatalogMonitorType[]>anyObject());
            inOrder.verify(vsStub).setProtection(Matchers.<String[]>anyObject(), Matchers.<String[]>anyObject());
            inOrder.verify(vsStub).setLogEnabled(Matchers.<String[]>anyObject(), Matchers.<boolean[]>anyObject());
            inOrder.verify(protectionStub).setAllowedAddresses(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
            inOrder.verify(protectionStub).setBannedAddresses(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
        }

        @Test
        public void shouldRunInOrderWhenDeletingALoadBalancer() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
            InOrder inOrder = inOrder(poolStub, vsStub, monitorStub, protectionStub, trafficIpGroupStub);
            adapterSpy.deleteLoadBalancer(dummyConfig, lb);
            inOrder.verify(poolStub).removeMonitors(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
            inOrder.verify(monitorStub).deleteMonitors(Matchers.<String[]>anyObject());
            inOrder.verify(vsStub).deleteVirtualServer(Matchers.<String[]>anyObject());
            inOrder.verify(poolStub).deletePool(Matchers.<String[]>anyObject());
            inOrder.verify(protectionStub).deleteProtection(Matchers.<String[]>anyObject());
            inOrder.verify(trafficIpGroupStub).deleteTrafficIPGroup(Matchers.<String[]>anyObject());
        }

    }

    public static class WhenSettingUpHostSubnets {
        private Hostssubnet hs1;
        private Hostssubnet hs2;
        private Hostssubnet hs3;
        private Hostssubnet hs4;
        private List<Hostssubnet> hList;

        @Before
        public void setUpClass() throws Exception {
            hs1 = new Hostssubnet();
            Hostsubnet hostsubnet;
            hostsubnet = new Hostsubnet();
            hostsubnet.setName("n01.zeus.something");
            hostsubnet.getNetInterfaces().add(newNetInterface("eth0", "192.168.0.0/16", "172.16.0.0/12"));
            hostsubnet.getNetInterfaces().add(newNetInterface("eth1", "10.69.0.0/24"));
            hs1.getHostsubnets().add(hostsubnet);
            hs2 = new Hostssubnet();
            hostsubnet = new Hostsubnet();
            hostsubnet.setName("n01.zeus.something");
            hostsubnet.getNetInterfaces().add(newNetInterface("eth1", "10.69.1.0/24"));
            hostsubnet.getNetInterfaces().add(newNetInterface("eth0", "169.254.0.0/16"));
            hostsubnet.getNetInterfaces().add(newNetInterface("lo", "127.0.0.0/8"));
            hs2.getHostsubnets().add(hostsubnet);
            hList = new ArrayList<Hostssubnet>();
            hList.add(hs1);
            hList.add(hs2);

            hs3 = new Hostssubnet();
            hostsubnet = new Hostsubnet();
            hostsubnet.setName("test");
            hostsubnet.getNetInterfaces().add(newNetInterface("eth0", "192.168.1.0/24", "192.168.2.0/24", "192.168.3.0/24"));
            hostsubnet.getNetInterfaces().add(newNetInterface("eth1", "127.0.0.0/8", "10.69.2.0/24", "10.69.1.0/24"));
            hostsubnet.getNetInterfaces().add(newNetInterface("lo", "169.254.0.0/16"));
            hs3.getHostsubnets().add(hostsubnet);

            hs4 = new Hostssubnet();
            hostsubnet = new Hostsubnet();
            hostsubnet.setName("test");
            hostsubnet.getNetInterfaces().add(newNetInterface("eth0", "192.168.2.0/24"));
            hostsubnet.getNetInterfaces().add(newNetInterface("eth1", "127.0.0.0/8"));
            hostsubnet.getNetInterfaces().add(newNetInterface("lo", "169.254.0.0/16"));
            hs4.getHostsubnets().add(hostsubnet);
        }

        @Test
        public void testdomainUnionSubnetMapping() {
            Hostssubnet resp = ZxtmAdapterImpl.domainUnionSubnetMapping(hList);
            Assert.assertTrue(resp.getHostsubnets().size() == 1);
            Assert.assertTrue(resp.getHostsubnets().get(0).getNetInterfaces().size() == 3);
        }

        @Test
        public void domainSubnetMappingRemove() {
            Hostssubnet resp = ZxtmAdapterImpl.domainSubnetMappingRemove(hs3, hs4);
            Assert.assertTrue(resp.getHostsubnets().size() == 1);
            Assert.assertTrue(resp.getHostsubnets().get(0).getNetInterfaces().size() == 2);
        }

        public static NetInterface newNetInterface(String name, String... blocks) {
            int i;
            NetInterface iface = new NetInterface();
            Cidr cidr;
            iface.setName(name);
            for (i = 0; i < blocks.length; i++) {
                cidr = new Cidr();
                cidr.setBlock(blocks[i]);
                iface.getCidrs().add(cidr);
            }
            return iface;
        }
    }
}
