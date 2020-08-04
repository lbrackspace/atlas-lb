package org.openstack.atlas.api.integration.threads;


import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerServiceImpl;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.services.HostService;
import org.openstack.atlas.service.domain.services.impl.LoadBalancerServiceImpl;


import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;

@RunWith(Enclosed.class)
public class ReverseProxyLoadBalancerServiceImplTest {

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
        ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter;
        ReverseProxyLoadBalancerServiceImpl reverseProxyLoadBalancerService;

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
        public void standUp() throws EntityNotFoundException, RemoteException {
            MockitoAnnotations.initMocks(this);

            loadBalancerService = new LoadBalancerServiceImpl();
            loadBalancer = new LoadBalancer();
            host = new Host();
            cluster = new Cluster();
            soapEndPointHost = new Host();
            failOverHost = new Host();
            failOverHostNames = new ArrayList<>();
            failoverHosts = new ArrayList<>();

            reverseProxyLoadBalancerService = new ReverseProxyLoadBalancerServiceImpl();
            reverseProxyLoadBalancerService.setLoadBalancerService(loadBalancerService);
            reverseProxyLoadBalancerService.setHostService(hostService);
            reverseProxyLoadBalancerService.setConfiguration(configuration);
            reverseProxyLoadBalancerService.setReverseProxyLoadBalancerAdapter(reverseProxyLoadBalancerAdapter);

            host.setName("testHost");
            host.setId(1);
            host.setEndpoint("testEndpoint.com");
            host.setHostStatus(HostStatus.ACTIVE);
            host.setRestEndpoint("https://test/api/tm/3.4/config/active/");
            cluster.setId(1);
            cluster.setName("testCluster");
            loadBalancer.setHost(host);
            loadBalancer.setName("testLB");
            loadBalancer.setId(1);
            loadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
            soapEndPointHost.setId(2);
            soapEndPointHost.setEndpoint("https://soapEndPointHostTest/soap");
            failOverHost.setEndpoint("failover.com/soap");
            loadBalancerId = 1;
            failOverHostNames.add("test.com/soap");
            failoverHosts.add(failOverHost);
            logFileLocation = "test";
            host.setCluster(cluster);
            loadBalancerService.setLoadBalancerRepository(loadBalancerRepository);
            loadBalancerRepository.setLoadBalancerAttrs(loadBalancer);
            loadBalancerService.setLoadBalancerAttrs(loadBalancer);
            soapEndPointHost.setRestEndpoint("https://test/api/tm/3.4/config/active/");

            when(loadBalancerRepository.getById(anyInt())).thenReturn(loadBalancer);
            when(hostService.getEndPointHost(anyInt())).thenReturn(soapEndPointHost);
            when(hostService.getFailoverHostNames(anyInt())).thenReturn(failOverHostNames);
            when(hostService.getFailoverHosts(anyInt())).thenReturn(failoverHosts);
            when(configuration.getString(anyString())).thenReturn(logFileLocation);
            when(reverseProxyLoadBalancerAdapter.getSsl3Ciphers(any(LoadBalancerEndpointConfiguration.class))).thenReturn("cipher list");
        }


        @Test
        public void getSSL3CiphersForLBShouldUseVTMAdapter() throws Exception{
            reverseProxyLoadBalancerService.getSsl3CiphersForLB(loadBalancerId);
            verify(reverseProxyLoadBalancerAdapter, times(1)).getSsl3Ciphers(any(LoadBalancerEndpointConfiguration.class));

        }


    }


}
