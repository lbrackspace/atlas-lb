package org.openstack.atlas.service.domain.service;

import org.junit.After;
import org.junit.Before;
import org.openstack.atlas.service.domain.entity.AccountLimit;
import org.openstack.atlas.service.domain.entity.AccountLimitType;
import org.openstack.atlas.service.domain.entity.LimitType;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.openstack.atlas.service.domain.repository.AccountLimitRepository;
import org.openstack.atlas.service.domain.repository.impl.AccountLimitRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;

public class AccountLimitServiceBase extends Base {
    @Autowired
    protected AccountLimitService accountLimitService;

    @Autowired
    protected AccountLimitRepository accountLimitRepository;

    protected AccountLimit accountLimit;

    @Before
    public void setUp() throws PersistenceServiceException {
        LimitType limitType = new LimitType();
        limitType.setName(AccountLimitType.LOADBALANCER_LIMIT);
        limitType.setDefaultValue(10);
        limitType.setDescription("Load balancer limit for an account");

        accountLimit = new AccountLimit();
        accountLimit.setAccountId(loadBalancer.getAccountId());
        accountLimit.setLimit(1);
        accountLimit.setLimitType(limitType);

        accountLimit = accountLimitService.create(loadBalancer.getAccountId(), accountLimit);
    }

    @After
    public void tearDown() throws PersistenceServiceException {
        accountLimitService.delete(loadBalancer.getAccountId());
    }
}
