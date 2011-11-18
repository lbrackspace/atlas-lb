package org.openstack.atlas.rax.domain.repository.impl;

import org.openstack.atlas.rax.domain.repository.RaxVirtualIpv6Repository;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip6;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip6_;
import org.openstack.atlas.service.domain.repository.impl.VirtualIpv6RepositoryImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import javax.persistence.criteria.*;

@Primary
@Repository
@Transactional
public class RaxVirtualIpv6RepositoryImpl extends VirtualIpv6RepositoryImpl implements RaxVirtualIpv6Repository {

    @Override
    public Long getNumIpv6VipsForLoadBalancer(LoadBalancer loadBalancer) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<LoadBalancerJoinVip6> lbJoinVipRoot = criteria.from(LoadBalancerJoinVip6.class);

        Expression<Long> countExp = builder.count(lbJoinVipRoot.get(LoadBalancerJoinVip6_.virtualIp));
        Predicate hasVip = builder.equal(lbJoinVipRoot.get(LoadBalancerJoinVip6_.loadBalancer), loadBalancer);

        criteria.select(countExp);
        criteria.where(hasVip);

        return entityManager.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult();
    }
}
