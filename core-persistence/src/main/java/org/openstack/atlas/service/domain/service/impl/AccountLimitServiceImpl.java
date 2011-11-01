package org.openstack.atlas.service.domain.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.AccountLimit;
import org.openstack.atlas.service.domain.entity.AccountLimitType;
import org.openstack.atlas.service.domain.entity.LimitType;
import org.openstack.atlas.service.domain.exception.EntityExistsException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.LimitReachedException;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.openstack.atlas.service.domain.repository.AccountLimitRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.AccountLimitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountLimitServiceImpl implements AccountLimitService {
    private final Log LOG = LogFactory.getLog(AccountLimitServiceImpl.class);

    @Autowired
    private AccountLimitRepository accountLimitRepository;

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;

    @Override
    @Transactional(rollbackFor = {EntityExistsException.class})
    public AccountLimit create(Integer accountId, AccountLimit accountLimit) throws PersistenceServiceException {
        List<AccountLimit> allAccountLimits = accountLimitRepository.getAccountLimits(accountId);

        for (AccountLimit limit : allAccountLimits) {
            if (limit.getLimitType().getName().equals(accountLimit.getLimitType().getName())) {
                throw new EntityExistsException("Account limit already exists.");
            }
        }

        return accountLimitRepository.create(accountLimit);
    }

    public void verifyLoadBalancerLimit(Integer accountId) throws EntityNotFoundException, LimitReachedException {
        Integer limit = this.getLimit(accountId, AccountLimitType.LOADBALANCER_LIMIT);
        final Integer numNonDeletedLoadBalancers = loadBalancerRepository.getNumNonDeletedLoadBalancersForAccount(accountId);
        boolean limitReached = (numNonDeletedLoadBalancers >= limit);
        if (limitReached) {
            LOG.error("Load balancer limit reached. Sending error response to client...");
            throw new LimitReachedException(String.format("Load balancer limit reached. "
                    + "Limit is set to '%d'. Contact support if you would like to increase your limit.",
                    limit));
        }
    }

    public int getLimit(Integer accountId, AccountLimitType accountLimitType) throws EntityNotFoundException {
        List<AccountLimit> allAccountLimits = accountLimitRepository.getAccountLimits(accountId);

        for (AccountLimit accountLimit : allAccountLimits) {
            if (accountLimit.getLimitType().getName().equals(accountLimitType)) {
                return accountLimit.getLimit();
            }
        }

        LimitType resultLimitType = accountLimitRepository.getLimitType(accountLimitType);
        return resultLimitType.getDefaultValue();
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class})
    public void delete(Integer accountId) throws EntityNotFoundException {
        for (AccountLimit accountLimit : accountLimitRepository.getCustomLimitsByAccountId(accountId)) {
            accountLimitRepository.delete(accountLimit);
        }
    }
}
