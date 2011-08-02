package org.openstack.atlas.adapter.itest;

import org.apache.axis.AxisFault;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.adapter.zxtm.ZxtmAdapterImpl;
import org.openstack.atlas.adapter.zxtm.ZxtmServiceStubs;
import org.openstack.atlas.service.domain.entities.Host;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Ignore
public class ZxtmAdapterStagingIntegrationTest {
    private static final String ZXTM_USERNAME = "user_name";
    private static final String ZXTM_PASSWORD = "user_password";
    private static final String ZXTM_ENDPOINT_URI = "https://zeus-endpoint:9090/soap";
    private static final String TARGET_HOST = "ztm-n03.staging1.test.com";
    private static final String FAILOVER_HOST_1 = "ztm-n02.staging1.test.com";

    private ReverseProxyLoadBalancerAdapter zxtmAdapter;
    private LoadBalancerEndpointConfiguration config;

    public ZxtmAdapterStagingIntegrationTest() throws MalformedURLException {
        zxtmAdapter = new ZxtmAdapterImpl();
        setupEndpointConfiguration();
    }

    private void setupEndpointConfiguration() throws MalformedURLException {
        List<String> targetFailoverHosts = new ArrayList<String>();
        targetFailoverHosts.add(FAILOVER_HOST_1);
        Host soapEndpointHost = new Host();
        soapEndpointHost.setEndpoint(ZXTM_ENDPOINT_URI);
        Host trafficManagerHost = new Host();
        trafficManagerHost.setHostName(TARGET_HOST);
        this.config = new LoadBalancerEndpointConfiguration(soapEndpointHost, ZXTM_USERNAME, ZXTM_PASSWORD, trafficManagerHost, targetFailoverHosts);

        // This is the default directory where the logs will go.
        this.config.setLogFileLocation("/opt/zeus/zxtm/log/access_log");
    }

    private ZxtmServiceStubs getServiceStubs() throws AxisFault {
        return ZxtmServiceStubs.getServiceStubs(config.getEndpointUrl(), config.getUsername(), config.getPassword());
    }

    @Test
    public void shouldBeValidApiVersion() {
        String ZEUS_API_VERSION = "7.1";
        try {
            Assert.assertEquals(ZEUS_API_VERSION, getServiceStubs().getSystemMachineInfoBinding().getProductVersion());
        } catch (RemoteException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void getStatsSystemLoadBalancerNames() throws Exception {
        List<String> loadBalancerNames = zxtmAdapter.getStatsSystemLoadBalancerNames(config);
        System.out.println(loadBalancerNames.size() + " loadbalancers on host machine.");
        for (String loadBalancerName : loadBalancerNames) {
            System.out.println(loadBalancerName);
        }
    }

    @Test
    public void getLoadBalancerCurrentConnections() throws Exception {
        List<String> loadBalancerNames = zxtmAdapter.getStatsSystemLoadBalancerNames(config);
        Map<String, Integer> concurrentConnections = zxtmAdapter.getLoadBalancerCurrentConnections(config, loadBalancerNames);
        System.out.println("Listing concurrent connections...");
        for (String loadBalancerName : concurrentConnections.keySet()) {
            System.out.println(String.format("LB Name: %s, Concurrent Connections: %d", loadBalancerName, concurrentConnections.get(loadBalancerName)));
        }
    }

    @Test
    public void getTotalCurrentConnectionsForHost() throws Exception {
        int concurrentConnections = zxtmAdapter.getTotalCurrentConnectionsForHost(config);
        System.out.println("Listing total current connections..." + concurrentConnections);
    }

    @Test
    public void getLoadBalancerBytesIn() throws Exception {
        List<String> loadBalancerNames = zxtmAdapter.getStatsSystemLoadBalancerNames(config);
        Map<String, Long> bandwidthBytesIn = zxtmAdapter.getLoadBalancerBytesIn(config, loadBalancerNames);
        System.out.println("Listing bandwidth bytes in...");
        for (String loadBalancerName : bandwidthBytesIn.keySet()) {
            System.out.println(String.format("LB Name: %s, Bandwidth Bytes In: %d", loadBalancerName, bandwidthBytesIn.get(loadBalancerName)));
        }
    }

    @Test
    public void getLoadBalancerBytesOut() throws Exception {
        List<String> loadBalancerNames = zxtmAdapter.getStatsSystemLoadBalancerNames(config);
        Map<String, Long> bandwidthBytesOut = zxtmAdapter.getLoadBalancerBytesIn(config, loadBalancerNames);
        System.out.println("Listing bandwidth bytes out...");
        for (String loadBalancerName : bandwidthBytesOut.keySet()) {
            System.out.println(String.format("LB Name: %s, Bandwidth Bytes Out: %d", loadBalancerName, bandwidthBytesOut.get(loadBalancerName)));
        }
    }

    @Test
    public void getHostBytesIn() throws RemoteException {
        long bytesIn = zxtmAdapter.getHostBytesIn(config);
        System.out.println(String.format("Host Name: %s, Bandwidth Bytes In: %d", config.getHostName(), bytesIn));
    }

    @Test
    public void getHostBytesOut() throws RemoteException {
        long bytesOut = zxtmAdapter.getHostBytesOut(config);
        System.out.println(String.format("Host Name: %s, Bandwidth Bytes Out: %d", config.getHostName(), bytesOut));
    }

    @Ignore
    @Test
    public void badAppleTest() throws Exception {
        List<String> loadBalancerNames = zxtmAdapter.getStatsSystemLoadBalancerNames(config);
        for (String loadBalancerName : loadBalancerNames) {
            List<String> singleItemList = new ArrayList<String>();
            singleItemList.add(loadBalancerName);
            try {
                zxtmAdapter.getLoadBalancerCurrentConnections(config, singleItemList);
            } catch (AxisFault af) {
                System.out.println(String.format("The bad apple is...'%s'!", loadBalancerName));
            }
        }
    }
}
