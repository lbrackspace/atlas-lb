package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapper;
import org.dozer.DozerBeanMapperBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Limit;
import org.openstack.atlas.docs.loadbalancers.api.v1.Meta;
import org.openstack.atlas.service.domain.entities.AccountLimit;
import org.openstack.atlas.service.domain.services.AccountLimitService;

import javax.ws.rs.core.Response;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class AccountLimitResourceTest {

    public static class whenDeletingAccountLimits {



            AccountLimitResource accountLimitResource;

            @Mock
            AccountLimitService accountLimitService;

            @Before
            public void setUp() {
                MockitoAnnotations.initMocks(this);
                accountLimitResource = new AccountLimitResource();
                accountLimitResource.setMockitoAuth(true);
                accountLimitResource.setAccountLimitService(accountLimitService);

            }

            @Test
            public void shouldReturn202WhenDeletingAccountLimit() {

                Response response = accountLimitResource.deleteAccountLimit();
                Assert.assertEquals(202, response.getStatus());
            }


    }

    public static class whenUpdatingAccountLimit {

        AccountLimitResource accountLimitResource;
        Limit limit;
        AccountLimit accountLimit;
        @Mock
        AccountLimitService accountLimitService;
        @Mock
        DozerBeanMapper dozerBeanMapper;


        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            accountLimitResource = new AccountLimitResource();
            accountLimitResource.setMockitoAuth(true);
            accountLimitResource.setAccountLimitService(accountLimitService);
            accountLimitResource.setDozerMapper(dozerBeanMapper);
            limit = new Limit();
            accountLimit = new AccountLimit();
            limit.setId(1);
            limit.setName("test");
            limit.setValue(40);
            accountLimit.setLimit(40);
            when(dozerBeanMapper.map(limit, AccountLimit.class)).thenReturn(accountLimit);
        }

        @Test
        public void shouldReturn202WhenUpdatingAccountLimit() {

            Response response = accountLimitResource.updateAccountLimit(limit);
            Assert.assertEquals(202, response.getStatus());
        }

    }



}
