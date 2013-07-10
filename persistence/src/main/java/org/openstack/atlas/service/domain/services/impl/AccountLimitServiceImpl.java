package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.AccountLimit;
import org.openstack.atlas.service.domain.entities.AccountLimitType;
import org.openstack.atlas.service.domain.entities.LimitType;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.AllAbsoluteLimits;
import org.openstack.atlas.service.domain.pojos.LoadBalancerCountByAccountIdClusterId;
import org.openstack.atlas.service.domain.services.AccountLimitService;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountLimitServiceImpl extends BaseService implements AccountLimitService {
    private final Log LOG = LogFactory.getLog(AccountLimitServiceImpl.class);

    @Override
    public AccountLimit getByIdAndAccountId(Integer id, Integer accountId) {
        return accountLimitRepository.getByIdAndAccountId(id, accountId);
    }

    @Override
    public void save(AccountLimit accountLimit) throws BadRequestException {
        try {
            AccountLimit dbLimit = accountLimitRepository.getByAccountIdAndType(accountLimit.getAccountId(), accountLimit.getLimitType());
            if (dbLimit != null) {
                throw new BadRequestException("A limit for the Limit Type " + dbLimit.getLimitType().getName().toString() + " already exists for the account id " + dbLimit.getAccountId());
            }
        } catch (NoResultException nre) {
            accountLimitRepository.save(accountLimit);
        }
    }

    @Override
    public void delete(AccountLimit accountLimit) throws EntityNotFoundException {
        AccountLimit dbLimit;
        try {
            dbLimit = accountLimitRepository.getById(accountLimit.getId());
        } catch (Exception e) {
            String errorMessage = "There is no absolute limit with id = " + accountLimit.getId();
            throw new EntityNotFoundException(errorMessage);
        }
        if (!accountLimit.getAccountId().equals(dbLimit.getAccountId())) {
            String errMsg = String.format("Cannot access accountLimit {id=%d}", accountLimit.getId());
            throw new EntityNotFoundException(errMsg);
        }
        accountLimitRepository.delete(accountLimit);
    }

    @Override
    public AccountLimit update(AccountLimit accountLimit) throws EntityNotFoundException {
        AccountLimit dbLimit;
        try {
            dbLimit = accountLimitRepository.getById(accountLimit.getId());
        } catch (Exception e) {
            String errorMessage = "There is no absolute limit with id = " + accountLimit.getId();
            throw new EntityNotFoundException(errorMessage);
        }
        if (!accountLimit.getAccountId().equals(dbLimit.getAccountId())) {
            String errMsg = String.format("Cannot access accountLimit {id=%d}", accountLimit.getId());
            throw new EntityNotFoundException(errMsg);
        }
        dbLimit.setLimit(accountLimit.getLimit());
        return accountLimitRepository.update(dbLimit);
    }

    @Override
    public List<AccountLimit> getCustomAccountLimits(Integer accountId) {
        return accountLimitRepository.getAccountLimits(accountId);
    }

    @Override
    public int getLimit(Integer accountId, AccountLimitType accountLimitType) throws EntityNotFoundException {
        List<AccountLimit> allAccountLimits = getCustomAccountLimits(accountId);

        for (AccountLimit accountLimit : allAccountLimits) {
            if (accountLimit.getLimitType().getName().equals(accountLimitType)) {
                return accountLimit.getLimit();
            }
        }

        LimitType resultLimitType;
        try {
            resultLimitType = accountLimitRepository.getLimitType(accountLimitType);
        } catch (Exception e) {
            String message = String.format("No limit type found for '%s'", accountLimitType.name());
            LOG.error(message, e);
            throw new EntityNotFoundException(message);
        }

        if (resultLimitType == null) {
            String message = String.format("No limit type found for '%s'", accountLimitType.name());
            LOG.error(message);
            throw new EntityNotFoundException(message);
        }

        return resultLimitType.getDefaultValue();
    }

    @Override
    public List<LimitType> getAllLimitTypes() {
        return accountLimitRepository.getAllLimitTypes();
    }

    @Override
    public Map<String, Integer> getAllLimitsForAccount(Integer accountId) {
        Map<String, Integer> limitsForAccount = new HashMap<String, Integer>();
        List<LimitType> allLimitTypes = getAllLimitTypes();
        List<AccountLimit> customAccountLimits = getCustomAccountLimits(accountId);

        for (LimitType limitType : allLimitTypes) {
            limitsForAccount.put(limitType.getName().name(), limitType.getDefaultValue());
        }

        for (AccountLimit customAccountLimit : customAccountLimits) {
            limitsForAccount.put(customAccountLimit.getLimitType().getName().name(), customAccountLimit.getLimit());
        }

        return limitsForAccount;
    }

    @Override
    public AllAbsoluteLimits getAllAbsoluteLimitsForAccount(Integer accountId) {
        AllAbsoluteLimits limitsForAccount = new AllAbsoluteLimits();
        List<LimitType> allLimitTypes = getAllLimitTypes();
        List<AccountLimit> customAccountLimits = getCustomAccountLimits(accountId);

        List<LimitType> removalList = new ArrayList<LimitType>();
        for (LimitType limitType : allLimitTypes) {
            for (AccountLimit customAccountLimit : customAccountLimits) {
                if (limitType.getName().equals(customAccountLimit.getLimitType().getName())) {
                    removalList.add(limitType);
                }
            }
        }

        if (removalList.size() > 0) {
            allLimitTypes.removeAll(removalList);
        }

        if (allLimitTypes.size() > 0) {
            limitsForAccount.setDefaultLimits(allLimitTypes);
        }

        if (customAccountLimits.size() > 0) {
            limitsForAccount.setCustomLimits(customAccountLimits);
        }

        return limitsForAccount;
    }

    @Override
    public Map<Integer, List<AccountLimit>> getAllAccountLimits() {
        Map<Integer, List<AccountLimit>> customLimitAccounts = new HashMap<Integer, List<AccountLimit>>();
        List<AccountLimit> customAccountLimitAccounts = accountLimitRepository.getAllCustomLimits();
        for (AccountLimit accountLimit : customAccountLimitAccounts) {
            if (customLimitAccounts.containsKey(accountLimit.getAccountId())) {
                customLimitAccounts.get(accountLimit.getAccountId()).add(accountLimit);
            } else {
                List<AccountLimit> newList = new ArrayList<AccountLimit>();
                newList.add(accountLimit);
                customLimitAccounts.put(accountLimit.getAccountId(), newList);
            }
        }
        return customLimitAccounts;
    }

    public Map<Integer, List<AccountLimit>> getAccountLimitsForCluster(Integer clusterId) {
        List<LoadBalancerCountByAccountIdClusterId> accountIds = clusterRepository.getAccountsInCluster(clusterId);
        List<Integer> ids = new ArrayList<Integer>();
        for (LoadBalancerCountByAccountIdClusterId accountId : accountIds) {
            ids.add(accountId.getAccountId());
        }
        Map<Integer, List<AccountLimit>> customLimitAccounts = getAllAccountLimits();
        List<Integer> remList = new ArrayList<Integer>();
        for (Integer key : new ArrayList<Integer>(customLimitAccounts.keySet())) {
            if (!ids.contains(key)) {
                remList.add(key);
            }
        }
        return customLimitAccounts;
    }
}
