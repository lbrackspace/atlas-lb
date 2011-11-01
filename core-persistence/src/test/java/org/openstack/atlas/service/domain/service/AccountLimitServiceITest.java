package org.openstack.atlas.service.domain.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.entity.AccountLimit;
import org.openstack.atlas.service.domain.entity.AccountLimitType;
import org.openstack.atlas.service.domain.exception.EntityExistsException;
import org.openstack.atlas.service.domain.exception.LimitReachedException;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(Enclosed.class)
public class AccountLimitServiceITest {

    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenCreatingAnAccountLimit extends AccountLimitServiceBase {

        @Test
        public void shouldSetIdWhenCreateSucceeds() {
            Assert.assertNotNull(accountLimit.getId());
        }

        @Test(expected = EntityExistsException.class)
        public void shouldThrowExceptionWhenCreatingLimitMoreThanOnce() throws PersistenceServiceException {
            accountLimitService.create(loadBalancer.getAccountId(), accountLimit);
        }
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenVerifyingLoadBalancerLimit extends AccountLimitServiceBase {

        @Test
        public void shouldNotThrowExceptionWhenAccountDoesNotExist() throws PersistenceServiceException {
            accountLimitService.verifyLoadBalancerLimit(-99999);
        }

        @Test(expected = LimitReachedException.class)
        public void shouldThrowExceptionWhenLimitIsReached() throws PersistenceServiceException {
            loadBalancerService.create(loadBalancer);
            accountLimitService.verifyLoadBalancerLimit(loadBalancer.getAccountId());
        }
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenGettingLimits extends AccountLimitServiceBase {

        @Test
        public void shouldNotThrowExceptionWhenAccountDoesNotExist() throws PersistenceServiceException {
            accountLimitService.getLimit(-99999, AccountLimitType.LOADBALANCER_LIMIT);
        }

        @Test
        public void shouldReturnSavedCustomLimit() throws PersistenceServiceException {
            int limit = accountLimitService.getLimit(loadBalancer.getAccountId(), accountLimit.getLimitType().getName());
            Assert.assertEquals(accountLimit.getLimit(), limit);
        }

        @Test
        public void shouldReturnADefaultLimitWhenCustomLimitDoesNotExist() throws PersistenceServiceException {
            accountLimitService.delete(loadBalancer.getAccountId());
            int limit = accountLimitService.getLimit(loadBalancer.getAccountId(), AccountLimitType.LOADBALANCER_LIMIT);
            Assert.assertNotSame(accountLimit.getLimit(), limit);
        }
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenDeleting extends AccountLimitServiceBase {

        @Test
        public void shouldNotExistWhenProperlyDeleted() throws PersistenceServiceException {
            accountLimitService.delete(loadBalancer.getAccountId());
            final List<AccountLimit> limits = accountLimitRepository.getCustomLimitsByAccountId(loadBalancer.getAccountId());
            Assert.assertTrue(limits.isEmpty());
        }
    }
}
