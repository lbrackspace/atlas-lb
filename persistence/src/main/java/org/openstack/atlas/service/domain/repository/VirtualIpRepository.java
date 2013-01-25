package org.openstack.atlas.service.domain.repository;

import javassist.tools.rmi.ObjectNotFoundException;
import org.openstack.atlas.lb.helpers.ipstring.IPv4Range;
import org.openstack.atlas.lb.helpers.ipstring.IPv4Ranges;
import org.openstack.atlas.lb.helpers.ipstring.IPv4ToolSet;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPStringException;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.OutOfVipsException;
import org.openstack.atlas.service.domain.pojos.VirtualIpBlock;
import org.openstack.atlas.service.domain.pojos.VirtualIpBlocks;
import org.openstack.atlas.service.domain.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.math.BigInteger;
import java.util.*;

@Repository
@Transactional
public class VirtualIpRepository {

    private final Log LOG = LogFactory.getLog(VirtualIpRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public VirtualIpRepository() {
    }

    public VirtualIpRepository(EntityManager em) {
        this.entityManager = em;
    }

    public VirtualIpv6 getVirtualIpv6BytClusterAccountOctet(Integer cid, Integer aid, Integer vo) throws EntityNotFoundException {
        List<VirtualIpv6> vips = new ArrayList<VirtualIpv6>();
        if (cid == null) {
            throw new EntityNotFoundException("Cluster not found");
        }

        if (aid == null) {
            throw new EntityNotFoundException("Account not found");
        }

        if (vo == null) {
            throw new EntityNotFoundException("vipOctets is null");
        }

        String qStr = "select v from VirtualIpv6 v where v.cluster.id=:cid and v.accountId=:aid and v.vipOctets=:vo";
        Query q = entityManager.createQuery(qStr).setParameter("cid", cid).setParameter("aid", aid).setParameter("vo", vo);
        vips = q.getResultList();
        if (vips.isEmpty()) {
            throw new EntityNotFoundException("Vip not found");
        }
        return vips.get(0);
    }

    public List<VirtualIp> listFreeVirtualIps() throws OutOfVipsException {
        List<VirtualIp> vips;
        VirtualIp freeVip;

        LOG.info("About to execute query for 'getAvailableVirtualIp()'");

        String query = "select v from VirtualIp v where v.lastDeallocation > v.lastAllocation or v.last_allocation is null"
                + "order by v.lastDeallocation asc";

        vips = entityManager.createQuery(query).getResultList();
        if (vips.isEmpty()) {
            LOG.error("No available virtual ips.");
            throw new OutOfVipsException("No available virtual ips. Please contact support.");
        }
        return vips;
    }

    public List<VirtualIp> getAll() {
        return getAll(null);
    }

    public Integer getNumUniqueVipsForAccount(Integer accountId, VirtualIpType type) {
        Query query = entityManager.createNativeQuery("select count(distinct j.virtualip_id) from loadbalancer_virtualip j, loadbalancer l, virtual_ip_ipv4 v where l.id = j.loadbalancer_id and v.id = j.virtualip_id and v.type = :virtualIpType and l.account_id = :accountId").setParameter("virtualIpType", type.toString()).setParameter("accountId", accountId.toString());

        return ((BigInteger) query.getSingleResult()).intValue();
    }

    public List<VirtualIp> getVipsByLoadBalancerId(Integer loadBalancerId) {
        List<VirtualIp> vips;
        String query = "select j.virtualIp from LoadBalancerJoinVip j where j.loadBalancer.id = :loadBalancerId";
//        String query = "select l.virtualIps from LoadBalancer l where l.id = :loadBalancerId";
        vips = entityManager.createQuery(query).setParameter("loadBalancerId", loadBalancerId).getResultList();
        return vips;
    }

    public List<VirtualIp> getVipsByClusterId(Integer clusterId) {
        List<VirtualIp> vips;
        String query = "select j from VirtualIp j where j.cluster.id = :clusterId";
        vips = entityManager.createQuery(query).setParameter("clusterId", clusterId).getResultList();
        return vips;
    }

    public List<VirtualIp> getVipsByAccountId(Integer accountId) {
        List<VirtualIp> vips;
        String query = "select distinct(j.virtualIp) from LoadBalancerJoinVip j where j.loadBalancer.accountId = :accountId";
        vips = entityManager.createQuery(query).setParameter("accountId", accountId).getResultList();
        return vips;
    }

    public List<LoadBalancer> getLoadBalancerByVip6Address(Integer vip6Id) {
        List<LoadBalancer> loadBalancers;
        String query = "select j.loadBalancer from LoadBalancerJoinVip6 j where j.virtualIp.id=:vipId";
        loadBalancers = entityManager.createQuery(query).setParameter("vipId", vip6Id).getResultList();
        return loadBalancers;
    }

    public List<LoadBalancer> getLoadBalancersByVipId(Integer vipId) {
        List<LoadBalancer> loadBalancers;
        String query = "select v.loadBalancer from LoadBalancerJoinVip v where v.virtualIp.id = :vipId";
        loadBalancers = entityManager.createQuery(query).setParameter("vipId", vipId).getResultList();
        return loadBalancers;
    }

    public List<LoadBalancer> getLoadBalancerByVipAddress(String address) {
        List<LoadBalancer> loadBalancers;
        Integer vipId;
        String query = "select v.id from VirtualIp v where v.ipAddress = :address";

        try {
            vipId = (Integer) entityManager.createQuery(query).setParameter("address", address).getSingleResult();
        } catch (NoResultException e) {
            return new ArrayList<LoadBalancer>();
        }

        loadBalancers = getLoadBalancersByVipId(vipId);

        return loadBalancers;
    }

    public List<VirtualIp> getAll(String orderBy, Integer... p) {
        List<VirtualIp> vips;
        String queryStr = "from VirtualIp v left join fetch v.loadBalancerJoinVipSet group by v";

        if (orderBy != null) {
            queryStr = String.format("%s order by v.%s", queryStr, orderBy);
        }
        Query query = entityManager.createQuery(queryStr);
        if (p.length >= 2) {
            Integer offset = p[0];
            Integer limit = p[1];
            if (offset == null) {
                offset = 0;
            }
            if (limit == null) {
                limit = 100;
            }
            query = query.setFirstResult(offset).setMaxResults(limit);
        }
        vips = query.getResultList();
        return vips;
    }

    //moved this to cluster repository
    public Cluster getClusterById(Integer id) throws EntityNotFoundException {
        List<Cluster> cl = entityManager.createQuery("from Cluster c where c.id = :id").setParameter("id",
                id).getResultList();
        if (cl.isEmpty()) {
            String errMsg = String.format("Cannot access cluster {id=%d}", id);
            LOG.warn(errMsg);
            throw new EntityNotFoundException(errMsg);
        }
        return cl.get(0);
    }

    public List<LoadBalancer> getLbsByVirtualIp4Blocks(VirtualIpBlocks vblocks) throws IPStringException {
        List<LoadBalancer> out = new ArrayList<LoadBalancer>();
        IPv4Ranges ipv4Ranges = ip4Blocks2ip4Ranges(vblocks);
        List<Object> hResults;

        LoadBalancer lb;

        String qStr = "select j.virtualIp.ipAddress, j.virtualIp.id, j.virtualIp.accountId, j.loadBalancer.port, j.virtualIp.ipVersion, j.virtualIp.vipType "
                + "from LoadBalancerJoinVip j order by j.loadBalancer.accountId, j.loadBalancer.id, j.loadBalancer.port, j.virtualIp.id";

//        String qStr = "select v.ipAddress,v.id,l.id,l.accountId,l.port, "
//                + "v.ipVersion, v.vipType "
//                + "from VirtualIp v join v.loadBalancers l "
//                + "order by l.accountId,l.id,l.port,v.id";
        hResults = entityManager.createQuery(qStr).getResultList();
        Integer clid = -1;
        lb = new LoadBalancer();
        for (Object r : hResults) {
            Object[] row = (Object[]) r;
            String ip = (String) row[0];
            Integer vid = (Integer) row[1];
            Integer lid = (Integer) row[2];
            Integer aid = (Integer) row[3];
            Integer port = (Integer) row[4];
            IpVersion ipVersion = (IpVersion) row[5];
            VirtualIpType vType = (VirtualIpType) row[6];

            if (ipv4Ranges.contains(ip)) {
                if (clid != lid) {
                    clid = lid;
                    lb = new LoadBalancer();
                    lb.setAccountId(aid);
                    lb.setId(lid);
                    lb.setPort(port);
                    lb.setLoadBalancerJoinVipSet(new HashSet<LoadBalancerJoinVip>());
                    out.add(lb);
                }
                VirtualIp vip = new VirtualIp();
                vip.setIpAddress(ip);
                vip.setId(vid);
//                vip.setIpVersion(ipVersion);
                vip.setVipType(vType);

                LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip(lb.getPort(), lb, vip);
                lb.getLoadBalancerJoinVipSet().add(loadBalancerJoinVip);
            }
        }
        return out;
    }

    public void persist(Object obj) {
        entityManager.persist(obj);
    }

    public void merge(Object obj) {
        entityManager.merge(obj);
    }

    public void remove(Object obj) {
        entityManager.remove(obj);
    }

    public void refresh(Object obj) {
        entityManager.refresh(obj);
    }

    //ToDo: for Debugging only remove when in production
    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public String getStackTrace(Exception ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Exception: %s:%s\n", ex.getMessage(), ex.getClass().getName()));
        for (StackTraceElement se : ex.getStackTrace()) {
            sb.append(String.format("%s\n", se.toString()));
        }
        return sb.toString();
    }

    private IPv4Ranges ip4Blocks2ip4Ranges(VirtualIpBlocks vBlocks) throws IPStringException {
        long lo;
        long hi;
        IPv4Ranges ipv4Ranges = new IPv4Ranges();
        for (VirtualIpBlock vBlock : vBlocks.getVirtualIpBlocks()) {
            IPv4Range ipv4Range = new IPv4Range();
            lo = IPv4ToolSet.ip2long(vBlock.getFirstIp());
            hi = IPv4ToolSet.ip2long(vBlock.getLastIp());
            ipv4Range.setLo(lo);
            ipv4Range.setHi(hi);
            ipv4Ranges.add(ipv4Range);
        }
        return ipv4Ranges;
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

    public void deallocateVirtualIp(VirtualIp virtualIp) {
        virtualIp = entityManager.find(VirtualIp.class, virtualIp.getId());
        virtualIp.setAllocated(false);
        virtualIp.setLastDeallocation(Calendar.getInstance());
        entityManager.merge(virtualIp);
        LOG.info(String.format("Virtual Ip '%d' de-allocated.", virtualIp.getId()));
    }

    public List<LoadBalancerJoinVip> getJoinRecordsForVip(VirtualIp virtualIp) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoadBalancerJoinVip> criteria = builder.createQuery(LoadBalancerJoinVip.class);
        Root<LoadBalancerJoinVip> lbJoinVipRoot = criteria.from(LoadBalancerJoinVip.class);

        Predicate hasVip = builder.equal(lbJoinVipRoot.get(LoadBalancerJoinVip_.virtualIp), virtualIp);

        criteria.select(lbJoinVipRoot);
        criteria.where(hasVip);
        return entityManager.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
    }

    public Long getNumLoadBalancersAttachedToVip(VirtualIp virtualIp) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<LoadBalancerJoinVip> lbJoinVipRoot = criteria.from(LoadBalancerJoinVip.class);

        Expression countExp = builder.count(lbJoinVipRoot.get(LoadBalancerJoinVip_.loadBalancer));
        Predicate hasVip = builder.equal(lbJoinVipRoot.get(LoadBalancerJoinVip_.virtualIp), virtualIp);

        criteria.select(countExp);
        criteria.where(hasVip);

        return entityManager.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult();
    }

