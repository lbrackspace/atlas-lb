package org.openstack.atlas.service.domain.repository.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.common.ErrorMessages;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.AccountLimitRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
@Transactional
public class AccountLimitRepositoryImpl implements AccountLimitRepository {
    final Log LOG = LogFactory.getLog(AccountLimitRepositoryImpl.class);
    private static final String entityNotFound = "Account limit not found";
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    @Override
    public AccountLimit create(AccountLimit accountLimit) {
        accountLimit = entityManager.merge(accountLimit);
        return accountLimit;
    }

    @Override
    public List<AccountLimit> getAccountLimits(Integer accountId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AccountLimit> criteria = builder.createQuery(AccountLimit.class);
        Root<AccountLimit> accountLimitRoot = criteria.from(AccountLimit.class);

        Predicate hasAccountId = builder.equal(accountLimitRoot.get(AccountLimit_.accountId), accountId);

        criteria.select(accountLimitRoot);
        criteria.where(hasAccountId);
        return entityManager.createQuery(criteria).getResultList();
    }

    @Override
    public int getLimit(Integer accountId, AccountLimitType accountLimitType) throws EntityNotFoundException {
        List<AccountLimit> allAccountLimits = getAccountLimits(accountId);

        for (AccountLimit accountLimit : allAccountLimits) {
            if (accountLimit.getLimitType().getName().equals(accountLimitType)) {
                return accountLimit.getLimit();
            }
        }

        LimitType dbLimitType = this.getLimitType(accountLimitType);
        if (dbLimitType == null) {
            throw new EntityNotFoundException(ErrorMessages.ACCOUNT_LIMIT_NOT_FOUND.getMessage(accountLimitType.name()));
        }

        return dbLimitType.getDefaultValue();
    }

    @Override
    public LimitType getLimitType(AccountLimitType accountLimitType) throws EntityNotFoundException {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LimitType> criteria = builder.createQuery(LimitType.class);
        Root<LimitType> limitTypeRoot = criteria.from(LimitType.class);

        Predicate hasName = builder.equal(limitTypeRoot.get(LimitType_.name), accountLimitType);

        criteria.select(limitTypeRoot);
        criteria.where(hasName);

        LimitType limitType = entityManager.createQuery(criteria).getSingleResult();
        if (limitType == null) {
            String message = String.format("No limit type found for '%s'", accountLimitType.name());
            LOG.error(message);
            throw new EntityNotFoundException(message);
        }
        return limitType;

    }

    @Override
    public List<AccountLimit> getCustomLimitsByAccountId(Integer accountId) throws EntityNotFoundException {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AccountLimit> criteria = builder.createQuery(AccountLimit.class);
        Root<AccountLimit> accountLimitRoot = criteria.from(AccountLimit.class);
        Predicate belongsToAccount = builder.equal(accountLimitRoot.get(AccountLimit_.accountId), accountId);

        criteria.select(accountLimitRoot);
        criteria.where(belongsToAccount);

        try {
            return entityManager.createQuery(criteria).getResultList();
        } catch (NoResultException e) {
            throw new EntityNotFoundException(entityNotFound);
        }
    }

    @Override
    public void delete(AccountLimit accountLimit) throws EntityNotFoundException {
        if (accountLimit == null) throw new EntityNotFoundException(entityNotFound);
        accountLimit = entityManager.merge(accountLimit); // Re-attach hibernate instance
        entityManager.remove(accountLimit);
    }
}
