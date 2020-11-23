package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.service.domain.operations.OperationResponse;

import javax.ws.rs.core.Response;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@RunWith(Enclosed.class)
public class ManagementResourceTest {
    static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

    public static class whenCheckingHealth {
        private ManagementResource resource;
        private OperationResponse response;
        private Configuration configuration;
        private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;

        @Before
        public void setUp() {
            reverseProxyLoadBalancerVTMService = mock(ReverseProxyLoadBalancerVTMService.class);
            resource = new ManagementResource();
            resource.setMockitoAuth(true);
            resource.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
            response = new OperationResponse();
            response.setExecutedOkay(true);
            configuration = mock(Configuration.class);
            resource.setConfiguration(configuration);
            resource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
            when(configuration.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        }

        @Test
        public void getGlobalCiphersShouldCallVTMService() throws Exception {
            when(reverseProxyLoadBalancerVTMService.getSsl3Ciphers()).thenReturn("Cipher1");

            Response resp = resource.getGlobalCiphers();
            Assert.assertEquals(200, resp.getStatus());
            verify(reverseProxyLoadBalancerVTMService, times(1)).getSsl3Ciphers();
        }

    }
}
