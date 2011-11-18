package org.openstack.atlas.rax.domain.repository.impl;

import org.openstack.atlas.rax.domain.repository.RaxVirtualIpRepository;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip_;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.repository.impl.VirtualIpRepositoryImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import javax.persistence.criteria.*;
import java.util.List;

@Primary
@Repository
@Transactional
public class RaxVirtualIpRepositoryImpl extends VirtualIpRepositoryImpl implements RaxVirtualIpRepository {

    @Override
    public Long getNumIpv4VipsForLoadBalancer(LoadBalancer loadBalancer) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<LoadBalancerJoinVip> lbJoinVipRoot = criteria.from(LoadBalancerJoinVip.class);

        Expression<Long> countExp = builder.count(lbJoinVipRoot.get(LoadBalancerJoinVip_.virtualIp));
        Predicate hasVip = builder.equal(lbJoinVipRoot.get(LoadBalancerJoinVip_.loadBalancer), loadBalancer);

        criteria.select(countExp);
        criteria.where(hasVip);

        return entityManager.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult();
    }

    @Override
    public List<Integer> getAccountIds(VirtualIp virtualIp) {
        List<Integer> accountIds;
        String query = "select distinct(j.loadBalancer.accountId) from LoadBalancerJoinVip j where j.virtualIp.id = :vipId order by (j.loadBalancer.accountId)";
        accountIds = entityManager.createQuery(query).setParameter("vipId", virtualIp.getId()).getResultList();
        return accountIds;
    }
}
