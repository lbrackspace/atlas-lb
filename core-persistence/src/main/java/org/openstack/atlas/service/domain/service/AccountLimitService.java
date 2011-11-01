package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.AccountLimit;
import org.openstack.atlas.service.domain.entity.AccountLimitType;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.LimitReachedException;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;

public interface AccountLimitService {

    AccountLimit create(Integer accountId, AccountLimit accountLimit) throws PersistenceServiceException;

    void verifyLoadBalancerLimit(Integer accountId) throws EntityNotFoundException, LimitReachedException;

    int getLimit(Integer accountId, AccountLimitType accountLimitType) throws EntityNotFoundException;

    void delete(Integer accountId) throws PersistenceServiceException;
}
