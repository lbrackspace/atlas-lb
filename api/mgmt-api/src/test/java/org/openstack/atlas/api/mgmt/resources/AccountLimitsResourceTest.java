package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Limit;
import org.openstack.atlas.service.domain.pojos.AllAbsoluteLimits;
import org.openstack.atlas.service.domain.services.AccountLimitService;

import javax.ws.rs.core.Response;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class AccountLimitsResourceTest {
    static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

    public static class whenRetrevingLimitsForAccount{

        AccountLimitsResource accountLimitsResource;

        @Mock
        AccountLimitService accountLimitService;

        AllAbsoluteLimits allAbsoluteLimits;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            accountLimitsResource = new AccountLimitsResource();
            accountLimitsResource.setAccountLimitService(accountLimitService);
            accountLimitsResource.setMockitoAuth(true);
            accountLimitsResource.setDozerMapper( DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());

            allAbsoluteLimits = new AllAbsoluteLimits();
            when(accountLimitService.getAllAbsoluteLimitsForAccount(anyInt())).thenReturn(allAbsoluteLimits);

        }

        @Test
        public void shouldReturn200whenRetrievingAccountLimits() throws Exception {
            Response response = accountLimitsResource.retrieveAllLimitsForAccount(1, 2, 3);
            Assert.assertEquals(200, response.getStatus());

        }

    }

    public static class whenCreatingAccountLimits {

        AccountLimitsResource accountLimitsResource;

        @Mock
        AccountLimitService accountLimitService;

        Limit limit;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            accountLimitsResource = new AccountLimitsResource();
            accountLimitsResource.setAccountLimitService(accountLimitService);
            accountLimitsResource.setMockitoAuth(true);
            accountLimitsResource.setDozerMapper( DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());

            limit = new Limit();
            limit.setName("LOADBALANCER_LIMIT");
            limit.setValue(101111);
//            limit.setId(1);

        }

        @Test
        public void shouldReturn200OnValidRequest() throws Exception {

            Response response = accountLimitsResource.createAccountLimit(limit);
            Assert.assertEquals(202, response.getStatus());

        }

        @Test
        public void shouldReturn400ForNullValue() throws Exception {
            limit.setValue(null);
            Response response = accountLimitsResource.createAccountLimit(limit);

            Assert.assertEquals(400, response.getStatus());

        }

        @Test
        public void shouldReturn400forNullName() throws Exception {

            limit.setName(null);
            Response response = accountLimitsResource.createAccountLimit(limit);

            Assert.assertEquals(400, response.getStatus());

        }
    }



}
