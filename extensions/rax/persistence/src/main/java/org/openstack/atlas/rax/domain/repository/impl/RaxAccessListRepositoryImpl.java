package org.openstack.atlas.rax.domain.repository.impl;

import org.openstack.atlas.rax.domain.entity.RaxAccessList;
import org.openstack.atlas.rax.domain.repository.RaxAccessListRepository;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.DeletedStatusException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Primary
@Repository
@Transactional
public class RaxAccessListRepositoryImpl implements RaxAccessListRepository {

    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;

    public RaxAccessList getNetworkItemByAccountIdLoadBalancerIdNetworkItemId(Integer aid, Integer lid, Integer nid) throws EntityNotFoundException {
        List<RaxAccessList> al = null;
        String qStr = "SELECT a FROM RaxAccessList a WHERE a.loadbalancer.id = :lid AND a.loadbalancer.accountId = :aid AND a.id = :nid";

        if (lid == null || aid == null || nid == null) {
            throw new EntityNotFoundException("Null parameter Query rejected");
        }

        Query q = entityManager.createQuery(qStr);
        q.setParameter("aid", aid);
        q.setParameter("lid", lid);
        q.setParameter("nid", nid);
        q.setMaxResults(1);
        al = q.getResultList();
        if (al.size() != 1) {
            throw new EntityNotFoundException("Node not found");
        }
        return al.get(0);
    }

    public List<RaxAccessList> getAccessListByAccountIdLoadBalancerId(int accountId, int loadbalancerId, Integer offset,
                                Integer limit, Integer marker) throws EntityNotFoundException, DeletedStatusException {
        LoadBalancer lb = loadBalancerRepository.getByIdAndAccountId(loadbalancerId, accountId);
        List<RaxAccessList> accessList = new ArrayList<RaxAccessList>();
        if (lb.getStatus().equals("DELETED")) {
            throw new DeletedStatusException("The loadbalancer is marked as deleted.");
        }
        Query query = entityManager.createQuery("FROM RaxAccessList a WHERE a.loadbalancer.id = :lid AND a.loadbalancer.accountId = :aid")
                .setParameter("lid", loadbalancerId).setParameter("aid", accountId);

        if (offset == null) {
            offset = 0;
        }
        if (limit == null || limit > 100) {
            limit = 100;
        }
        if (marker != null) {
            query = entityManager.createQuery("FROM RaxAccessList a WHERE a.loadbalancer.id = :lid AND a.loadbalancer.accountId = :aid AND a.id >= :accessId")
                    .setParameter("lid", loadbalancerId).setParameter("aid", accountId).setParameter("accessId", marker);
        }
        query = query.setFirstResult(offset).setMaxResults(limit);

        accessList = query.getResultList();
        return accessList;
    }
}
