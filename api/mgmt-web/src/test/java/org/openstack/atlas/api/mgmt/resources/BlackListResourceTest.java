package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Blacklist;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.BlacklistItem;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.BlacklistType;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.IpVersion;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.repository.BlacklistRepository;
import org.openstack.atlas.service.domain.services.BlackListService;

import javax.ws.rs.core.Response;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@RunWith(Enclosed.class)
public class BlackListResourceTest {

    public static class WhenDeletingABlackListItem {

        private ManagementAsyncService asyncService;
        private BlackListResource blackListResource;
        private BlackListService blackListService;
        private OperationResponse operationResponse;

        @Before
        public void setUp() {
            blackListResource = new BlackListResource();
            blackListResource.setMockitoAuth(true);
            asyncService = mock(ManagementAsyncService.class);
            blackListService = mock(BlackListService.class);
            blackListResource.setManagementAsyncService(asyncService);
            blackListResource.setBlackListService(blackListService);
            blackListResource.setId(12);
            operationResponse = new OperationResponse();
            operationResponse.setExecutedOkay(true);
        }

        @Test
        public void shouldReturn500WhenExecutedOkayisFalse() throws Exception {
            doThrow(Exception.class).when(blackListService).deleteBlackList(any());

            Response resp = blackListResource.deleteBlackListItem(12);
            Assert.assertEquals(500, resp.getStatus());
        }
    }

    public static class WhenAddingABlackListItem {
        static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";


        private ManagementAsyncService asyncService;
        private BlackListResource blackListResource;
        private BlackListService blackListService;
        private OperationResponse operationResponse;
        private Blacklist bl;
        private BlacklistItem bli;


        @Before
        public void setUp() {
            blackListResource = new BlackListResource();
            blackListResource.setMockitoAuth(true);
            asyncService = mock(ManagementAsyncService.class);
            blackListService = mock(BlackListService.class);
            blackListResource.setManagementAsyncService(asyncService);
            blackListResource.setId(12);
            blackListResource.setBlackListService(blackListService);
            operationResponse = new OperationResponse();
            operationResponse.setExecutedOkay(true);
            blackListResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
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
        public void shouldReturn500OnException() throws Exception {
            doThrow(Exception.class).when(blackListService).createBlacklist(any());
            Response resp = blackListResource.addBlacklistItem(bl);
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldReturn400WhenValidationFails() throws Exception {

            Response resp = blackListResource.addBlacklistItem(null);
            Assert.assertEquals(400, resp.getStatus());
        }

        @Test
        public void shouldReturnOKWhenExecutedOkay() throws Exception {
            Response resp = blackListResource.addBlacklistItem(bl);
            Assert.assertEquals(202, resp.getStatus());
        }
    }
}
