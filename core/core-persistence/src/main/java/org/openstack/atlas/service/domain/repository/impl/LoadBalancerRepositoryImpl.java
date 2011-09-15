package org.openstack.atlas.service.domain.repository.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.common.Constants;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Transactional
public class LoadBalancerRepositoryImpl implements LoadBalancerRepository {

    final Log LOG = LogFactory.getLog(LoadBalancerRepositoryImpl.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    @Override
    public LoadBalancer getById(Integer id) throws EntityNotFoundException {
        LoadBalancer loadBalancer = entityManager.find(LoadBalancer.class, id);
        if (loadBalancer == null) {
            String message = Constants.LoadBalancerNotFound;
            LOG.warn(message);
            throw new EntityNotFoundException(message);
        }
        return loadBalancer;
    }

    @Override
    public LoadBalancer getByIdAndAccountId(Integer id, Integer accountId) throws EntityNotFoundException {
        LoadBalancer loadBalancer = getById(id);
        if (!loadBalancer.getAccountId().equals(accountId)) {
            String message = Constants.LoadBalancerNotFound;
            LOG.warn(message);
            throw new EntityNotFoundException(message);
        }

        return loadBalancer;
    }

    @Override
    public List<LoadBalancer> getByAccountId(Integer accountId) {
        return entityManager.createQuery("SELECT lb FROM LoadBalancer lb WHERE lb.accountId = :accountId")
                .setParameter("accountId", accountId)
                .getResultList();
    }


    @Override
    public LoadBalancer create(LoadBalancer loadBalancer) {

        final Set<LoadBalancerJoinVip> lbJoinVipsToLink = loadBalancer.getLoadBalancerJoinVipSet();
        loadBalancer.setLoadBalancerJoinVipSet(null);
        final Set<LoadBalancerJoinVip6> lbJoinVip6sToLink = loadBalancer.getLoadBalancerJoinVip6Set();
        loadBalancer.setLoadBalancerJoinVip6Set(null);

        loadBalancer = setLbIdOnChildObjects(loadBalancer);

        Calendar current = Calendar.getInstance();
        loadBalancer.setCreated(current);
        loadBalancer.setUpdated(current);
        loadBalancer = entityManager.merge(loadBalancer);

        // Now attach loadbalancer to vips
        for (LoadBalancerJoinVip lbJoinVipToLink : lbJoinVipsToLink) {
            entityManager.merge(lbJoinVipToLink.getVirtualIp());
            LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip(loadBalancer.getPort(), loadBalancer, lbJoinVipToLink.getVirtualIp());
            entityManager.merge(loadBalancerJoinVip);
        }

/*        for(LoadBalancerJoinVip6 lbJoinVipToLink : lbJoinVip6sToLink) {
            LoadBalancerJoinVip6 jv = new LoadBalancerJoinVip6(loadBalancer.getPort(), loadBalancer, lbJoinVipToLink.getVirtualIp());
            entityManager.persist(jv);
        }*/

        loadBalancer.setLoadBalancerJoinVip6Set(lbJoinVip6sToLink);
        entityManager.flush();

        Set<LoadBalancerJoinVip6> loadBalancerJoinVip6SetConfig = loadBalancer.getLoadBalancerJoinVip6Set();
        loadBalancer.setLoadBalancerJoinVip6Set(null);
        Set<LoadBalancerJoinVip6> newLbVip6Setconfig = new HashSet<LoadBalancerJoinVip6>();
        loadBalancer.setLoadBalancerJoinVip6Set(newLbVip6Setconfig);
        for (LoadBalancerJoinVip6 jv6 : loadBalancerJoinVip6SetConfig) {
            LoadBalancerJoinVip6 jv = new LoadBalancerJoinVip6(loadBalancer.getPort(), loadBalancer, jv6.getVirtualIp());
            entityManager.persist(jv);
        }
        return loadBalancer;
    }

/*    @Transactional
    private void joinIpv6OnLoadBalancer(LoadBalancer lb) {
        Set<LoadBalancerJoinVip6> loadBalancerJoinVip6SetConfig = lb.getLoadBalancerJoinVip6Set();
        lb.setLoadBalancerJoinVip6Set(null);
        Set<LoadBalancerJoinVip6> newLbVip6Setconfig = new HashSet<LoadBalancerJoinVip6>();
        lb.setLoadBalancerJoinVip6Set(newLbVip6Setconfig);
        for (LoadBalancerJoinVip6 jv6 : loadBalancerJoinVip6SetConfig) {
            LoadBalancerJoinVip6 jv = new LoadBalancerJoinVip6(lb.getPort(), lb, jv6.getVirtualIp());
            entityManager.persist(jv);
        }
    }*/

    protected LoadBalancer setLbIdOnChildObjects(final LoadBalancer loadBalancer) {
        if (loadBalancer.getNodes() != null) {
            for (Node node : loadBalancer.getNodes()) {
                node.setLoadBalancer(loadBalancer);
            }
        }

        if (loadBalancer.getConnectionThrottle() != null) {
            loadBalancer.getConnectionThrottle().setLoadBalancer(loadBalancer);
        }
        if (loadBalancer.getHealthMonitor() != null) {
            loadBalancer.getHealthMonitor().setLoadBalancer(loadBalancer);
        }
        return loadBalancer;
    }

    @Override
    public LoadBalancer update(LoadBalancer loadBalancer) {
        final Set<LoadBalancerJoinVip> lbJoinVipsToLink = loadBalancer.getLoadBalancerJoinVipSet();
        loadBalancer.setLoadBalancerJoinVipSet(null);

        loadBalancer.setUpdated(Calendar.getInstance());
        loadBalancer = entityManager.merge(loadBalancer);

        // Now attach loadbalancer to vips
        for (LoadBalancerJoinVip lbJoinVipToLink : lbJoinVipsToLink) {
            VirtualIp virtualIp = entityManager.find(VirtualIp.class, lbJoinVipToLink.getVirtualIp().getId());
            LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip(loadBalancer.getPort(), loadBalancer, virtualIp);
            entityManager.merge(loadBalancerJoinVip);
            entityManager.merge(lbJoinVipToLink.getVirtualIp());
        }

        entityManager.flush();
        return loadBalancer;
    }

    @Override
    public Integer getNumNonDeletedLoadBalancersForAccount(Integer accountId) {
        Query query = entityManager.createNativeQuery(
                "select count(account_id) from load_balancer where status != 'DELETED' and account_id = :accountId").setParameter("accountId", accountId);

        return ((BigInteger) query.getSingleResult()).intValue();
    }

    public boolean testAndSetStatus(Integer accountId, Integer loadbalancerId, LoadBalancerStatus statusToChangeTo, boolean allowConcurrentModifications) throws EntityNotFoundException, UnprocessableEntityException {
        String qStr = "from LoadBalancer lb where lb.accountId=:aid and lb.id=:lid";
        List<LoadBalancer> lbList;
        Query q = entityManager.createQuery(qStr).setLockMode(LockModeType.PESSIMISTIC_WRITE).
                setParameter("aid", accountId).
                setParameter("lid", loadbalancerId);
        lbList = q.getResultList();
        if (lbList.size() < 1) {
            throw new EntityNotFoundException(Constants.LoadBalancerNotFound);
        }

        LoadBalancer lb = lbList.get(0);
        if (lb.getStatus().equals(LoadBalancerStatus.DELETED)) throw new UnprocessableEntityException(Constants.LoadBalancerDeleted);
        final boolean isActive = lb.getStatus().equals(LoadBalancerStatus.ACTIVE);
        final boolean isPendingOrActive = lb.getStatus().equals(LoadBalancerStatus.PENDING_UPDATE) || isActive;

        if(allowConcurrentModifications ? isPendingOrActive : isActive) {
            lb.setStatus(statusToChangeTo);
            lb.setUpdated(Calendar.getInstance());
            entityManager.merge(lb);
            return true;
        }

        return false;
    }

    public void updatePortInJoinTable(LoadBalancer lb) {
        String queryString = "from LoadBalancerJoinVip where loadBalancer.id = :lbId";
        Query query = entityManager.createQuery(queryString).setParameter("lbId", lb.getId());
        LoadBalancerJoinVip loadBalancerJoinVip = (LoadBalancerJoinVip) query.getSingleResult();
        loadBalancerJoinVip.setPort(lb.getPort());
        entityManager.merge(loadBalancerJoinVip);
    }

    public boolean canUpdateToNewPort(Integer newPort, Set<LoadBalancerJoinVip> setToCheckAgainst) {
        Set<VirtualIp> vipsToCheckAgainst = new HashSet<VirtualIp>();

        for (LoadBalancerJoinVip loadBalancerJoinVip : setToCheckAgainst) {
            vipsToCheckAgainst.add(loadBalancerJoinVip.getVirtualIp());
        }

        String queryString = "select j from LoadBalancerJoinVip j where j.virtualIp in (:vips)";
        Query query = entityManager.createQuery(queryString).setParameter("vips", vipsToCheckAgainst);

        List<LoadBalancerJoinVip> entriesWithPortsToCheckAgainst = query.getResultList();

        for (LoadBalancerJoinVip entryWithPortToCheckAgainst : entriesWithPortsToCheckAgainst) {
            if (entryWithPortToCheckAgainst.getPort().equals(newPort)) {
                return false;
            }
        }

        return true;
    }

    public void setStatus(LoadBalancer loadBalancer,LoadBalancerStatus status) throws EntityNotFoundException{
        String qStr = "from LoadBalancer lb where lb.accountId=:aid and lb.id=:lid";
        List<LoadBalancer> lbList;
        Query q = entityManager.createQuery(qStr).setLockMode(LockModeType.PESSIMISTIC_WRITE).
                setParameter("aid", loadBalancer.getAccountId()).
                setParameter("lid", loadBalancer.getId());
        lbList = q.getResultList();
        if (lbList.size() < 1) {
            throw new EntityNotFoundException(Constants.LoadBalancerNotFound);
        }


        lbList.get(0).setStatus(status);
        entityManager.merge(lbList.get(0));
    }
}
