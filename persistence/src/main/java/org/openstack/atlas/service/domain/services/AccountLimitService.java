package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.AccountLimit;
import org.openstack.atlas.service.domain.entities.AccountLimitType;
import org.openstack.atlas.service.domain.entities.LimitType;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.AllAbsoluteLimits;

import java.util.List;
import java.util.Map;

public interface AccountLimitService {

    void delete(AccountLimit accountLimit) throws EntityNotFoundException;

    AccountLimit getByIdAndAccountId(Integer id, Integer accountId);

    Map<Integer, List<AccountLimit>> getAllAccountLimits();

    Map<String, Integer> getAllLimitsForAccount(Integer accountId);

    AllAbsoluteLimits getAllAbsoluteLimitsForAccount(Integer accountId);

    Map<Integer, List<AccountLimit>> getAccountLimitsForCluster(Integer clusterId);

    List<LimitType> getAllLimitTypes();

    List<AccountLimit> getCustomAccountLimits(Integer accountId);

    int getLimit(Integer accountId, AccountLimitType accountLimitType) throws EntityNotFoundException;

    void save(AccountLimit accountLimit) throws BadRequestException;

    AccountLimit update(AccountLimit accountLimit) throws EntityNotFoundException;
}
