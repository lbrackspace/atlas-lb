package org.openstack.atlas.api.integration.threads;


import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerStmAdapter;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerVTMAdapter;
import org.openstack.atlas.adapter.vtm.VTMAdapterResources;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerServiceVTMImpl;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

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
        @Mock
        ReverseProxyLoadBalancerStmAdapter reverseProxyLoadBalancerStmAdapter;
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
            reverseProxyLoadBalancerServiceVTM.setReverseProxyLoadBalancerStmAdapter(reverseProxyLoadBalancerStmAdapter);

            host.setName("testHost");
            host.setId(1);
            host.setEndpoint("testEndpoint.com");
            host.setHostStatus(HostStatus.ACTIVE);
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
            verify(reverseProxyLoadBalancerStmAdapter, times(0)).getSsl3Ciphers(any(LoadBalancerEndpointConfiguration.class));

        }

        @Test
        public void getSSL3CiphersForLBShouldUseSTMAdapter() throws Exception{
            soapEndPointHost.setRestEndpoint("https://test/api/tm/3.4/config/active/");
            reverseProxyLoadBalancerServiceVTM.getSsl3CiphersForLB(loadBalancerId);
            verify(reverseProxyLoadBalancerVTMAdapter, times(0)).getSsl3Ciphers(any(LoadBalancerEndpointConfiguration.class));
            verify(reverseProxyLoadBalancerStmAdapter, times(1)).getSsl3Ciphers(any(LoadBalancerEndpointConfiguration.class));
        }


    }







}
