package org.openstack.atlas.api.mgmt.resources;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openstack.atlas.api.mgmt.helpers.MgmtMapperBuilder;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.services.LoadBalancerService;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class LoadBalancerResourceTest {
    public static class WhenSettingLoadbalancerStatus {

        private ManagementAsyncService asyncService;
        private LoadBalancerResource lbResource;
        private OperationResponse operationResponse;
        private LoadBalancerService lbService;
        private org.openstack.atlas.service.domain.entities.LoadBalancer lb;

        @Before
        public void setUp() {
            lbResource = new LoadBalancerResource();
            lbResource.setMockitoAuth(true);
            asyncService = mock(ManagementAsyncService.class);
            lbService = mock(LoadBalancerService.class);
            lbResource.setManagementAsyncService(asyncService);
            lbResource.setLoadBalancerService(lbService);
            lbResource.setId(12);
            operationResponse = new OperationResponse();
            lbResource.setDozerMapper(MgmtMapperBuilder.getConfiguredMapper());
            lb = new org.openstack.atlas.service.domain.entities.LoadBalancer();
        }

        @Test
        public void shouldReturn200ForErrorStatus() throws Exception {
            lb.setStatus(LoadBalancerStatus.ERROR);
            lb.setId(12345);
            when(lbService.get(12)).thenReturn(lb);
            operationResponse.setExecutedOkay(true);
            operationResponse.setEntity(lb);
            Response resp = lbResource.updateLbStatus("ACTIVE");
            Assert.assertEquals(200, resp.getStatus());
        }

        @Test
        public void shouldReturn400WhenSuspended() throws Exception {
            lb.setStatus(LoadBalancerStatus.SUSPENDED);
            lb.setId(12345);
            when(lbService.get(12)).thenReturn(lb);
            operationResponse.setExecutedOkay(true);
            operationResponse.setEntity(lb);
            Response resp = lbResource.updateLbStatus("ACTIVE");
            Assert.assertEquals(400, resp.getStatus());
        }

        @Test
        public void shouldReturn400ForBadStatus() throws Exception {
            lb.setStatus(LoadBalancerStatus.ERROR);
            lb.setId(12345);
            when(lbService.get(12)).thenReturn(lb);
            operationResponse.setExecutedOkay(true);
            operationResponse.setEntity(lb);
            Response resp = lbResource.updateLbStatus("BLAH");
            Assert.assertEquals(400, resp.getStatus());
        }
    }
}
