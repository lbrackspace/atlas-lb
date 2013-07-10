package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.entities.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
@Transactional
public class AccountLimitRepository {
    final Log LOG = LogFactory.getLog(AccountLimitRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public AccountLimit getById(Integer id) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AccountLimit> criteria = builder.createQuery(AccountLimit.class);
        Root<AccountLimit> accountLimitRoot = criteria.from(AccountLimit.class);

        Predicate hasId = builder.equal(accountLimitRoot.get(AccountLimit_.id), id);

        criteria.select(accountLimitRoot);
        criteria.where(hasId);
        return entityManager.createQuery(criteria).getSingleResult();
    }

    public AccountLimit getByIdAndAccountId(Integer id, Integer accountId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AccountLimit> criteria = builder.createQuery(AccountLimit.class);
        Root<AccountLimit> accountLimitRoot = criteria.from(AccountLimit.class);

        Predicate hasId = builder.equal(accountLimitRoot.get(AccountLimit_.id), id);
        Predicate hasAccountId = builder.equal(accountLimitRoot.get(AccountLimit_.accountId), accountId);

        criteria.select(accountLimitRoot);
        criteria.where(hasId);
        criteria.where(hasAccountId);
        return entityManager.createQuery(criteria).getSingleResult();
    }

    public AccountLimit getByAccountIdAndType(Integer accountId, LimitType type) throws NoResultException {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AccountLimit> criteria = builder.createQuery(AccountLimit.class);
        Root<AccountLimit> accountLimitRoot = criteria.from(AccountLimit.class);

        Predicate hasAccountId = builder.equal(accountLimitRoot.get(AccountLimit_.accountId), accountId);
        Predicate hasType = builder.equal(accountLimitRoot.get(AccountLimit_.limitType), type);

        criteria.select(accountLimitRoot);
        criteria.where(builder.and(hasType, hasAccountId));
        return entityManager.createQuery(criteria).getSingleResult();
    }

    public void save(AccountLimit accountLimit) {
        entityManager.persist(accountLimit);
    }

    public void save(LimitType limitType) {
        entityManager.persist(limitType);
    }

    public void delete(AccountLimit accountLimit) {
        accountLimit = entityManager.merge(accountLimit);
        entityManager.remove(accountLimit);
    }

    public AccountLimit update(AccountLimit accountLimit) {
        LOG.info("Updating AccountLimit " + accountLimit.getId() + "...");
        accountLimit = entityManager.merge(accountLimit);
        entityManager.flush();
        return accountLimit;
    }

    public List<AccountLimit> getAccountLimits(Integer accountId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AccountLimit> criteria = builder.createQuery(AccountLimit.class);
        Root<AccountLimit> accountLimitRoot = criteria.from(AccountLimit.class);

        Predicate hasAccountId = builder.equal(accountLimitRoot.get(AccountLimit_.accountId), accountId);

        criteria.select(accountLimitRoot);
        criteria.where(hasAccountId);
        return entityManager.createQuery(criteria).getResultList();
    }

    public LimitType getLimitType(AccountLimitType acountLimitType) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LimitType> criteria = builder.createQuery(LimitType.class);
        Root<LimitType> limitTypeRoot = criteria.from(LimitType.class);

        Predicate hasName = builder.equal(limitTypeRoot.get(LimitType_.name), acountLimitType);

        criteria.select(limitTypeRoot);
        criteria.where(hasName);
        return entityManager.createQuery(criteria).getSingleResult();
    }

    public List<LimitType> getAllLimitTypes() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LimitType> criteria = builder.createQuery(LimitType.class);
        Root<LimitType> limitTypeRoot = criteria.from(LimitType.class);

        criteria.select(limitTypeRoot);
        return entityManager.createQuery(criteria).getResultList();
    }

    public List<AccountLimit> getAllCustomLimits(){
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AccountLimit> criteria = builder.createQuery(AccountLimit.class);
        Root<AccountLimit> accountLimitRoot = criteria.from(AccountLimit.class);
        criteria.select(accountLimitRoot);
        return entityManager.createQuery(criteria).getResultList();
    }
}
