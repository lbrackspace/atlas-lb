package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.common.Constants;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.OutOfVipsException;
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
public class VirtualIpRepository {

    private final Log LOG = LogFactory.getLog(VirtualIpRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public void persist(Object obj) {
        entityManager.persist(obj);
    }

    public List<VirtualIp> getVipsByAccountId(Integer accountId) {
        String query = "select distinct(j.virtualIp) from LoadBalancerJoinVip j where j.loadBalancer.accountId = :accountId";
        List<VirtualIp> vips = entityManager.createQuery(query).setParameter("accountId", accountId).getResultList();
        return vips;
    }

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
            throw new OutOfVipsException(Constants.OutOfVips);
        }

        vipCandidate.setAllocated(true);
        vipCandidate.setLastAllocation(Calendar.getInstance());
        entityManager.merge(vipCandidate);
        return vipCandidate;
    }

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
            throw new OutOfVipsException(Constants.OutOfVips);
        }

        vipCandidate.setAllocated(true);
        vipCandidate.setLastAllocation(Calendar.getInstance());
        entityManager.merge(vipCandidate);
        return vipCandidate;
    }

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