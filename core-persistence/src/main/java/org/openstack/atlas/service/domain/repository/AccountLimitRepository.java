package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.entity.AccountLimit;
import org.openstack.atlas.service.domain.entity.AccountLimitType;
import org.openstack.atlas.service.domain.entity.LimitType;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;

import java.util.List;

public interface AccountLimitRepository {

    AccountLimit create(AccountLimit accountLimit);

    List<AccountLimit> getAccountLimits(Integer accountId);

    int getLimit(Integer accountId, AccountLimitType accountLimitType) throws EntityNotFoundException;

    LimitType getLimitType(AccountLimitType accountLimitType) throws EntityNotFoundException;

    List<AccountLimit> getCustomLimitsByAccountId(Integer accountId) throws EntityNotFoundException;

    void delete(AccountLimit accountLimit) throws EntityNotFoundException;
}
