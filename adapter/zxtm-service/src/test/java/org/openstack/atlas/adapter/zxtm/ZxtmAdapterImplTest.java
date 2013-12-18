package org.openstack.atlas.adapter.zxtm;

import org.openstack.atlas.util.ca.primitives.RsaConst;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import com.zxtm.service.client.*;
import org.apache.axis.types.UnsignedInt;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
import org.openstack.atlas.service.domain.pojos.*;
import org.openstack.atlas.util.ca.StringUtils;

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
import static org.openstack.atlas.service.domain.entities.NodeCondition.DRAINING;
import static org.openstack.atlas.service.domain.entities.NodeCondition.ENABLED;
import static org.openstack.atlas.service.domain.entities.SessionPersistence.HTTP_COOKIE;

@RunWith(Enclosed.class)
public class ZxtmAdapterImplTest {

    protected static final ZeusUtils zeusUtils;
    protected static final String testpkcs1 = "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIICWwIBAAKBgQCV9RAA8bqJ0igreCLb1cAQKlu9Sd/arkX4N42giadMgGkDDH96\n"
            + "nhus2x/Ljh2+I7C/pUuTVA83yRxIE4oc1OYXNsbxNqZhR8rPbeT1l/OJUJR2ohk8\n"
            + "wxYpdWjHTVwb6v0pDkVoGN2bq3QvkopzGRKoBKmJlhZ6jCyJQ+PKibv8QwIDAQAB\n"
            + "AoGAX+qLHP+BIGyC8yL5sQFswypE2bNp8tcrvBPUjA3gK6erA5833vdInmzeTnYN\n"
            + "gjQT9tFEoOzQew8w/s04ty5aKR4pUSQYyeK4zd5JQcqvkEArtD3xE9YtQ5P/Q1Pn\n"
            + "CWHV4TTltsqklnV4j1b1b5Iq+M+zXordWxpa+nBUzKjtDvkCQQDMc6SLtTfA76Qr\n"
            + "NKx5nehqb+eyD3k6iT7POmquZLXBb1OgH6l32/VtMnuyBw1qxIKwcvoXjgXjo6P0\n"
            + "hSOMeus/AkEAu8QR3erwdB8noFOkRzyzNy7lHzzArHvIJJXIOb8aVRCfDgRzetwV\n"
            + "ooJiGdQ4ZlfNFzcUALw4DdfyOwgrwFnB/QJAfzjSwnDQscktQglz7fW540mhRqbk\n"
            + "S+2NXQw/yCc5BpZM1k39eW0xKnMIj75dtft0S3Iwd6nTtlRkuMqEVW1DpwJATeRJ\n"
            + "kSjsNQ/TjXQFOzQYGHVQwUOfni3/WgX0uNfpIY6ynMsF6Nqx8udpsNNTzkjwIIhN\n"
            + "uPxbuIpzxz9dfv79NQJAS7wILWp270Tbo0kP2jDv84LJhqBumTw306n1kj8yqhfR\n"
            + "IzLnW0NQf9wGdv2z7fheczdcw8q8ZwfrruU2WGRmEg==\n"
            + "-----END RSA PRIVATE KEY-----";
    protected static final String testpkcs8 = "-----BEGIN PRIVATE KEY-----\n"
            + "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJX1EADxuonSKCt4\n"
            + "ItvVwBAqW71J39quRfg3jaCJp0yAaQMMf3qeG6zbH8uOHb4jsL+lS5NUDzfJHEgT\n"
            + "ihzU5hc2xvE2pmFHys9t5PWX84lQlHaiGTzDFil1aMdNXBvq/SkORWgY3ZurdC+S\n"
            + "inMZEqgEqYmWFnqMLIlD48qJu/xDAgMBAAECgYBf6osc/4EgbILzIvmxAWzDKkTZ\n"
            + "s2ny1yu8E9SMDeArp6sDnzfe90iebN5Odg2CNBP20USg7NB7DzD+zTi3LlopHilR\n"
            + "JBjJ4rjN3klByq+QQCu0PfET1i1Dk/9DU+cJYdXhNOW2yqSWdXiPVvVvkir4z7Ne\n"
            + "it1bGlr6cFTMqO0O+QJBAMxzpIu1N8DvpCs0rHmd6Gpv57IPeTqJPs86aq5ktcFv\n"
            + "U6AfqXfb9W0ye7IHDWrEgrBy+heOBeOjo/SFI4x66z8CQQC7xBHd6vB0HyegU6RH\n"
            + "PLM3LuUfPMCse8gklcg5vxpVEJ8OBHN63BWigmIZ1DhmV80XNxQAvDgN1/I7CCvA\n"
            + "WcH9AkB/ONLCcNCxyS1CCXPt9bnjSaFGpuRL7Y1dDD/IJzkGlkzWTf15bTEqcwiP\n"
            + "vl21+3RLcjB3qdO2VGS4yoRVbUOnAkBN5EmRKOw1D9ONdAU7NBgYdVDBQ5+eLf9a\n"
            + "BfS41+khjrKcywXo2rHy52mw01POSPAgiE24/Fu4inPHP11+/v01AkBLvAgtanbv\n"
            + "RNujSQ/aMO/zgsmGoG6ZPDfTqfWSPzKqF9EjMudbQ1B/3AZ2/bPt+F5zN1zDyrxn\n"
            + "B+uu5TZYZGYS\n"
            + "-----END PRIVATE KEY-----\n";
    protected static final String testCrt = "-----BEGIN CERTIFICATE-----\n"
            + "MIIDITCCAoqgAwIBAgIGATbHOhIHMA0GCSqGSIb3DQEBBQUAMG4xDDAKBgNVBAYT\n"
            + "A1VTQTEOMAwGA1UECBMFVGV4YXMxFDASBgNVBAcTC1NhbiBBbnRvbmlvMQ0wCwYD\n"
            + "VQQKEwRUZXN0MREwDwYDVQQLEwhUb3AgQ0EgMjEWMBQGA1UEAxMNVG9wIENBIDIg\n"
            + "VGVzdDAeFw0xMjA0MTgyMDUyMTNaFw0yMzA3MDYyMDUyMTNaMGsxDDAKBgNVBAYT\n"
            + "A1VTQTEOMAwGA1UECBMFVGV4YXMxFDASBgNVBAcTC1NhbiBBbnRvbmlvMQ0wCwYD\n"
            + "VQQKEwRUZXN0MREwDwYDVQQLEwhFbmQgVXNlcjETMBEGA1UEAxMKd3d3LmV1Lm9y\n"
            + "ZzCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAlfUQAPG6idIoK3gi29XAECpb\n"
            + "vUnf2q5F+DeNoImnTIBpAwx/ep4brNsfy44dviOwv6VLk1QPN8kcSBOKHNTmFzbG\n"
            + "8TamYUfKz23k9ZfziVCUdqIZPMMWKXVox01cG+r9KQ5FaBjdm6t0L5KKcxkSqASp\n"
            + "iZYWeowsiUPjyom7/EMCAwEAAaOBzDCByTAMBgNVHRMBAf8EAjAAMIGZBgNVHSME\n"
            + "gZEwgY6AFIHKHyKCb/UX5l1k/k4FDyDD4jfboW6kbDBqMRQwEgYDVQQDEwtUb3Ag\n"
            + "Q0EgVGVzdDEPMA0GA1UECxMGVG9wIENBMQ0wCwYDVQQKEwRUZXN0MRQwEgYDVQQH\n"
            + "EwtTYW4gQW50b25pbzEOMAwGA1UECBMFVGV4YXMxDDAKBgNVBAYTA1VTQYIGATbH\n"
            + "OSgXMB0GA1UdDgQWBBRdttAMJYQrChBJCpkqC1Yvy8nCuzANBgkqhkiG9w0BAQUF\n"
            + "AAOBgQA8LuaDGGmzCK5VEtPRGJdzBpYYFQUtoAEHoNBSmzBAZIwqAtKU/QmxbHOV\n"
            + "gDIO5BgO7+ZXFQpWOn6wjLIR9mvpixnEzcnZVPB2g/b32EqahhUZztBuK7EM3TzK\n"
            + "7bYqlQTqCxN/L+76HLrWXAU6WWlRJPuqc0byOzsSSRrdxrSBrg==\n"
            + "-----END CERTIFICATE-----\n";
    protected static final String testChain = "-----BEGIN CERTIFICATE-----\n"
            + "MIIDHjCCAoegAwIBAgIGATbHOSgXMA0GCSqGSIb3DQEBBQUAMGoxFDASBgNVBAMT\n"
            + "C1RvcCBDQSBUZXN0MQ8wDQYDVQQLEwZUb3AgQ0ExDTALBgNVBAoTBFRlc3QxFDAS\n"
            + "BgNVBAcTC1NhbiBBbnRvbmlvMQ4wDAYDVQQIEwVUZXhhczEMMAoGA1UEBhMDVVNB\n"
            + "MB4XDTEyMDQxODIwNTExM1oXDTIzMDcwNjIwNTExM1owbjEMMAoGA1UEBhMDVVNB\n"
            + "MQ4wDAYDVQQIEwVUZXhhczEUMBIGA1UEBxMLU2FuIEFudG9uaW8xDTALBgNVBAoT\n"
            + "BFRlc3QxETAPBgNVBAsTCFRvcCBDQSAyMRYwFAYDVQQDEw1Ub3AgQ0EgMiBUZXN0\n"
            + "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCZHmTOiuV071K1b/8cQYPkwq7l\n"
            + "OH+qpEvqiRugkk6UAlPsjbtr3l0BjVG6SMmQrxedx5lMaPxbqKCu8fkN9xpjVhpH\n"
            + "uhN3aCR/6AzOVPKP2OLMzxb2SpjkSmCyvfTcIy1GHsERvDivTuOTOeL6NldLj/gX\n"
            + "lAh1jCmE0nfyqpMu+QIDAQABo4HKMIHHMA8GA1UdEwEB/wQFMAMBAf8wgZQGA1Ud\n"
            + "IwSBjDCBiYAUY3Vobf2d4HtKZVE17BoxFoxEFQKhbqRsMGoxFDASBgNVBAMTC1Rv\n"
            + "cCBDQSBUZXN0MQ8wDQYDVQQLEwZUb3AgQ0ExDTALBgNVBAoTBFRlc3QxFDASBgNV\n"
            + "BAcTC1NhbiBBbnRvbmlvMQ4wDAYDVQQIEwVUZXhhczEMMAoGA1UEBhMDVVNBggEB\n"
            + "MB0GA1UdDgQWBBSByh8igm/1F+ZdZP5OBQ8gw+I32zANBgkqhkiG9w0BAQUFAAOB\n"
            + "gQCbb74q/rHQskgru1beKiO2grOooYhrnBfZP9saOyn8q1/9GNVgOxsRzIfCu2JU\n"
            + "CJlY0o/8YFKUioOD1nGVUQs+FI9Ui2VZFzYaDIcHJwvD+ynFoFObFeYVRh8kVMT9\n"
            + "gTDV9+xVOqql+ezaJ3XGikQPyPHA+japgpHimuvixAep4w==\n"
            + "-----END CERTIFICATE-----\n"
            + "-----BEGIN CERTIFICATE-----\n"
            + "MIICfDCCAeWgAwIBAgIBATANBgkqhkiG9w0BAQUFADBqMRQwEgYDVQQDEwtUb3Ag\n"
            + "Q0EgVGVzdDEPMA0GA1UECxMGVG9wIENBMQ0wCwYDVQQKEwRUZXN0MRQwEgYDVQQH\n"
            + "EwtTYW4gQW50b25pbzEOMAwGA1UECBMFVGV4YXMxDDAKBgNVBAYTA1VTQTAeFw0x\n"
            + "MjA0MTgyMDUwMTZaFw0yMzA3MDYyMDUwMTZaMGoxFDASBgNVBAMTC1RvcCBDQSBU\n"
            + "ZXN0MQ8wDQYDVQQLEwZUb3AgQ0ExDTALBgNVBAoTBFRlc3QxFDASBgNVBAcTC1Nh\n"
            + "biBBbnRvbmlvMQ4wDAYDVQQIEwVUZXhhczEMMAoGA1UEBhMDVVNBMIGfMA0GCSqG\n"
            + "SIb3DQEBAQUAA4GNADCBiQKBgQC55/X9PEfTjdH9hwG5Ka8CNgHwz3AkAb0BOSUm\n"
            + "8e0rGawIcJNwHEN/Lhv8pjsx0u7WfUAoEaJu2EdLkC1tJgIi98TEAjDDBiArbYsK\n"
            + "QFJn+LdO6jpNbbzEeSP0MZn0f5G3Vl2UFncsiCQq32mJqQ0cD2ZN6+16ileEscvW\n"
            + "p0jiXwIDAQABozIwMDAPBgNVHRMBAf8EBTADAQH/MB0GA1UdDgQWBBRjdWht/Z3g\n"
            + "e0plUTXsGjEWjEQVAjANBgkqhkiG9w0BAQUFAAOBgQBKh7/4GOO6i/Id5sXDsJsg\n"
            + "h9QI4hpwHdnF9tZLjKQI33ZT5pM+IWEH0KF7OLovNd+vJG0K//bahaSrIuqWFrzs\n"
            + "LeZnkJImpkWH4AoCHGtVRs9P4hJpgSLou/bgM2kw/gh5QK5ZyJpTBNCpbmzXv32z\n"
            + "8+ZxXJuXPBYk2O1RnJKCjA==\n"
            + "-----END CERTIFICATE-----\n";

