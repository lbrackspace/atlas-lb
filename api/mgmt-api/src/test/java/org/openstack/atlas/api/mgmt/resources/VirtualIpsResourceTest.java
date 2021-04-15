package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Cidr;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIps;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openstack.atlas.service.domain.services.VirtualIpService;
import org.openstack.atlas.service.domain.services.impl.ClusterServiceImpl;
import org.openstack.atlas.service.domain.services.impl.VirtualIpServiceImpl;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class VirtualIpsResourceTest {
    static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";
    public static class WhenGettingALoadBalancerVirtualIps {

        private ManagementAsyncService asyncService;
        private VirtualIpsResource virtualIpsResource;
        private OperationResponse operationResponse;
        private VirtualIpRepository vpRepository;
        private List<org.openstack.atlas.service.domain.entities.VirtualIp> vips;

        @Before
        public void setUp() {
            virtualIpsResource = new VirtualIpsResource();
            virtualIpsResource.setMockitoAuth(true);
            asyncService = mock(ManagementAsyncService.class);
            vpRepository = mock(VirtualIpRepository.class);
            virtualIpsResource.setVipRepository(vpRepository);
            
            vips = new ArrayList<org.openstack.atlas.service.domain.entities.VirtualIp>();

            virtualIpsResource.setManagementAsyncService(asyncService);
            virtualIpsResource.setId(12);
            operationResponse = new OperationResponse();

            virtualIpsResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
        }


        @Test
        public void shouldProduceInternalServerErrorWhenNullPointerFoundOrSomething() throws NullPointerException {
            doThrow(NullPointerException.class).when(vpRepository).getAll(any(), any());
            Response response = virtualIpsResource.retrieveAllVirtualIps(0,0);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test        
        public void shouldReturn200OnSuccessfulRequests() throws Exception {
            when(vpRepository.getAll(any(),any())).thenReturn(vips);
            Response resp = virtualIpsResource.retrieveAllVirtualIps(0, 0);
            Assert.assertEquals(200, resp.getStatus());
        }
    }

    public static class whenUpdatingVipsClusterId {

        VirtualIpsResource virtualIpsResource;

        Cidr cidr;
        List<VirtualIp> oldClusterVips;
        VirtualIp vip1;
        VirtualIp vip2;
        VirtualIp vip3;
        VirtualIp vip4;
        Cluster oldCluster;
        Cluster newCluster;

        @Mock
        VirtualIpServiceImpl virtualIpService;
        @Mock
        ClusterServiceImpl clusterService;


        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            virtualIpsResource = new VirtualIpsResource();
            virtualIpsResource.setMockitoAuth(true);
            virtualIpsResource.setVirtualIpService(virtualIpService);
            virtualIpsResource.setClusterService(clusterService);
            oldCluster = new Cluster();
            newCluster = new Cluster();
            vip1 = new VirtualIp();
            vip2 = new VirtualIp();
            vip3 = new VirtualIp();
            vip4 = new VirtualIp();
            oldCluster.setId(1);
            newCluster.setId(2);
            oldClusterVips = new ArrayList<>();
            vip1.setCluster(oldCluster);
            vip2.setCluster(oldCluster);
            vip3.setCluster(oldCluster);
            vip4.setCluster(oldCluster);
            vip1.setIpAddress("195.25.0.0");
            vip2.setIpAddress("195.25.0.1");
            vip3.setIpAddress("195.25.0.2");
            vip4.setIpAddress("195.25.0.3");
            oldClusterVips.add(vip1);
            oldClusterVips.add(vip2);
            oldClusterVips.add(vip3);
            oldClusterVips.add(vip4);
            cidr = new Cidr();
            cidr.setBlock("195.25.0.0/31");
            when(virtualIpService.getVipsByClusterId(anyInt())).thenReturn(oldClusterVips);
            when(clusterService.get(anyInt())).thenReturn(newCluster);
        }

        @Test
        public void shouldReturn200WhenUpdatingValidCidr() throws Exception {

            Response response = virtualIpsResource.updateClusterForVipBlock(1, 2, cidr);
            Assert.assertEquals(200, response.getStatus());



        }




    }
}
