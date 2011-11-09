package org.openstack.atlas.service.domain.repository.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.common.ErrorMessages;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.OutOfVipsException;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

@Repository
@Transactional
public class VirtualIpRepositoryImpl implements VirtualIpRepository {
    private final Log LOG = LogFactory.getLog(VirtualIpRepositoryImpl.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public void persist(Object obj) {
        entityManager.persist(obj);
    }

    @Override
    public List<LoadBalancerJoinVip> getJoinRecordsForVip(VirtualIp virtualIp) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoadBalancerJoinVip> criteria = builder.createQuery(LoadBalancerJoinVip.class);
        Root<LoadBalancerJoinVip> lbJoinVipRoot = criteria.from(LoadBalancerJoinVip.class);

        Predicate hasVip = builder.equal(lbJoinVipRoot.get(LoadBalancerJoinVip_.virtualIp), virtualIp);

        criteria.select(lbJoinVipRoot);
        criteria.where(hasVip);
        return entityManager.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
    }

    @Override
    public List<VirtualIp> getVipsByAccountId(Integer accountId) {
        String query = "select distinct(j.virtualIp) from LoadBalancerJoinVip j where j.loadBalancer.accountId = :accountId";
        List<VirtualIp> vips = entityManager.createQuery(query).setParameter("accountId", accountId).getResultList();
        return vips;
    }

    @Override
    public List<VirtualIp> getVipsByLoadBalancerId(Integer loadBalancerId) {
        List<VirtualIp> vips;
        String query = "select j.virtualIp from LoadBalancerJoinVip j where j.loadBalancer.id = :loadBalancerId";
        vips = entityManager.createQuery(query).setParameter("loadBalancerId", loadBalancerId).getResultList();
        return vips;
    }

    @Override
    public void removeJoinRecord(LoadBalancerJoinVip loadBalancerJoinVip) {
        loadBalancerJoinVip = entityManager.find(LoadBalancerJoinVip.class, loadBalancerJoinVip.getId());
        VirtualIp virtualIp = entityManager.find(VirtualIp.class, loadBalancerJoinVip.getVirtualIp().getId());
        virtualIp.getLoadBalancerJoinVipSet().remove(loadBalancerJoinVip);
        entityManager.remove(loadBalancerJoinVip);
    }

    @Override
    public void deallocateVirtualIp(VirtualIp virtualIp) {
        virtualIp = entityManager.find(VirtualIp.class, virtualIp.getId());
        virtualIp.setAllocated(false);
        virtualIp.setLastDeallocation(Calendar.getInstance());
        entityManager.merge(virtualIp);
        LOG.info(String.format("Virtual Ip '%d' de-allocated.", virtualIp.getId()));
    }

    @Override
    public VirtualIp allocateIpv4VipBeforeDate(Cluster cluster, Calendar vipReuseTime, VirtualIpType vipType) throws OutOfVipsException {
        VirtualIp vipCandidate;
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<VirtualIp> criteria = builder.createQuery(VirtualIp.class);
        Root<VirtualIp> vipRoot = criteria.from(VirtualIp.class);

        Predicate isNotAllocated = builder.equal(vipRoot.get(VirtualIp_.isAllocated), false);
        Predicate lastDeallocationIsNull = builder.isNull(vipRoot.get(VirtualIp_.lastDeallocation));
        Predicate isBeforeLastDeallocation = builder.lessThan(vipRoot.get(VirtualIp_.lastDeallocation), vipReuseTime);
        Predicate isVipType = builder.equal(vipRoot.get(VirtualIp_.vipType), vipType);
        Predicate belongsToCluster = builder.equal(vipRoot.get(VirtualIp_.cluster), cluster);

        criteria.select(vipRoot);
        criteria.where(builder.and(isNotAllocated, isVipType, belongsToCluster, builder.or(lastDeallocationIsNull, isBeforeLastDeallocation)));

        try {
            vipCandidate = entityManager.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).setMaxResults(1).getSingleResult();
        } catch (Exception e) {
            LOG.error(e);
            throw new OutOfVipsException(ErrorMessages.OUT_OF_VIPS);
        }

        vipCandidate.setAllocated(true);
        vipCandidate.setLastAllocation(Calendar.getInstance());
        entityManager.merge(vipCandidate);
        return vipCandidate;
    }

    @Override
    public VirtualIp allocateIpv4VipAfterDate(Cluster cluster, Calendar vipReuseTime, VirtualIpType vipType) throws OutOfVipsException {
        VirtualIp vipCandidate;
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<VirtualIp> criteria = builder.createQuery(VirtualIp.class);
        Root<VirtualIp> vipRoot = criteria.from(VirtualIp.class);

        Predicate isNotAllocated = builder.equal(vipRoot.get(VirtualIp_.isAllocated), false);
        Predicate isAfterLastDeallocation = builder.greaterThan(vipRoot.get(VirtualIp_.lastDeallocation), vipReuseTime);
        Predicate isVipType = builder.equal(vipRoot.get(VirtualIp_.vipType), vipType);
        Predicate belongsToCluster = builder.equal(vipRoot.get(VirtualIp_.cluster), cluster);

        criteria.select(vipRoot);
        criteria.where(isNotAllocated, isAfterLastDeallocation, isVipType, belongsToCluster);

        try {
            vipCandidate = entityManager.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).setMaxResults(1).getSingleResult();
        } catch (Exception e) {
            LOG.error(e);
            throw new OutOfVipsException(ErrorMessages.OUT_OF_VIPS);
        }

        vipCandidate.setAllocated(true);
        vipCandidate.setLastAllocation(Calendar.getInstance());
        entityManager.merge(vipCandidate);
        return vipCandidate;
    }

    @Override
    public Map<Integer, List<LoadBalancer>> getPorts(Integer vid) {
        Map<Integer, List<LoadBalancer>> map = new TreeMap<Integer, List<LoadBalancer>>();
        List<Object> hResults;

        String query = "select j.virtualIp.id, j.loadBalancer.id, j.loadBalancer.accountId, j.loadBalancer.port " +
                "from LoadBalancerJoinVip j where j.virtualIp.id = :vid order by j.loadBalancer.port, j.loadBalancer.id";

        hResults = entityManager.createQuery(query).setParameter("vid", vid).getResultList();
        for (Object r : hResults) {
            Object[] row = (Object[]) r;
            Integer port = (Integer) row[3];
            if (!map.containsKey(port)) {
                map.put(port, new ArrayList<LoadBalancer>());
            }
            LoadBalancer lb = new LoadBalancer();
            lb.setId((Integer) row[1]);
            lb.setAccountId((Integer) row[2]);
            lb.setPort((Integer) row[3]);
            map.get(port).add(lb);
        }
        return map;
    }
}