    static {
        zeusUtils = new ZeusUtils();
    }

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

            when(serviceStubs.getPoolBinding().getLoadBalancingAlgorithm(Matchers.<String[]>any())).thenReturn(new PoolLoadBalancingAlgorithm[]{PoolLoadBalancingAlgorithm.wroundrobin});
            when(serviceStubs.getVirtualServerBinding().getRules(Matchers.<String[]>any())).thenReturn(new VirtualServerRule[][]{{}});
            when(serviceStubs.getVirtualServerBinding().getListenOnAllAddresses(Matchers.<String[]>any())).thenReturn(new boolean[]{false});
            when(serviceStubs.getVirtualServerBinding().getProtocol(Matchers.<String[]>any())).thenReturn(new VirtualServerProtocol[]{VirtualServerProtocol.fromValue(VirtualServerProtocol._http)});
            when(serviceStubs.getVirtualServerBinding().getVirtualServerNames()).thenReturn(new String[]{});
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
            node1.setCondition(DRAINING);
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
            lb.setHttpsRedirect(false);
            lb.setNodes(nodeList);
            lb.setLoadBalancerJoinVipSet(vipList);
        }

        @Test
        public void shouldRunInOrderWhenCreatingASimpleLoadBalancer() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
            InOrder inOrder = inOrder(poolStub, vsStub, trafficIpGroupStub, ruleStub, rateStub);
            adapterSpy.createLoadBalancer(dummyConfig, lb);
            inOrder.verify(poolStub).addPool(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
            inOrder.verify(poolStub).setLoadBalancingAlgorithm(Matchers.<String[]>anyObject(), Matchers.<PoolLoadBalancingAlgorithm[]>anyObject());
            inOrder.verify(poolStub).setDisabledNodes(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
            inOrder.verify(poolStub).setDrainingNodes(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
            inOrder.verify(poolStub).setNodesWeightings(Matchers.<String[]>anyObject(), Matchers.<PoolWeightingsDefinition[][]>anyObject());
            inOrder.verify(vsStub).addVirtualServer(Matchers.<String[]>anyObject(), Matchers.<VirtualServerBasicInfo[]>anyObject());
            inOrder.verify(trafficIpGroupStub).addTrafficIPGroup(Matchers.<String[]>anyObject(), Matchers.<TrafficIPGroupsDetails[]>anyObject());
            inOrder.verify(vsStub).setListenTrafficIPGroups(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
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
            inOrder.verify(poolStub).setLoadBalancingAlgorithm(Matchers.<String[]>anyObject(), Matchers.<PoolLoadBalancingAlgorithm[]>anyObject());
            inOrder.verify(poolStub).setDisabledNodes(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
            inOrder.verify(poolStub).setDrainingNodes(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
            inOrder.verify(poolStub).setNodesWeightings(Matchers.<String[]>anyObject(), Matchers.<PoolWeightingsDefinition[][]>anyObject());
            inOrder.verify(vsStub).addVirtualServer(Matchers.<String[]>anyObject(), Matchers.<VirtualServerBasicInfo[]>anyObject());
            inOrder.verify(trafficIpGroupStub).addTrafficIPGroup(Matchers.<String[]>anyObject(), Matchers.<TrafficIPGroupsDetails[]>anyObject());
            inOrder.verify(vsStub).setListenTrafficIPGroups(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
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
            inOrder.verify(vsStub).deleteVirtualServer(Matchers.<String[]>anyObject());
            inOrder.verify(poolStub).deletePool(Matchers.<String[]>anyObject());
            inOrder.verify(protectionStub).deleteProtection(Matchers.<String[]>anyObject());
            inOrder.verify(poolStub).removeMonitors(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
            inOrder.verify(monitorStub).deleteMonitors(Matchers.<String[]>anyObject());
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

    public static class WhenVerifyingSSLTermination {
        //TODO: move...

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
        private CatalogSSLCertificatesBindingStub certificateCatalogService;
        private LoadBalancer lb;
        private static final String ZXTM_USERNAME = "mocked_username";
        private static final String ZXTM_PASSWORD = "mocked_password";
        private static final String ZXTM_ENDPOINT_URI = "https://mock.endpoint.uri:9090/soap";
        private static final String TARGET_HOST = "ztm-n01.mock.endpoint.uri";
        private static final String FAILOVER_HOST_1 = "ztm-n03.mock.endpoint.uri";
        private static final String FAILOVER_HOST_2 = "ztm-n04.mock.endpoint.uri";
        private SslTermination sslTermination;

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
            node1.setCondition(DRAINING);
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

        @Before
        public void setUpClass() throws Exception {
            List<String> targetFailoverHosts = new ArrayList<String>();
            targetFailoverHosts.add(FAILOVER_HOST_1);
            targetFailoverHosts.add(FAILOVER_HOST_2);
            Host soapEndpointHost = new Host();
            soapEndpointHost.setEndpoint(ZXTM_ENDPOINT_URI);
            Host trafficManagerHost = new Host();
            trafficManagerHost.setTrafficManagerName(TARGET_HOST);
            dummyConfig = new LoadBalancerEndpointConfiguration(soapEndpointHost, ZXTM_USERNAME, ZXTM_PASSWORD, trafficManagerHost, targetFailoverHosts);
            RsaConst.init();
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
            certificateCatalogService = mock(CatalogSSLCertificatesBindingStub.class);

            when(serviceStubs.getPoolBinding()).thenReturn(poolStub);
            when(serviceStubs.getVirtualServerBinding()).thenReturn(vsStub);
            when(serviceStubs.getTrafficIpGroupBinding()).thenReturn(trafficIpGroupStub);
            when(serviceStubs.getProtectionBinding()).thenReturn(protectionStub);
            when(serviceStubs.getPersistenceBinding()).thenReturn(persistenceStub);
            when(serviceStubs.getMonitorBinding()).thenReturn(monitorStub);
            when(serviceStubs.getZxtmRuleCatalogService()).thenReturn(ruleStub);
            when(serviceStubs.getZxtmRateCatalogService()).thenReturn(rateStub);
            when(serviceStubs.getZxtmCatalogSSLCertificatesBinding()).thenReturn(certificateCatalogService);

            when(serviceStubs.getPoolBinding().getLoadBalancingAlgorithm(Matchers.<String[]>any())).thenReturn(new PoolLoadBalancingAlgorithm[]{PoolLoadBalancingAlgorithm.wroundrobin});
            when(serviceStubs.getVirtualServerBinding().getRules(Matchers.<String[]>any())).thenReturn(new VirtualServerRule[][]{{}});
            when(serviceStubs.getVirtualServerBinding().getListenOnAllAddresses(Matchers.<String[]>any())).thenReturn(new boolean[]{false});
            when(serviceStubs.getVirtualServerBinding().getProtocol(Matchers.<String[]>any())).thenReturn(new VirtualServerProtocol[]{VirtualServerProtocol.fromValue(VirtualServerProtocol._http)});
            when(serviceStubs.getVirtualServerBinding().getVirtualServerNames()).thenReturn(new String[]{});
            when(serviceStubs.getVirtualServerBinding().getPort(Matchers.<String[]>any())).thenReturn(new UnsignedInt[]{new UnsignedInt(80)});
            when(serviceStubs.getZxtmRuleCatalogService().getRuleNames()).thenReturn(new String[]{});

        }

        @Test
        public void WhenAddingSslTermination() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
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

            SslTermination sslTermination = new SslTermination();
            sslTermination.setIntermediateCertificate(testChain);
            sslTermination.setCertificate(testCrt);
            sslTermination.setPrivatekey(testpkcs8);
            sslTermination.setEnabled(true);
            sslTermination.setSecurePort(443);
            sslTermination.setSecureTrafficOnly(false);

            lb.setSslTermination(sslTermination);
            ZeusCrtFile zcf = zeusUtils.buildZeusCrtFileLbassValidation(testpkcs8, testCrt, testChain);
            String pkcs1 = zcf.getPrivate_key();
            String errors = StringUtils.joinString(zcf.getFatalErrorList(), ",");
            Assert.assertFalse(errors, zcf.hasFatalErrors());
            ZeusSslTermination zeusSslTermination = new ZeusSslTermination();
            zeusSslTermination.setCertIntermediateCert(testChain);
            zeusSslTermination.setSslTermination(sslTermination);

            InOrder inOrder = inOrder(vsStub, vsStub, vsStub, protectionStub, protectionStub, certificateCatalogService, vsStub, vsStub);
            adapterSpy.updateSslTermination(dummyConfig, lb, zeusSslTermination);
            inOrder.verify(vsStub).addVirtualServer(Matchers.<String[]>anyObject(), Matchers.<VirtualServerBasicInfo[]>anyObject());
            inOrder.verify(vsStub).setProtection(Matchers.<String[]>anyObject(), Matchers.<String[]>anyObject());
            inOrder.verify(vsStub).setLogEnabled(Matchers.<String[]>anyObject(), Matchers.<boolean[]>anyObject());
            inOrder.verify(protectionStub).setAllowedAddresses(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
            inOrder.verify(protectionStub).setBannedAddresses(Matchers.<String[]>anyObject(), Matchers.<String[][]>anyObject());
            inOrder.verify(certificateCatalogService).importCertificate(Matchers.<String[]>anyObject(), Matchers.<CertificateFiles[]>anyObject());
            inOrder.verify(vsStub).setSSLCertificate(Matchers.<String[]>anyObject(), Matchers.<String[]>anyObject());
            inOrder.verify(vsStub).setSSLDecrypt(Matchers.<String[]>anyObject(), Matchers.<boolean[]>anyObject());
        }

        @Test
        public void WhenDeletingSslTermination() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
            SslTermination sslTermination = new SslTermination();
            sslTermination.setIntermediateCertificate("iCert");
            sslTermination.setCertificate("cert");
            sslTermination.setPrivatekey("aPrivateKey");
            sslTermination.setEnabled(true);
            sslTermination.setSecurePort(443);
            sslTermination.setSecureTrafficOnly(false);

            lb.setSslTermination(sslTermination);

            InOrder inOrder = inOrder(vsStub, vsStub, certificateCatalogService, vsStub);
            adapterSpy.removeSslTermination(dummyConfig, lb);
            inOrder.verify(vsStub).setSSLDecrypt(Matchers.<String[]>anyObject(), Matchers.<boolean[]>anyObject());
            inOrder.verify(vsStub).setSSLCertificate(Matchers.<String[]>anyObject(), Matchers.<String[]>anyObject());
            inOrder.verify(certificateCatalogService).deleteCertificate(Matchers.<String[]>anyObject());
            inOrder.verify(vsStub).deleteVirtualServer(Matchers.<String[]>anyObject());
        }
    }
}
