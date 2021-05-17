package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Blacklist;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.BlacklistItem;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.BlacklistType;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.IpVersion;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.repository.BlacklistRepository;
import org.openstack.atlas.service.domain.services.BlackListService;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.ip.exception.IpTypeMissMatchException;

import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        private BlacklistItem bli2;


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
            bli2 = new BlacklistItem();
            bli.setCidrBlock("192.0.0.0/32");
            bli2.setCidrBlock("192.5.0.0/32");
            bli.setIpVersion(IpVersion.IPV4);
            bli2.setIpVersion(IpVersion.IPV4);
            bli.setType(BlacklistType.NODE);
            bli2.setType(BlacklistType.NODE);
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
        public void shouldReturn400WhenCidrBlockIsInvalid() {
            bli.setCidrBlock("0.0.0.0");
            Response resp = blackListResource.addBlacklistItem(bl);
            Assert.assertEquals(400, resp.getStatus());
        }

        @Test
        public void shouldReturn400WhenBlacklistIsEmpty() {
            bl.getBlacklistItems().clear();
            Response resp = blackListResource.addBlacklistItem(bl);
            Assert.assertEquals(400, resp.getStatus());
        }

        @Test
        public void shouldReturnOKWhenExecutedOkay() throws Exception {
            Response resp = blackListResource.addBlacklistItem(bl);
            Assert.assertEquals(202, resp.getStatus());
        }

        @Test
        public void shouldReturn400WhenDuplicatesAreInList() throws Exception {
            bl.getBlacklistItems().add(bli);
            Response response = blackListResource.addBlacklistItem(bl);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldReturn400WhenBlackListItemHasId() throws Exception {
            bli.setId(1);
            Response response = blackListResource.addBlacklistItem(bl);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldReturn202WhenListHasTwoNonDuplicates() {
            bl.getBlacklistItems().add(bli2);
            Response response = blackListResource.addBlacklistItem(bl);
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldReturn202WhenListUsingIPV6HasTwoNonDuplicates() {
            bli2.setIpVersion(IpVersion.IPV6);
            bli2.setCidrBlock("2001:4100:7901::682a:3ae1::1/32");
            bli.setIpVersion(IpVersion.IPV6);
            bli.setCidrBlock("2001:4100:7908::682a:3ae1::1/32");
            bl.getBlacklistItems().add(bli2);
            Response response = blackListResource.addBlacklistItem(bl);
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldReturn400WhenListUsingIPV6HasTwoDuplicates() {
            bli2.setIpVersion(IpVersion.IPV6);
            bli2.setCidrBlock("2001:4100:7901::682a:3ae1::1/32");
            bli.setIpVersion(IpVersion.IPV6);
            bli.setCidrBlock("2001:4100:7901::682a:3ae1::1/32");
            bl.getBlacklistItems().add(bli2);
            Response response = blackListResource.addBlacklistItem(bl);
            Assert.assertEquals(400, response.getStatus());
        }

    }

    public static class whenRetrievingBlackList {

        static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

        BlackListResource blackListResource;

        @Mock
        BlacklistRepository blacklistRepository;

        List<org.openstack.atlas.service.domain.entities.BlacklistItem> blacklistItemList;
        org.openstack.atlas.service.domain.entities.BlacklistItem blacklistItem;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            blackListResource = new BlackListResource();
            blackListResource.setBlacklistRepository(blacklistRepository);
            blackListResource.setMockitoAuth(true);
            blacklistItemList = new ArrayList<>();
            blacklistItem = new org.openstack.atlas.service.domain.entities.BlacklistItem();
            blacklistItem.setId(1);
            blacklistItem.setBlacklistType(org.openstack.atlas.service.domain.entities.BlacklistType.NODE);
            blacklistItemList.add(blacklistItem);
            blackListResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());

            when(blacklistRepository.getAllBlacklistItems()).thenReturn(blacklistItemList);
        }

        @Test
        public void shouldReturn200WhenRetrieving() {
            Response response = blackListResource.retrieveBlacklist();
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturn200WithEmptyList() {
            blacklistItemList.clear();
            Response response = blackListResource.retrieveBlacklist();
            Assert.assertEquals(200, response.getStatus());
        }

    }

    public static class whenCheckingIfNodeIsBlackList {

        BlackListResource blackListResource;
        @Mock
        BlackListService blackListService;

        Node node;

        @Before
        public void setUp() throws IpTypeMissMatchException, IPStringConversionException {
            MockitoAnnotations.initMocks(this);
            blackListResource = new BlackListResource();
            blackListResource.setMockitoAuth(true);
            blackListResource.setBlackListService(blackListService);
        }

        @Test
        public void shouldReturn200WhenNodeIsBlackListed() throws IpTypeMissMatchException, IPStringConversionException {
            when(blackListService.getBlackListedItemNode(any())).thenReturn(node);
            Response response = blackListResource.isBlackListedNode("10.1.1.20");
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturn200WhenNodeIsNotBlacklisted() throws IpTypeMissMatchException, IPStringConversionException {
            when(blackListService.getBlackListedItemNode(any())).thenReturn(null);
            Response response = blackListResource.isBlackListedNode("10.1.1.20");
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldThrowIpStringConversionException() throws IPStringConversionException, IpTypeMissMatchException {
            when(blackListService.getBlackListedItemNode(any())).thenThrow(IPStringConversionException.class);
            Response response = blackListResource.isBlackListedNode("10.1.1.20");
            Assert.assertEquals(500, response.getStatus());
        }
    }

}
