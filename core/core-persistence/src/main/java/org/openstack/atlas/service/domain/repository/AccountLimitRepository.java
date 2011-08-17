package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
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

    public List<AccountLimit> getAccountLimits(Integer accountId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AccountLimit> criteria = builder.createQuery(AccountLimit.class);
        Root<AccountLimit> accountLimitRoot = criteria.from(AccountLimit.class);

        Predicate hasAccountId = builder.equal(accountLimitRoot.get(AccountLimit_.accountId), accountId);

        criteria.select(accountLimitRoot);
        criteria.where(hasAccountId);
        return entityManager.createQuery(criteria).getResultList();
    }

    public LimitType getLimitType(AccountLimitType acountLimitType) throws EntityNotFoundException {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LimitType> criteria = builder.createQuery(LimitType.class);
        Root<LimitType> limitTypeRoot = criteria.from(LimitType.class);

        Predicate hasName = builder.equal(limitTypeRoot.get(LimitType_.name), acountLimitType);

        criteria.select(limitTypeRoot);
        criteria.where(hasName);

        LimitType limitType = entityManager.createQuery(criteria).getSingleResult();
        if (limitType == null) {
            String message = String.format("No limit type found for '%s'", acountLimitType.name());
            LOG.error(message);
            throw new EntityNotFoundException(message);
        }
        return limitType;

    }
}
