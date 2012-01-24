package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ServiceUnavailableException;
import org.openstack.atlas.service.domain.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.*;

@Repository
@Transactional
public class VirtualIpv6Repository {

    final Log LOG = LogFactory.getLog(ClusterRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public VirtualIpv6 getById(Integer id) throws EntityNotFoundException {
        VirtualIpv6 vip = entityManager.find(VirtualIpv6.class, id);
        if (vip == null) {
            String message = Constants.VirtualIpNotFound;
            LOG.warn(message);
            throw new EntityNotFoundException(message);
        }
        return vip;
    }

    public List<Integer> getAccountIdsAlreadyShaHashed() {
        return entityManager.createQuery("select a.id from Account a").getResultList();
    }

    public List<Integer> getAccountIds() {
        return entityManager.createQuery("select distinct(lb.accountId) from LoadBalancer lb").getResultList();
    }

    public VirtualIpv6 getByAccountIdVipOctets(Integer accountId, Integer vipOctets) throws EntityNotFoundException {
        List<VirtualIpv6> v6List;
        String qStr = "SELECT v from VirtualIpv6 v where accountId=:aid and vipOctets=:vo";
        Query q = entityManager.createQuery(qStr);
        q.setParameter("aid", accountId);
        q.setParameter("vo", vipOctets);
        v6List = q.getResultList();
        if (v6List.size() != 1) {
            String format = "Vip not found for accountId=%d and vipOctets=%d\n";
            String msg = String.format(format, accountId, vipOctets);
            throw new EntityNotFoundException(msg);
        }
        return null;
    }

    public Integer getNextVipOctet(Integer accountId) {
        List<Integer> maxList;
        Integer max;
        int retry_count = 3;

        String qStr = "SELECT max(v.vipOctets) from VirtualIpv6 v where v.accountId=:aid";

        while (retry_count > 0) {
            retry_count--;
            try {
                maxList = entityManager.createQuery(qStr).setLockMode(LockModeType.PESSIMISTIC_WRITE).setParameter("aid", accountId).getResultList();
                max = maxList.get(0);
                if (max == null) {
                    max = 0;
                }
                max++; // The next VipOctet
                return max;
            } catch (PersistenceException e) {
                LOG.warn(String.format("Deadlock detected. %d retries left.", retry_count));
                if (retry_count <= 0) throw e;

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

        throw new ServiceUnavailableException("Too many create requests received. Please try again in a few moments.");
    }

    public Set<VirtualIpv6> getVirtualIpv6sByLoadBalancerId(Integer lid) {
        String qStr = "SELECT j.virtualIp from LoadBalancerJoinVip6 j where j.loadBalancer.id  = :lid";
        Query q = entityManager.createQuery(qStr).setParameter("lid", lid);
        return new HashSet<VirtualIpv6>(q.getResultList());
    }

    public List<VirtualIpv6> getVips6ByAccountId(Integer accountId) {
        List<VirtualIpv6> vips;
        String query = "select distinct(j.virtualIp) from LoadBalancerJoinVip6 j where j.loadBalancer.accountId = :accountId";
//        String query = "select distinct l.virtualIps from LoadBalancer l where l.accountId = :accountId";
        vips = entityManager.createQuery(query).setParameter("accountId", accountId).getResultList();
        return vips;
    }

    private void persist(Object v) {
        entityManager.persist(v);
    }

    private void merge(Object v) {
        entityManager.merge(v);
    }

    private void remove(Object v) {
        entityManager.remove(v);
    }

    public List<LoadBalancer> getLoadBalancersByVipId(Integer virtualIpv6Id) {
        List<LoadBalancer> lbs;
        String qStr = "select v.loadBalancer from LoadBalancerJoinVip6 "
                + "v where v.virtualIp.id=:vid";
        Query q = entityManager.createQuery(qStr);
        q.setParameter("vid", virtualIpv6Id);
        return q.getResultList();
    }

    public Map<Integer, List<LoadBalancer>> getPorts(Integer vid) {
        Map<Integer, List<LoadBalancer>> map = new TreeMap<Integer, List<LoadBalancer>>();
        List<Object> hResults;

        String query = "select j.virtualIp.id, j.loadBalancer.id, j.loadBalancer.accountId, j.loadBalancer.port " +
                "from LoadBalancerJoinVip6 j where j.virtualIp.id = :vid order by j.loadBalancer.port, j.loadBalancer.id";

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

            int sslPort = getSslPorts((Integer) row[1]);
            if ((sslPort != -1) && !map.containsKey(sslPort)) {
                map.put(sslPort, new ArrayList<LoadBalancer>());
                LoadBalancer lbssl = new LoadBalancer();
                lbssl.setId((Integer) row[1]);
                lbssl.setAccountId((Integer) row[2]);
                lbssl.setPort((Integer) row[3]);
                map.get(sslPort).add(lbssl);
            }
        }
        return map;
    }

    public int getSslPorts(Integer lbId) {
        Map<Integer, List<LoadBalancer>> map = new TreeMap<Integer, List<LoadBalancer>>();
        List<Object> hResults;

        String query = "select j.securePort "
                + "from SslTermination j where j.loadbalancer.id = :lbId";

        hResults = entityManager.createQuery(query).setParameter("lbId", lbId).getResultList();

        if (hResults == null || hResults.isEmpty()) {
            return -1;
        }

        return (Integer) hResults.get(0);
    }

    public void removeJoinRecord(LoadBalancerJoinVip6 loadBalancerJoinVip6) {
        loadBalancerJoinVip6 = entityManager.find(LoadBalancerJoinVip6.class, loadBalancerJoinVip6.getId());
        VirtualIpv6 virtualIpv6 = entityManager.find(VirtualIpv6.class, loadBalancerJoinVip6.getVirtualIp().getId());
        virtualIpv6.getLoadBalancerJoinVip6Set().remove(loadBalancerJoinVip6);
        entityManager.remove(loadBalancerJoinVip6);
    }

    public void deleteVirtualIp(VirtualIpv6 virtualIpv6) {
        virtualIpv6 = entityManager.find(VirtualIpv6.class, virtualIpv6.getId());
        entityManager.remove(virtualIpv6);
        LOG.info(String.format("IPv6 virtual Ip '%d' deleted.", virtualIpv6.getId()));
    }

    public List<LoadBalancerJoinVip6> getJoinRecordsForVip(VirtualIpv6 virtualIp) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoadBalancerJoinVip6> criteria = builder.createQuery(LoadBalancerJoinVip6.class);
        Root<LoadBalancerJoinVip6> lbJoinVipRoot = criteria.from(LoadBalancerJoinVip6.class);

        Predicate hasVip = builder.equal(lbJoinVipRoot.get(LoadBalancerJoinVip6_.virtualIp), virtualIp);

        criteria.select(lbJoinVipRoot);
        criteria.where(hasVip);
        return entityManager.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
    }

    public List<VirtualIpv6> getVipsByLoadBalancerId(Integer loadBalancerId) {
        List<VirtualIpv6> vips;
        String query = "select j.virtualIp from LoadBalancerJoinVip6 j where j.loadBalancer.id = :loadBalancerId";
//        String query = "select l.virtualIps from LoadBalancer l where l.id = :loadBalancerId";
        vips = entityManager.createQuery(query).setParameter("loadBalancerId", loadBalancerId).getResultList();
        return vips;
    }

    public Long getNumIpv6VipsForLoadBalancer(LoadBalancer loadBalancer) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<LoadBalancerJoinVip6> lbJoinVipRoot = criteria.from(LoadBalancerJoinVip6.class);

        Expression countExp = builder.count(lbJoinVipRoot.get(LoadBalancerJoinVip6_.virtualIp));
        Predicate hasVip = builder.equal(lbJoinVipRoot.get(LoadBalancerJoinVip6_.loadBalancer), loadBalancer);

        criteria.select(countExp);
        criteria.where(hasVip);

        return entityManager.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult();
    }

    public Account getLockedAccountRecord(Integer accountId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Account> criteria = builder.createQuery(Account.class);
        Root<Account> accountRoot = criteria.from(Account.class);

        Predicate recordWithId = builder.equal(accountRoot.get(Account_.id), accountId);

        criteria.select(accountRoot);
        criteria.where(recordWithId);
        return entityManager.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult();
    }
}