    public Long getNumIpv4VipsForLoadBalancer(LoadBalancer loadBalancer) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<LoadBalancerJoinVip> lbJoinVipRoot = criteria.from(LoadBalancerJoinVip.class);

        Expression countExp = builder.count(lbJoinVipRoot.get(LoadBalancerJoinVip_.virtualIp));
        Predicate hasVip = builder.equal(lbJoinVipRoot.get(LoadBalancerJoinVip_.loadBalancer), loadBalancer);

        criteria.select(countExp);
        criteria.where(hasVip);

        return entityManager.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult();
    }

    public void removeJoinRecord(LoadBalancerJoinVip loadBalancerJoinVip) {
        loadBalancerJoinVip = entityManager.find(LoadBalancerJoinVip.class, loadBalancerJoinVip.getId());
        VirtualIp virtualIp = entityManager.find(VirtualIp.class, loadBalancerJoinVip.getVirtualIp().getId());
        virtualIp.getLoadBalancerJoinVipSet().remove(loadBalancerJoinVip);
        entityManager.remove(loadBalancerJoinVip);
    }

    public void deleteVirtualIp(VirtualIp virtualIp) {
        entityManager.remove(virtualIp);
    }

    public VirtualIp getById(Integer id) throws EntityNotFoundException {
        VirtualIp virtualIp = entityManager.find(VirtualIp.class, id);
        if (virtualIp == null) {
            String message = Constants.VirtualIpNotFound;
            LOG.warn(message);
            throw new EntityNotFoundException(message);
        }
        return virtualIp;
    }

    public List<Integer> getAccountIds(VirtualIp virtualIp) {
        List<Integer> accountIds;
        String query = "select distinct(j.loadBalancer.accountId) from LoadBalancerJoinVip j where j.virtualIp.id = :vipId order by (j.loadBalancer.accountId)";
        accountIds = entityManager.createQuery(query).setParameter("vipId", virtualIp.getId()).getResultList();
        return accountIds;
    }

    public List<Integer> getAccountBySha1Sum(String sha1) {
        List<Integer> accountIds;
        String qStr = "select a.id from Account a where a.sha1SumForIpv6=:sha";
        accountIds = entityManager.createQuery(qStr).setParameter("sha", sha1).getResultList();
        return accountIds;
    }

    public Map<Integer, List<LoadBalancer>> getPorts(Integer vid) {
        Map<Integer, List<LoadBalancer>> map = new TreeMap<Integer, List<LoadBalancer>>();
        List<Object> hResults;

        String query = "select j.virtualIp.id, j.loadBalancer.id, j.loadBalancer.accountId, j.loadBalancer.port "
                + "from LoadBalancerJoinVip j where j.virtualIp.id = :vid order by j.loadBalancer.port, j.loadBalancer.id";

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
}
