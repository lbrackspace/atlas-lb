package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.api.mgmt.helpers.MgmtMapperBuilder;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openstack.atlas.service.domain.services.VirtualIpService;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
@Ignore
public class LoadBalancerVipResourceTest {
    public static class WhenGettingALoadBalancerVirtualIps {

        private ManagementAsyncService asyncService;
        private LoadbalancerVipResource virtualIpsResource;
        private VirtualIpService virtualIpService;
        private OperationResponse operationResponse;
        private VirtualIp domainVip;
        private List<VirtualIp> domainVips;
        private VirtualIpRepository vpRepository;

        @Before
        public void setUp() {
            virtualIpsResource = new LoadbalancerVipResource();
            virtualIpsResource.setMockitoAuth(true);
            asyncService = mock(ManagementAsyncService.class);
            vpRepository = mock(VirtualIpRepository.class);
            virtualIpService = mock(VirtualIpService.class);
            virtualIpsResource.setManagementAsyncService(asyncService);
            virtualIpsResource.setId(12);
            virtualIpsResource.setLoadBalancerId(4);
            virtualIpsResource.setVipRepository(vpRepository);
            virtualIpsResource.setVirtualIpService(virtualIpService);
            operationResponse = new OperationResponse();
            virtualIpsResource.setDozerMapper(MgmtMapperBuilder.getConfiguredMapper());
            domainVip = new VirtualIp();
            domainVips = new ArrayList<VirtualIp>();
        }

        @Test
        public void shouldReturn200WhenEsbIsNormal() throws Exception {
            when(vpRepository.getVipsByLoadBalancerId(Matchers.anyInt())).thenReturn(domainVips);            
            Response resp = virtualIpsResource.getVipsbyLoadBalancerId();
            Assert.assertEquals(200, resp.getStatus());
        }

         @Test
        public void shouldReturn500WhenEntityReturnsNull() throws Exception {
            when(vpRepository.getVipsByLoadBalancerId(Matchers.anyInt())).thenReturn(null);
            Response resp = virtualIpsResource.getVipsbyLoadBalancerId();
            Assert.assertEquals(500, resp.getStatus());
        }
    }

    public static class WhenPostingALoadBalancerVirtualIp {

        private ManagementAsyncService asyncService;
        private LoadbalancerVipResource virtualIpsResource;
        private VirtualIpService virtualIpService;
        private OperationResponse operationResponse;
        private VirtualIp domainVip;
        private org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp vip;

        @Before
        public void setUp() {
            virtualIpsResource = new LoadbalancerVipResource();
            virtualIpsResource.setMockitoAuth(true);
            asyncService = mock(ManagementAsyncService.class);
            virtualIpService = mock(VirtualIpService.class);
            virtualIpsResource.setManagementAsyncService(asyncService);
            virtualIpsResource.setId(12);
            operationResponse = new OperationResponse();
            virtualIpsResource.setVirtualIpService(virtualIpService);
            virtualIpsResource.setDozerMapper(MgmtMapperBuilder.getConfiguredMapper());
            domainVip = new VirtualIp();
            vip = new org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp();
            vip.setType(VipType.SERVICENET);
            Ticket ticket = new Ticket();
            ticket.setTicketId("1234");
            ticket.setComment("My first comment!");
            vip.setTicket(ticket);
            
        }

        @Test
        public void shouldReturn202WhenVipTypeIsPassedIn() throws Exception {
            when(virtualIpService.addVirtualIpToLoadBalancer(any(), any(), any())).thenReturn(new VirtualIp());
            Response resp = virtualIpsResource.addVirtualIpToLoadBalancer(vip);
            Assert.assertEquals(202, resp.getStatus());
        }
        
        @Test
        public void shouldReturn202WhenVipIdIsPassedIn() throws Exception {
            when(virtualIpService.addVirtualIpToLoadBalancer(any(), any(), any())).thenReturn(new VirtualIp());
            vip.setId(23);
            vip.setType(null);
            Response resp = virtualIpsResource.addVirtualIpToLoadBalancer(vip);
            Assert.assertEquals(202, resp.getStatus());
        }

        @Test
        public void shouldReturn500WhenExecutedOkayisFalse() throws Exception {
            doThrow(Exception.class).when(virtualIpService).addVirtualIpToLoadBalancer(any(), any(), any());
            Response resp = virtualIpsResource.addVirtualIpToLoadBalancer(vip);
            Assert.assertEquals(500, resp.getStatus());
        }
    }
}
