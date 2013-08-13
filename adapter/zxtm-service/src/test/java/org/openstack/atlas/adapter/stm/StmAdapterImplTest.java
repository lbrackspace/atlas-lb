package org.openstack.atlas.adapter.stm;

import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.helpers.StmAdapterImplTestHelper;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerStmAdapter;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@RunWith(Enclosed.class)
public class StmAdapterImplTest extends StmAdapterImplTestHelper {
    private static ReverseProxyLoadBalancerStmAdapter adapterSpy;
    private static StmAdapterResources resources;
    private static LoadBalancer loadBalancer;
    private static LoadBalancerEndpointConfiguration config;

    private static final String ZXTM_USERNAME = "mocked_username";
    private static final String ZXTM_PASSWORD = "mocked_password";
    private static final String ZXTM_ENDPOINT_URI = "https://mock.endpoint.uri:9090/soap";
    private static final String TARGET_HOST = "ztm-n01.mock.endpoint.uri";
    private static final String FAILOVER_HOST_1 = "ztm-n03.mock.endpoint.uri";
    private static final String FAILOVER_HOST_2 = "ztm-n04.mock.endpoint.uri";

    public static class WhenModifyingLoadbalancerResources {
        @Before
        public void standUp () {
            List<String> targetFailoverHosts = new ArrayList<String>();
            targetFailoverHosts.add(FAILOVER_HOST_1);
            targetFailoverHosts.add(FAILOVER_HOST_2);
            Host soapEndpointHost = new Host();
            soapEndpointHost.setEndpoint(ZXTM_ENDPOINT_URI);
            soapEndpointHost.setRestEndpoint(ZXTM_ENDPOINT_URI);
            Host trafficManagerHost = new Host();
            trafficManagerHost.setTrafficManagerName(TARGET_HOST);
            config = new LoadBalancerEndpointConfiguration(soapEndpointHost, ZXTM_USERNAME, ZXTM_PASSWORD, trafficManagerHost, targetFailoverHosts, "9070");

            adapterSpy = spy(new StmAdapterImpl());
            resources = mock(StmAdapterResources.class);

            loadBalancer = generateLoadBalancer();

        }

        @Test
        public void testCreateLoadBalancer() {
        }

        @After
        public void tearDown() {
        }
    }
}