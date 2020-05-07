package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIpBlocks;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.services.ClusterService;

import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class ClusterResourceTest {
    static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

    public static class whenCheckingHealth {
        private ClusterResource resource;
        private OperationResponse response;
        private Configuration configuration;
        private ClusterService clusterService;
        private ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;
        private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
        private List<Host> hosts;

        @Before
        public void setUp() {
            reverseProxyLoadBalancerService = mock(ReverseProxyLoadBalancerService.class);
            reverseProxyLoadBalancerVTMService = mock(ReverseProxyLoadBalancerVTMService.class);
            clusterService = mock(ClusterService.class);

            resource = new ClusterResource();
            resource.setMockitoAuth(true);
            resource.setReverseProxyLoadBalancerService(reverseProxyLoadBalancerService);
            resource.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
            resource.setClusterService(clusterService);
            response = new OperationResponse();
            response.setExecutedOkay(true);
            configuration = mock(Configuration.class);
            resource.setConfiguration(configuration);
            resource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());

            hosts = new ArrayList<>();
            Host host = new Host();
            host.setId(2);
            hosts.add(host);

            when(clusterService.getHosts(anyInt())).thenReturn(hosts);
            when(configuration.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        }

        @Test
        public void addVirtualIpBlocksShouldCallVTMService() throws Exception {
            when(reverseProxyLoadBalancerVTMService.getSubnetMappings(any())).thenReturn(new Hostssubnet());

            Response resp = resource.addVirtualIpBlocks(new VirtualIpBlocks());
            Assert.assertEquals(200, resp.getStatus());
            verify(reverseProxyLoadBalancerService, times(0)).getSubnetMappings(any());
            verify(reverseProxyLoadBalancerVTMService, times(1)).getSubnetMappings(any());
        }

        @Test
        public void addVirtualIpBlocksShouldCallSoapService() throws Exception {
            when(configuration.getString(Matchers.<ConfigurationKey>any())).thenReturn("NOTREST");

            when(reverseProxyLoadBalancerService.getSubnetMappings(any())).thenReturn(new Hostssubnet());

            Response resp = resource.addVirtualIpBlocks(new VirtualIpBlocks());
            Assert.assertEquals(200, resp.getStatus());
            verify(reverseProxyLoadBalancerService, times(1)).getSubnetMappings(any());
            verify(reverseProxyLoadBalancerVTMService, times(0)).getSubnetMappings(any());
        }
    }
}
