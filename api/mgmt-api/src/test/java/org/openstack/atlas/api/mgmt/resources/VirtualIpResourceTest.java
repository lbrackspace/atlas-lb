package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Port;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Ports;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.services.VirtualIpService;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class VirtualIpResourceTest {
    static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";
    public static class WhenGettingALoadBalancerVirtualIps {

        private ManagementAsyncService asyncService;
        private VirtualIpRepository virtualIpRepository;
        private VirtualIpsResource virtualIpsResource;
        private OperationResponse operationResponse;

        @Before
        public void setUp() {
            virtualIpsResource = new VirtualIpsResource();
            virtualIpsResource.setMockitoAuth(true);
            virtualIpRepository = mock(VirtualIpRepository.class);
            virtualIpsResource.setVipRepository(virtualIpRepository);
            asyncService = mock(ManagementAsyncService.class);
            virtualIpsResource.setManagementAsyncService(asyncService);
            virtualIpsResource.setId(12);
            operationResponse = new OperationResponse();
            virtualIpsResource.setDozerMapper( DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
        }

        @Test(expected = Exception.class)
        public void shouldProduceExceptionWhenEntitymanagerFails() throws Exception {
            doThrow(Exception.class).when(virtualIpRepository.getEntityManager().createQuery((String) any()));
            Response response = virtualIpsResource.retrieveAllVirtualIps(0, 0);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldReturn200OnSuccessfulRequests() throws Exception {
            Response resp = virtualIpsResource.retrieveAllVirtualIps(0, 0);
            Assert.assertEquals(200, resp.getStatus());
        }
    }

    public static class whenGettingLoadBalancerPorts {

        @Mock
        VirtualIpRepository virtualIpRepository;

        VirtualIpResource virtualIpResource;

        List<org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer> loadBalancers;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer loadBalancer;
        Ports rPorts = new Ports();
        Port rPort;
        LoadBalancer lb;
        List<LoadBalancer> lbs;
        Map<Integer, List<LoadBalancer>> portMap;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);

            virtualIpResource = new VirtualIpResource();
            virtualIpResource.setMockitoAuth(true);
            virtualIpResource.setVipRepository(virtualIpRepository);
            virtualIpResource.setDozerMapper( DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());

            loadBalancer = new org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer();
            loadBalancers = new ArrayList<>();
            loadBalancers.add(loadBalancer);
            lbs = new ArrayList<>();
            lb = new LoadBalancer();
            lbs.add(lb);
            rPort = new Port();
            portMap = new HashMap<>();
            rPort.setValue(1);
            rPort.setLoadBalancers(loadBalancers);
            portMap.put(1, lbs);
        }

        @Test
        public void shouldReturn200WithReturningLBPorts() {

            when(virtualIpRepository.getPorts(anyInt())).thenReturn(portMap);

            Response response = virtualIpResource.getLoadBalancerPorts();
            Assert.assertEquals(200, response.getStatus());


        }

        @Test
        public void shouldReturn200WithAndNullPortsIfPortsAreEmpty() {

            portMap.clear();
            Response response = virtualIpResource.getLoadBalancerPorts();
            Assert.assertEquals(200, response.getStatus());
            Ports ports = (Ports) response.getEntity();
            Assert.assertTrue(ports.getPorts().isEmpty());

        }

        @Test
        public void shouldReturn500WhenUnableToReturnLBPorts() {

            when(virtualIpRepository.getPorts(anyInt())).thenReturn(null);

            Response response = virtualIpResource.getLoadBalancerPorts();
            Assert.assertEquals(500, response.getStatus());


        }


    }

    public static class whenDeletingVirtualIp {

        @Mock
        VirtualIpService virtualIpService;

        VirtualIpResource virtualIpResource;

        @Before
        public void setUp() {

            MockitoAnnotations.initMocks(this);
            virtualIpResource = new VirtualIpResource();
            virtualIpResource.setMockitoAuth(true);
            virtualIpResource.setVirtualIpService(virtualIpService);

            virtualIpResource.setId(1);


        }


        @Test
        public void shouldReturn200DeletingVip() throws Exception {
            Response response = virtualIpResource.deleteVirtualIp();
            Assert.assertEquals(200, response.getStatus());

        }


    }
}