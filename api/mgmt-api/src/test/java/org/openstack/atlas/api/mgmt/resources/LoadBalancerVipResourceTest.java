package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.api.mgmt.helpers.MgmtMapperBuilder;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
@Ignore
public class LoadBalancerVipResourceTest {
    public static class WhenGettingALoadBalancerVirtualIps {

        private ManagementAsyncService asyncService;
        private LoadbalancerVipResource virtualIpsResource;
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
            virtualIpsResource.setManagementAsyncService(asyncService);
            virtualIpsResource.setId(12);
            virtualIpsResource.setLoadBalancerId(4);
            virtualIpsResource.setVipRepository(vpRepository);
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
        private OperationResponse operationResponse;
        private VirtualIp domainVip;
        private org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp vip;

        @Before
        public void setUp() {
            virtualIpsResource = new LoadbalancerVipResource();
            virtualIpsResource.setMockitoAuth(true);
            asyncService = mock(ManagementAsyncService.class);
            virtualIpsResource.setManagementAsyncService(asyncService);
            virtualIpsResource.setId(12);
            operationResponse = new OperationResponse();
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
            operationResponse.setExecutedOkay(true);
            operationResponse.setEntity(domainVip);
            org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp nVip = new org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp();
            nVip.setType(VipType.PUBLIC);
            Response resp = virtualIpsResource.addVirtualIpToLoadBalancer(vip);
            Assert.assertEquals(202, resp.getStatus());
        }
        
        @Test
        public void shouldReturn202WhenVipIdIsPassedIn() throws Exception {
            operationResponse.setExecutedOkay(true);
            operationResponse.setEntity(domainVip);
            org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp nVip = new org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp();
            nVip.setId(3);
            Response resp = virtualIpsResource.addVirtualIpToLoadBalancer(vip);
            Assert.assertEquals(202, resp.getStatus());
        }

        @Test
        public void shouldReturn500WhenExecutedOkayisFalse() throws Exception {
            operationResponse.setExecutedOkay(false);
            operationResponse.setEntity(domainVip);            
            Response resp = virtualIpsResource.addVirtualIpToLoadBalancer(vip);
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldReturn500WhenEsbReturnsNull() throws Exception {
            operationResponse.setExecutedOkay(false);
            operationResponse.setEntity(domainVip);            
            Response resp = virtualIpsResource.addVirtualIpToLoadBalancer(vip);
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldReturn500OnEsbException() throws Exception {
            operationResponse.setExecutedOkay(false);
            operationResponse.setEntity(domainVip);
            Response resp = virtualIpsResource.addVirtualIpToLoadBalancer(vip);
            Assert.assertEquals(500, resp.getStatus());
        }
    }
}
