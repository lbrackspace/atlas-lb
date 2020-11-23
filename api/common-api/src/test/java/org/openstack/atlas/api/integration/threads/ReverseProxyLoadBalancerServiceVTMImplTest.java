package org.openstack.atlas.api.integration.threads;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerVTMAdapter;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerServiceVTMImpl;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.services.HostService;
import org.openstack.atlas.service.domain.services.impl.LoadBalancerServiceImpl;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class ReverseProxyLoadBalancerServiceVTMImplTest {


    public static class getSsl3Ciphers{

        @Mock
        HostService hostService;
        @Mock
        Configuration configuration;
        @Mock
        LoadBalancerRepository loadBalancerRepository;
        @Mock
        LoadBalancerServiceImpl loadBalancerService;
        @Mock
        ReverseProxyLoadBalancerVTMAdapter reverseProxyLoadBalancerVTMAdapter;
        ReverseProxyLoadBalancerServiceVTMImpl reverseProxyLoadBalancerServiceVTM;

        Cluster cluster;
        Host failOverHost;
        Host soapEndPointHost;
        LoadBalancer loadBalancer;
        Host host;
        List<String> failOverHostNames;
        List<Host> failoverHosts;
        String logFileLocation;
        Integer loadBalancerId;

        @Before
        public void standUp() throws EntityNotFoundException, RemoteException, RollBackException, VTMRestClientObjectNotFoundException, VTMRestClientException, InsufficientRequestException {
            MockitoAnnotations.initMocks(this);

            loadBalancerService = new LoadBalancerServiceImpl();
            loadBalancer = new LoadBalancer();
            host = new Host();
            cluster = new Cluster();
            soapEndPointHost = new Host();
            failOverHost = new Host();
            failOverHostNames = new ArrayList<>();
            failoverHosts = new ArrayList<>();

            reverseProxyLoadBalancerServiceVTM = new ReverseProxyLoadBalancerServiceVTMImpl();
            reverseProxyLoadBalancerServiceVTM.setLoadBalancerService(loadBalancerService);
            reverseProxyLoadBalancerServiceVTM.setHostService(hostService);
            reverseProxyLoadBalancerServiceVTM.setConfiguration(configuration);
            reverseProxyLoadBalancerServiceVTM.setReverseProxyLoadBalancerVTMAdapter(reverseProxyLoadBalancerVTMAdapter);

            host.setName("testHost");
            host.setId(1);
            host.setEndpoint("testEndpoint.com");
            host.setHostStatus(HostStatus.ACTIVE);
            host.setTrafficManagerName("n1.com");
            host.setZone(Zone.A);
            cluster.setId(1);
            cluster.setName("testCluster");
            loadBalancer.setHost(host);
            loadBalancer.setName("testLB");
            loadBalancer.setId(1);
            loadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
            soapEndPointHost.setId(2);
            soapEndPointHost.setEndpoint("https://soapEndPointHostTest/soap");
            failOverHost.setEndpoint("failover.com/soap");
            failOverHost.setTrafficManagerName("nf1.com");
            failOverHost.setZone(Zone.A);
            loadBalancerId = 1;
            failOverHostNames.add("nf1.com");
            failoverHosts.add(failOverHost);
            logFileLocation = "test";
            host.setCluster(cluster);
            loadBalancerService.setLoadBalancerRepository(loadBalancerRepository);
            loadBalancerRepository.setLoadBalancerAttrs(loadBalancer);
            loadBalancerService.setLoadBalancerAttrs(loadBalancer);

            when(loadBalancerRepository.getById(anyInt())).thenReturn(loadBalancer);
            when(hostService.getRestEndPointHost(anyInt())).thenReturn(soapEndPointHost);
            when(hostService.getFailoverHostNames(anyInt())).thenReturn(failOverHostNames);
            when(hostService.getFailoverHosts(anyInt())).thenReturn(failoverHosts);
            when(configuration.getString(anyString())).thenReturn(logFileLocation);
            when(reverseProxyLoadBalancerVTMAdapter.getSsl3Ciphers(any(LoadBalancerEndpointConfiguration.class))).thenReturn("cipher list");
        }

        @Test
        public void getSSL3CiphersForLBShouldUseVTMAdapter() throws Exception{
            soapEndPointHost.setRestEndpoint("https://test/api/tm/7.0/config/active/");
            reverseProxyLoadBalancerServiceVTM.getSsl3CiphersForLB(loadBalancerId);
            verify(reverseProxyLoadBalancerVTMAdapter, times(1)).getSsl3Ciphers(any(LoadBalancerEndpointConfiguration.class));

        }
    }

    public static class subnetMappings{

        @Mock
        HostService hostService;
        @Mock
        Configuration configuration;
        @Mock
        LoadBalancerRepository loadBalancerRepository;
        @Mock
        LoadBalancerServiceImpl loadBalancerService;
        @Mock
        ReverseProxyLoadBalancerVTMAdapter reverseProxyLoadBalancerVTMAdapter;
        @Spy
        ReverseProxyLoadBalancerServiceVTMImpl reverseProxyLoadBalancerServiceVTM;

        Cluster cluster;
        Host failOverHost;
        Host soapEndPointHost;
        LoadBalancer loadBalancer;
        Host host;
        List<String> failOverHostNames;
        List<Host> failoverHosts;
        String logFileLocation;
        Integer loadBalancerId;

        @Before
        public void standUp() throws EntityNotFoundException, RemoteException, RollBackException, VTMRestClientObjectNotFoundException, VTMRestClientException, InsufficientRequestException {
            MockitoAnnotations.initMocks(this);

            loadBalancerService = new LoadBalancerServiceImpl();
            loadBalancer = new LoadBalancer();
            host = new Host();
            cluster = new Cluster();
            soapEndPointHost = new Host();
            failOverHost = new Host();
            failOverHostNames = new ArrayList<>();
            failoverHosts = new ArrayList<>();

            reverseProxyLoadBalancerServiceVTM = spy(new ReverseProxyLoadBalancerServiceVTMImpl());
            reverseProxyLoadBalancerServiceVTM.setLoadBalancerService(loadBalancerService);
            reverseProxyLoadBalancerServiceVTM.setHostService(hostService);
            reverseProxyLoadBalancerServiceVTM.setConfiguration(configuration);
            reverseProxyLoadBalancerServiceVTM.setReverseProxyLoadBalancerVTMAdapter(reverseProxyLoadBalancerVTMAdapter);

            host.setName("testHost");
            host.setId(1);
            host.setEndpoint("https://test.com:3030/soap");
            host.setRestEndpoint("https://test/api/tm/7.0/config/active/");
            host.setHostStatus(HostStatus.ACTIVE);
            host.setTrafficManagerName("n1.com");
            host.setZone(Zone.A);
            cluster.setId(1);
            cluster.setName("testCluster");
            loadBalancer.setHost(host);
            loadBalancer.setName("testLB");
            loadBalancer.setId(1);
            loadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
            soapEndPointHost.setId(2);
            soapEndPointHost.setEndpoint("https://soapEndPointHostTest/soap");
            failOverHost.setEndpoint("failover.com/soap");
            failOverHost.setTrafficManagerName("nf1.com");
            failOverHost.setZone(Zone.A);
            loadBalancerId = 1;
            failOverHostNames.add("nf1.com");
            failoverHosts.add(failOverHost);
            logFileLocation = "test";
            host.setCluster(cluster);
            loadBalancerService.setLoadBalancerRepository(loadBalancerRepository);
            loadBalancerRepository.setLoadBalancerAttrs(loadBalancer);
            loadBalancerService.setLoadBalancerAttrs(loadBalancer);

            when(loadBalancerRepository.getById(anyInt())).thenReturn(loadBalancer);
            when(hostService.getRestEndPointHost(anyInt())).thenReturn(soapEndPointHost);
            when(hostService.getFailoverHostNames(anyInt())).thenReturn(failOverHostNames);
            when(hostService.getFailoverHosts(anyInt())).thenReturn(failoverHosts);
            when(configuration.getString(anyString())).thenReturn(logFileLocation);
        }

        @Test
        public void setSubnetMappingsShouldUseVTMAdapter() throws Exception{
            soapEndPointHost.setRestEndpoint("https://test/api/tm/7.0/config/active/");
            Hostssubnet hsubnet = new Hostssubnet();
            reverseProxyLoadBalancerServiceVTM.setSubnetMappings(host, hsubnet);
            verify(reverseProxyLoadBalancerServiceVTM, times(1)).getConfigHost(host);
            verify(reverseProxyLoadBalancerVTMAdapter, times(1)).setSubnetMappings(any(LoadBalancerEndpointConfiguration.class), any());

        }

    }

    public static class WhenRetrievingHostConfigs {

        @Mock
        HostService hostService;
        @Mock
        Configuration configuration;
        @Mock
        LoadBalancerRepository loadBalancerRepository;
        @Mock
        LoadBalancerServiceImpl loadBalancerService;
        @Mock
        ReverseProxyLoadBalancerVTMAdapter reverseProxyLoadBalancerVTMAdapter;
        ReverseProxyLoadBalancerServiceVTMImpl reverseProxyLoadBalancerServiceVTM;

        Cluster cluster;
        Host failOverHost;
        Host soapEndPointHost;
        LoadBalancer loadBalancer;
        Host host;
        List<String> failOverHostNames;
        List<Host> failoverHosts;
        String logFileLocation;
        Integer loadBalancerId;

        @Before
        public void standUp() throws EntityNotFoundException, RemoteException, RollBackException, VTMRestClientObjectNotFoundException, VTMRestClientException, InsufficientRequestException {
            MockitoAnnotations.initMocks(this);

            loadBalancerService = new LoadBalancerServiceImpl();
            loadBalancer = new LoadBalancer();
            host = new Host();
            cluster = new Cluster();
            soapEndPointHost = new Host();
            failOverHost = new Host();
            failOverHostNames = new ArrayList<>();
            failoverHosts = new ArrayList<>();

            reverseProxyLoadBalancerServiceVTM = new ReverseProxyLoadBalancerServiceVTMImpl();
            reverseProxyLoadBalancerServiceVTM.setLoadBalancerService(loadBalancerService);
            reverseProxyLoadBalancerServiceVTM.setHostService(hostService);
            reverseProxyLoadBalancerServiceVTM.setConfiguration(configuration);
            reverseProxyLoadBalancerServiceVTM.setReverseProxyLoadBalancerVTMAdapter(reverseProxyLoadBalancerVTMAdapter);

            host.setName("testHost");
            host.setId(1);
            host.setEndpoint("https://endPointHostTest/soap");
            host.setRestEndpoint("https://restEndPointHostTest/config/active");
            host.setHostStatus(HostStatus.ACTIVE);
            host.setZone(Zone.A);
            host.setTrafficManagerName("n1.com");
            cluster.setId(1);
            cluster.setName("testCluster");
            host.setCluster(cluster);
            loadBalancer.setHost(host);
            loadBalancer.setName("testLB");
            loadBalancer.setId(1);
            loadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
            soapEndPointHost.setId(2);
            soapEndPointHost.setEndpoint("https://soapEndPointHostTest/soap");
            Host failOverHost2 = new Host();
            failOverHost.setEndpoint("failover.com/soap");
            failOverHost.setZone(Zone.A);
            failOverHost.setTrafficManagerName("nf1.com");
            failOverHost2.setZone(Zone.B);
            failOverHost2.setEndpoint("failover.com/soap");
            failOverHost2.setTrafficManagerName("nf2.com");
            loadBalancerId = 1;
            failOverHostNames.add("test.com/soap");
            failoverHosts.add(failOverHost);
            failoverHosts.add(failOverHost2);
            logFileLocation = "test";
            host.setCluster(cluster);
            loadBalancerService.setLoadBalancerRepository(loadBalancerRepository);
            loadBalancerRepository.setLoadBalancerAttrs(loadBalancer);
            loadBalancerService.setLoadBalancerAttrs(loadBalancer);

            when(loadBalancerRepository.getById(anyInt())).thenReturn(loadBalancer);
            when(hostService.getRestEndPointHost(anyInt())).thenReturn(host);
            when(hostService.getFailoverHostNames(anyInt())).thenReturn(failOverHostNames);
            when(hostService.getFailoverHosts(anyInt())).thenReturn(failoverHosts);
            when(configuration.getString(anyString())).thenReturn(logFileLocation);
        }

        @Test
        public void getConfigWithOneFailoverOnSameZone() throws Exception{
            LoadBalancerEndpointConfiguration lbEndpointConfig = reverseProxyLoadBalancerServiceVTM.getConfigHost(host);
            // Should use the specified host's endpoint
            verify(hostService, times(0)).getRestEndPointHost(cluster.getId());
            verify(hostService, times(1)).getFailoverHosts(cluster.getId());
            Assert.assertEquals(1, lbEndpointConfig.getFailoverTrafficManagerNames().size());
            Assert.assertTrue(lbEndpointConfig.getFailoverTrafficManagerNames().contains("nf1.com"));
            Assert.assertFalse(lbEndpointConfig.getFailoverTrafficManagerNames().contains("nf2.com"));

        }

        @Test
        public void getConfigWithTwoFailoversOnSameZone() throws Exception{
            Host failOverHost3 = new Host();
            failOverHost3.setEndpoint("failover3.com/soap");
            failOverHost3.setZone(Zone.A);
            failOverHost3.setTrafficManagerName("nf3.com");
            failoverHosts.add(failOverHost3);
            when(hostService.getFailoverHosts(anyInt())).thenReturn(failoverHosts);

            LoadBalancerEndpointConfiguration lbEndpointConfig = reverseProxyLoadBalancerServiceVTM.getConfigHost(host);
            // Should use the specified host's endpoint
            verify(hostService, times(0)).getRestEndPointHost(cluster.getId());
            verify(hostService, times(1)).getFailoverHosts(cluster.getId());
            Assert.assertEquals(2, lbEndpointConfig.getFailoverTrafficManagerNames().size());
            Assert.assertTrue(lbEndpointConfig.getFailoverTrafficManagerNames().contains("nf1.com"));
            Assert.assertTrue(lbEndpointConfig.getFailoverTrafficManagerNames().contains("nf3.com"));
            Assert.assertFalse(lbEndpointConfig.getFailoverTrafficManagerNames().contains("nf2.com"));

        }

        @Test
        public void getConfigbyLoadBalancerIdWithOneFailoverOnSameZone() throws Exception{
            LoadBalancerEndpointConfiguration lbEndpointConfig = reverseProxyLoadBalancerServiceVTM.getConfigbyLoadBalancerId(loadBalancerId);
            verify(hostService, times(1)).getRestEndPointHost(cluster.getId());
            verify(hostService, times(1)).getFailoverHosts(cluster.getId());
            Assert.assertEquals(1, lbEndpointConfig.getFailoverTrafficManagerNames().size());
            Assert.assertTrue(lbEndpointConfig.getFailoverTrafficManagerNames().contains("nf1.com"));
            Assert.assertFalse(lbEndpointConfig.getFailoverTrafficManagerNames().contains("nf2.com"));

        }

        @Test
        public void getConfigbyLoadBalancerIdWithTwoFailoversOnSameZone() throws Exception{
            Host failOverHost3 = new Host();
            failOverHost3.setEndpoint("failover3.com/soap");
            failOverHost3.setZone(Zone.A);
            failOverHost3.setTrafficManagerName("nf3.com");
            failoverHosts.add(failOverHost3);
            when(hostService.getFailoverHosts(anyInt())).thenReturn(failoverHosts);

            LoadBalancerEndpointConfiguration lbEndpointConfig = reverseProxyLoadBalancerServiceVTM.getConfigbyLoadBalancerId(loadBalancerId);
            verify(hostService, times(1)).getRestEndPointHost(cluster.getId());
            verify(hostService, times(1)).getFailoverHosts(cluster.getId());
            Assert.assertEquals(2, lbEndpointConfig.getFailoverTrafficManagerNames().size());
            Assert.assertTrue(lbEndpointConfig.getFailoverTrafficManagerNames().contains("nf1.com"));
            Assert.assertTrue(lbEndpointConfig.getFailoverTrafficManagerNames().contains("nf3.com"));
            Assert.assertFalse(lbEndpointConfig.getFailoverTrafficManagerNames().contains("nf2.com"));

        }
    }
}
