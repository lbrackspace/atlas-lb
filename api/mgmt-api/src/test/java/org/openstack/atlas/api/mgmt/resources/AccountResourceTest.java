package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountRecord;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ExtendedAccountLoadbalancer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ExtendedAccountLoadbalancers;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.service.domain.entities.Account;
import org.openstack.atlas.service.domain.entities.GroupRateLimit;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.pojos.AccountLoadBalancer;
import org.openstack.atlas.service.domain.pojos.ExtendedAccountLoadBalancer;
import org.openstack.atlas.service.domain.repository.GroupRepository;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.VirtualIpService;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class AccountResourceTest {
    static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

    public static class whenRetrievingAccountDetails {
        private ManagementAsyncService asyncService;
        private OperationResponse operationResponse;
        private AccountResource accountResource;
        private LoadBalancersResource lbResource;
        private LoadBalancerService lbService;
        private VirtualIpService virtualIpService;
        private GroupRepository groupRepository;

        @Before
        public void setUp() {
            accountResource = new AccountResource();
            lbResource = new LoadBalancersResource();
            accountResource.setMockitoAuth(true);
            accountResource.setId(12);

            asyncService = mock(ManagementAsyncService.class);
            lbService = mock(LoadBalancerService.class);
            accountResource.setLoadBalancerService(lbService);
            groupRepository = mock(GroupRepository.class);
            accountResource.setGroupRepository(groupRepository);
            accountResource.setManagementAsyncService(asyncService);
            accountResource.setLoadBalancersResource(lbResource);
            virtualIpService = mock(VirtualIpService.class);
            accountResource.setVirtualIpService(virtualIpService);
            operationResponse = new OperationResponse();
            operationResponse.setExecutedOkay(true);
            accountResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
        }

        @Test
        public void shouldReturnLoadbalancersResource() throws Exception {
            LoadBalancersResource resource = accountResource.getLoadBalancersResource();
            Assert.assertEquals(12, resource.getAccountId());
        }

        @Test
        public void shouldReturnAccountLoadBalancers() throws Exception {
            List<AccountLoadBalancer> elbs = new ArrayList<AccountLoadBalancer>();
            AccountLoadBalancer elb = new AccountLoadBalancer();
            elb.setClusterId(2);
            elbs.add(elb);

            when(lbService.getAccountLoadBalancers(12)).thenReturn(elbs);
            Response response = accountResource.retrieveLoadBalancers();

            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals(String.valueOf(elb.getClusterId()),
                    ((org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountLoadBalancers)
                            response.getEntity()).getAccountLoadBalancers().get(0).getClusterId().toString());
        }

        @Test
        public void shouldReturnExtendedAccountLoadBalancers() {
            List<ExtendedAccountLoadBalancer> elbs = new ArrayList<ExtendedAccountLoadBalancer>();
            ExtendedAccountLoadBalancer elb = new ExtendedAccountLoadBalancer();
            elb.setClusterId(2);
            elbs.add(elb);

            when(lbService.getExtendedAccountLoadBalancer(12)).thenReturn(elbs);
            Response response = accountResource.retrieveExtendedAccountLoadBalancers();

            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals(String.valueOf(elb.getClusterId()),
                    ((ExtendedAccountLoadbalancers)
                            response.getEntity()).getExtendedAccountLoadbalancers().get(0).getClusterId().toString());

        }

        @Test
        public void shouldReturnGroups() throws Exception {
            List<GroupRateLimit> elbs = new ArrayList<GroupRateLimit>();
            GroupRateLimit elb = new GroupRateLimit();
            elb.setDescription("Description");
            elbs.add(elb);

            when(groupRepository.getByAccountId(12)).thenReturn(elbs);
            Response response = accountResource.retrieveGroups();

            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldDeleteGroups() throws Exception {
            List<GroupRateLimit> elbs = new ArrayList<GroupRateLimit>();
            GroupRateLimit elb = new GroupRateLimit();
            elb.setDescription("Description");
            elbs.add(elb);

            doNothing().when(groupRepository).deleteAllForAccount(12);
            Response response = accountResource.deleteAccountGroup();

            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldRetrieveAccountRecord() throws Exception {
            Account ar = new Account();
            ar.setId(12);
            ar.setSha1SumForIpv6("sum42");

            when(virtualIpService.getAccountRecord(12)).thenReturn(ar);
            Response response = accountResource.getAccountRecord();

            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals(String.valueOf(12), ((AccountRecord) response.getEntity()).getId().toString());
            Assert.assertEquals("sum42", ((AccountRecord) response.getEntity()).getSha1SumForIpv6());
        }

        @Test
        public void shouldDeleteAccountRecord() throws Exception {
            Account ar = new Account();
            ar.setId(12);
            ar.setSha1SumForIpv6("sum42");

            when(virtualIpService.deleteAccountRecord(12)).thenReturn(true);
            Response response = accountResource.removeAccountRecord();

            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldFailToDeleteAccountRecord() throws Exception {
            Account ar = new Account();
            ar.setId(12);
            ar.setSha1SumForIpv6("sum42");

            when(virtualIpService.deleteAccountRecord(12)).thenReturn(false);
            Response response = accountResource.removeAccountRecord();

            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("account not deleted possibly because it wasn't in the table",
                    ((BadRequest) response.getEntity()).getValidationErrors().getMessages().get(0));
        }
    }
}
