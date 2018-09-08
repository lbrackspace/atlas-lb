package org.openstack.atlas.api.mgmt.resources;

import junit.framework.Assert;
import org.dozer.DozerBeanMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.openstack.atlas.service.domain.entities.GroupRateLimit;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.pojos.AccountLoadBalancer;
import org.openstack.atlas.service.domain.pojos.ExtendedAccountLoadBalancer;
import org.openstack.atlas.service.domain.repository.GroupRepository;
import org.openstack.atlas.service.domain.services.LoadBalancerService;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class AccountsResourceTest {
    public static class whenRetrievingAccountDetails {
        private ManagementAsyncService asyncService;
        private AccountsResource accountsResource;
        private OperationResponse operationResponse;
        private AccountResource accountResource;
        private LoadBalancerService lbService;
        private GroupRepository groupRepository;

        @Before
        public void setUp() {
            accountsResource = new AccountsResource();
            accountResource = new AccountResource();
            accountResource.setMockitoAuth(true);
            accountResource.setId(12);
            accountsResource.setAccountResource(accountResource);

            asyncService = mock(ManagementAsyncService.class);
            lbService = mock(LoadBalancerService.class);
            accountResource.setLoadBalancerService(lbService);
            groupRepository = mock(GroupRepository.class);
            accountResource.setGroupRepository(groupRepository);
            accountResource.setManagementAsyncService(asyncService);
            operationResponse = new OperationResponse();
            operationResponse.setExecutedOkay(true);
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-management-mapping.xml");
            accountResource.setDozerMapper(new DozerBeanMapper(mappingFiles));
        }

        @Test
        public void shouldReturnAccountResource() throws Exception {
            AccountResource resource = accountsResource.retrieveAccountResource(12);
            Assert.assertEquals(12, resource.getId());
        }

        @Test
        public void shouldReturnAccountLoadBalancers() throws Exception {
            List<AccountLoadBalancer> elbs = new ArrayList<AccountLoadBalancer>();
            AccountLoadBalancer elb = new AccountLoadBalancer();
            elb.setClusterId(2);
            elbs.add(elb);

            when(lbService.getAccountLoadBalancers(12)).thenReturn(elbs);
            AccountResource resource = accountsResource.retrieveAccountResource(12);
            Response response = resource.retrieveLoadBalancers();

            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturnExtendedAccountLoadBalancers() throws Exception {
            List<ExtendedAccountLoadBalancer> elbs = new ArrayList<ExtendedAccountLoadBalancer>();
            ExtendedAccountLoadBalancer elb = new ExtendedAccountLoadBalancer();
            elb.setClusterId(2);
            elbs.add(elb);

            when(lbService.getExtendedAccountLoadBalancer(12)).thenReturn(elbs);
            AccountResource resource = accountsResource.retrieveAccountResource(12);
            Response response = resource.retrieveExtendedAccountLoadBalancers();

            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturnGroups() throws Exception {
            List<GroupRateLimit> elbs = new ArrayList<GroupRateLimit>();
            GroupRateLimit elb = new GroupRateLimit();
            elb.setDescription("Description");
            elbs.add(elb);

            when(groupRepository.getByAccountId(12)).thenReturn(elbs);
            AccountResource resource = accountsResource.retrieveAccountResource(12);
            Response response = resource.retrieveGroups();

            Assert.assertEquals(200, response.getStatus());
        }

        @Ignore
        @Test
        public void shouldDeleteGroup() throws Exception {
            AccountResource resource = accountsResource.retrieveAccountResource(12);
//            Response response = resource.deleteAccountGroup();
            verify(groupRepository).deleteAllForAccount(12);

//            Assert.assertEquals(200, response.getStatus());
        }
    }
}
