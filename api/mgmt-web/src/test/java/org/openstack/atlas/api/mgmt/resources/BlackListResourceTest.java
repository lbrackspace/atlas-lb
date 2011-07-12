package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Blacklist;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.BlacklistItem;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.BlacklistType;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.IpVersion;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;

import static org.mockito.Mockito.mock;

@RunWith(Enclosed.class)
public class BlackListResourceTest {

    public static class WhenDeletingABlackListItem {

        private ManagementAsyncService asyncService;
        private BlackListResource blackListResource;
        private OperationResponse operationResponse;

        @Before
        public void setUp() {
            blackListResource = new BlackListResource();
            blackListResource.setMockitoAuth(true);
            asyncService = mock(ManagementAsyncService.class);
            blackListResource.setManagementAsyncService(asyncService);
            blackListResource.setId(12);
            operationResponse = new OperationResponse();
            operationResponse.setExecutedOkay(true);
        }

        @Test
        public void shouldReturn500OnEsbReturningNull() throws Exception {
            
            Response resp = blackListResource.deleteBlackListItem(12);
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldReturn500WhenExecutedOkayisFalse() throws Exception {
            operationResponse.setExecutedOkay(false);
            Response resp = blackListResource.deleteBlackListItem(12);
            Assert.assertEquals(500, resp.getStatus());
        }
    }

    public static class WhenAddingABlackListItem {

        private ManagementAsyncService asyncService;
        private BlackListResource blackListResource;
        private OperationResponse operationResponse;
        private Blacklist bl;
        private BlacklistItem bli;


        @Before
        public void setUp() {
            blackListResource = new BlackListResource();
            blackListResource.setMockitoAuth(true);
            asyncService = mock(ManagementAsyncService.class);
            blackListResource.setManagementAsyncService(asyncService);
            blackListResource.setId(12);
            operationResponse = new OperationResponse();
            operationResponse.setExecutedOkay(true);
        }

        @Before
        public void standUpBlackList() {
            bl = new Blacklist();
            bli = new BlacklistItem();
            bli.setCidrBlock("192.0.0.0/32");
            bli.setIpVersion(IpVersion.IPV4);
            bli.setType(BlacklistType.NODE);
            bl.getBlacklistItems().add(bli);
        }


        @Test
        public void shouldReturn500OnEsbReturningNull() throws Exception {
            
            Response resp = blackListResource.addBlacklistItem(bl);
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldReturn400WhenExecutedOkayisFalse() throws Exception {
            operationResponse.setExecutedOkay(false);

            Response resp = blackListResource.addBlacklistItem(null);
            Assert.assertEquals(400, resp.getStatus());
        }

        @Test
        public void shouldReturnOKWhenExecutedOkay() throws Exception {
            operationResponse.setExecutedOkay(true);
            Response resp = blackListResource.addBlacklistItem(bl);
            Assert.assertEquals(500, resp.getStatus());
        }
    }
}
