package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterStatus;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.ClusterStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.Customer;
import org.openstack.atlas.service.domain.pojos.LoadBalancerCountByAccountIdClusterId;
import org.openstack.atlas.service.domain.pojos.VirtualIpAvailabilityReport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.*;

@Repository
@Transactional
public class ClusterRepository  {

    final Log LOG = LogFactory.getLog(ClusterRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;
     public Cluster getById(Integer id) throws EntityNotFoundException {
        Cluster cl = entityManager.find(Cluster.class, id);
        if (cl == null) {
            String errMsg = String.format("Cannot access cluster {id=%d}", id);
            LOG.warn(errMsg);
            throw new EntityNotFoundException(errMsg);
        }
        return cl;
    }

    public Cluster getByName(String name) throws EntityNotFoundException {
        List<Cluster> cls = entityManager.createQuery("SELECT cl FROM Cluster cl where cluster.name = :name").setParameter("name", name).getResultList();
        if (cls != null && cls.size() > 0) {
            return cls.get(0);
        } else {
            return null;
        }
    }

    public List<Cluster> getAll(Integer... p) {
        List<Cluster> clusters = new ArrayList<Cluster>();
        Query query = entityManager.createQuery("SELECT c FROM Cluster c");
        if (p.length >= 2) {
            Integer offset = p[0];
            Integer limit = p[1];
            if (offset == null) {
                offset = 0;
            }
            if (limit == null || limit > 100) {
                limit = 100;
            }
            query = query.setFirstResult(offset).setMaxResults(limit);
        }
        clusters = query.getResultList();
        return clusters;
    }
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

    public void save(Cluster cluster) {
        entityManager.persist(cluster);
    }

    public void delete(Cluster cluster) {
        cluster = entityManager.merge(cluster);
        entityManager.remove(cluster);

    }

    public Cluster update(Cluster cluster) {
        LOG.info("Updating Cluster " + cluster.getId() + "...");
        cluster = entityManager.merge(cluster);
        entityManager.flush();
        return cluster;
    }

    public List<VirtualIp> getVirtualIps(Integer clusterId, Integer... p) {
        List<VirtualIp> vips;

        String queryStr = "SELECT vIp FROM VirtualIp vIp where cluster.id = :clusterId";
        Query query = entityManager.createQuery(queryStr);
        if (p.length >= 2) {
            Integer offset = p[0];
            Integer limit = p[1];
            if (offset == null) {
                offset = 0;
            }
            if (limit == null || limit > 100) {
                limit = 100;
            }

            query = query.setFirstResult(offset).setMaxResults(limit);
        }
        vips = query.setParameter("clusterId", clusterId).getResultList();
        return vips;
    }

    public Integer getVirtualIpCountByType(Integer clusterId, String type) {
        return (entityManager.createQuery("SELECT count(vIp) FROM VirtualIp vIp where cluster.id = :clusterId and type = :type").setParameter("clusterId", clusterId).setParameter("type", type).getResultList()).size();
    }

    public Integer getAssignedVirtualIpCountByType(Integer clusterId, String type) {
         String sql = "select count(distinct(l.virtualip_id)) from loadbalancer_virtualip  l , loadbalancer lb, host h, virtual_ip_ipv4 vip "+
                       " where l.loadbalancer_id = lb.id "+
                       " and lb.host_id = h.id "+
                        " and l.virtualip_id = vip.id "+
                        " and vip.type = :type "+
                        " and h.cluster_id = :clusterId ";
        Query qry = entityManager.createNativeQuery(sql);
        qry.setParameter("clusterId", clusterId);
        qry.setParameter("type", type);
        List<BigInteger> results = qry.getResultList();
        if (results != null && results.size() > 0) {
            results.get(0).longValue();
        }
        return 0;
    }

    public List<Host> getHosts(Integer clusterId) {
        String hqlStr = "from Host h where h.cluster.id = :clusterId";
        Query q = entityManager.createQuery(hqlStr).setParameter("clusterId", clusterId);
        List<Host> hosts = q.getResultList();
        return hosts;
    }

    public List<Host> getAllHosts() {
        String hqlStr = "from Host h where h.hostStatus in ('ACTIVE_TARGET', 'FAILOVER') ";
        List<Host> hosts;
        hosts = entityManager.createQuery(hqlStr).getResultList();
        return hosts;

    }

    public List<Integer> getLoadBalancerIdsForCluster(Integer clusterId) {
        List<Integer> ids = new ArrayList();
        String sql = "SELECT lb.id from LoadBalancer lb where lb.host.id in (SELECT h.id from Host h where h.cluster.id = :id)";
        Query qry = entityManager.createQuery(sql);
        qry.setParameter("id", clusterId);
        List values = qry.getResultList();
        for (Object i : values) {
            ids.add((Integer) (i));

        }
        return ids;
    }

    public boolean isAccountInCluster(Integer clusterId, Integer accountId) {
        List<Integer> ids = new ArrayList();
        String sql = "SELECT lb.id from LoadBalancer lb where lb.accountId = :accountId and lb.host.id in (SELECT h.id from Host h where h.cluster.id = :clusterId)";
        Query qry = entityManager.createQuery(sql);
        qry.setParameter("clusterId", clusterId);
        qry.setParameter("accountId", accountId);
        List results = qry.getResultList();
        if (results != null && results.size() > 0) {
            return true;
        }
        return false;
    }

    public long getUnassignedVirtualIpCountByTyep(Integer clusterId, String type) {
        String sql = "select count(id) from virtual_ip_ipv4 vip where type = :type and vip.cluster_id = :id"
                + " and not exists "
                + " (select * from loadbalancer_virtualip l where l.virtualip_id = vip.id)";
        Query qry = entityManager.createNativeQuery(sql);
        qry.setParameter("id", clusterId);
        qry.setParameter("type", type);
        List<BigInteger> results = qry.getResultList();
        if (results != null && results.size() > 0) {
            return results.get(0).longValue();
        }
        return 0;

    }

    public long getAllocatedVIPCountFor7days(Integer clusterId, String type) {
        Calendar now = Calendar.getInstance();

        Calendar earlier = (Calendar) now.clone();
        earlier.add(Calendar.DATE, -7);

        String sql = "select count(id) from virtual_ip_ipv4  where type = :type and cluster_id = :id and last_allocation <= :now and last_allocation > :earlier "
                + " and exists "
                + " (select * from loadbalancer_virtualip l where l.virtualip_id = virtual_ip_ipv4.id)";
        Query qry = entityManager.createNativeQuery(sql);
        qry.setParameter("id", clusterId);
        qry.setParameter("type", type);
        qry.setParameter("now", now);
        qry.setParameter("earlier", earlier);
        List<BigInteger> results = qry.getResultList();
        if (results != null && results.size() > 0) {
            results.get(0).longValue();
        }
        return 0;

    }

    public long getUnAllocatedVIPCountFor7days(Integer clusterId, String type) {
        Calendar now = Calendar.getInstance();

        Calendar earlier = (Calendar) now.clone();
        earlier.add(Calendar.DATE, -7);

        String sql = "select count(id) from virtual_ip_ipv4 vip where type = :type and vip.cluster_id = :id and vip.last_allocation <= :now and vip.last_allocation > :earlier "
                + " and not exists "
                + " (select * from loadbalancer_virtualip l where l.virtualip_id = vip.id)";
        Query qry = entityManager.createNativeQuery(sql);
        qry.setParameter("id", clusterId);
        qry.setParameter("type", type);
        qry.setParameter("now", now);
        qry.setParameter("earlier", earlier);
        List<BigInteger> results = qry.getResultList();
        if (results != null && results.size() > 0) {
            return results.get(0).longValue();
        }
        return 0;

    }

    public long getDeallocatedVirtualIpCounForCluster(Integer clusterId, String type) {
        String sql = "select count(*) from VirtualIp vip where type = :type and vip.cluster.id = :id and vip.lastDeallocation <= :now and vip.lastDeallocation > :earlier ";
        Calendar now = Calendar.getInstance();
        Calendar earlier = (Calendar) now.clone();
        earlier.add(Calendar.HOUR, -24);

        Query qry = entityManager.createQuery(sql);
        qry.setParameter("id", clusterId);
        qry.setParameter("type", type);
        qry.setParameter("now", now);
        qry.setParameter("earlier", earlier);
        List<Long> results = qry.getResultList();
        if (results != null && results.size() > 0) {
            return results.get(0).longValue();
        }
        return 0;
    }

    public long getAllocatedVirtualIpCounForCluster(Integer clusterId, String type) {
        String sql = "select count(*) from VirtualIp vip where type = :type and vip.cluster.id = :id and vip.lastAllocation <= :now and vip.lastAllocation > :earlier ";
        Calendar now = Calendar.getInstance();
        Calendar earlier = (Calendar) now.clone();
        earlier.add(Calendar.HOUR, -24);

        Query qry = entityManager.createQuery(sql);
        qry.setParameter("id", clusterId);
        qry.setParameter("type", type);
        qry.setParameter("now", now);
        qry.setParameter("earlier", earlier);
        List<Long> results = qry.getResultList();
        if (results != null && results.size() > 0) {
            return results.get(0).longValue();
        }
        return 0;
    }

    public long getWeeklyAllocatedVirtualIpCount(Integer clusterId, String type) {
        String sql = "select count(*) from VirtualIp vip where type = :type and vip.cluster.id = :id and vip.lastAllocation <= :now and vip.lastAllocation > :earlier ";
        Calendar now = Calendar.getInstance();
        Calendar earlier = (Calendar) now.clone();
        earlier.add(Calendar.DATE, -7);

        Query qry = entityManager.createQuery(sql);
        qry.setParameter("id", clusterId);
        qry.setParameter("type", type);
        qry.setParameter("now", now);
        qry.setParameter("earlier", earlier);
        List<Long> results = qry.getResultList();
        if (results != null && results.size() > 0) {
            return results.get(0).longValue();
        }
        return 0;
    }

    public long getVirtualIpCountByTyepeForCluster(Integer clusterId, String type) {
        String sql = "select count(*) from VirtualIp vip where type = :type and vip.cluster.id = :id";
        Query qry = entityManager.createQuery(sql);
        qry.setParameter("id", clusterId);
        qry.setParameter("type", type);
        List<Long> results = qry.getResultList();
        if (results != null && results.size() > 0) {
            return results.get(0).longValue();
        }
        return 0;
    }

    // For Jira https://jira.mosso.com/browse/SITESLB-231
    public List<LoadBalancerCountByAccountIdClusterId> getAccountsInCluster(Integer id) {
        List<LoadBalancerCountByAccountIdClusterId> lbCounts = new ArrayList<LoadBalancerCountByAccountIdClusterId>();
        List<Object> results;
        String query;
        if (id == null) { // Assume we want to grab all Clusters
            query = "select l.accountId ,c.id,count(*) from LoadBalancer l "
                    + "join l.host h join h.cluster c WHERE l.status != 'DELETED' group by l.accountId, c.id";
            results = entityManager.createQuery(query).getResultList();
        } else {
            query = "select l.accountId ,c.id,count(*) from LoadBalancer l "
                    + "join l.host h join h.cluster c  where c.id = :id AND l.status != 'DELETED' group by l.accountId, c.id";
            results = entityManager.createQuery(query).setParameter("id", id).getResultList();
        }
        for (Object r : results) {
            Object[] tuple = (Object[]) r;
            LoadBalancerCountByAccountIdClusterId lbCount = new LoadBalancerCountByAccountIdClusterId();
            lbCount.setAccountId((Integer) tuple[0]);
            lbCount.setClusterId((Integer) tuple[1]);
            lbCount.setLoadBalancerCount((Long) tuple[2]);
            lbCounts.add(lbCount);
        }
        return lbCounts;
    }

    public Integer getNumberOfUniqueAccountsForCluster(Integer id) {
        List<Object> results;
        String query = "select distinct(l.accountId) from LoadBalancer l "
                + "join l.host h  where l.status != :status and h.cluster.id = :id";
        results = entityManager.createQuery(query).setParameter("status", LoadBalancerStatus.DELETED).setParameter("id", id).getResultList();
        return results.size();
    }

    public Integer getNumberOfLoadBalancersForCluster(Integer id) {
        List<Long> results;
        String query = "select count(*) from LoadBalancer l "
                + "where l.status != :status and l.host.cluster.id = :id";
        results = entityManager.createQuery(query).setParameter("id", id).setParameter("status", LoadBalancerStatus.DELETED).getResultList();
        return new Integer(results.get(0).toString());
    }

        public Integer getNumberOfActiveLoadBalancersForCluster(Integer id) {
        List<Long> results;
        String query = "select count(*) from LoadBalancer l "
                + "where l.host.cluster.id = :id and l.status='ACTIVE'";
        results = entityManager.createQuery(query).setParameter("id", id).getResultList();
        return new Integer(results.get(0).toString());
    }

    public Integer getClusterUtlization(Integer id) {
        Integer utilization = new Integer(0);

        List<Long> results;
        String queryMaxConnection = "select sum(maxConcurrentConnections) from Host h where h.cluster.id = :id";
        results = entityManager.createQuery(queryMaxConnection).setParameter("id", id).getResultList();

        if (results.size() > 0 && results.get(0) != null) {
            long maxAllowed = results.get(0).longValue();
            String queryAllowedByRateProfile = "select sum(r.connectionThreshold) "
                    + " from LoadBalancer l join l.loadBalancerRateProfile r join l.host h where h.cluster.id = :id";
            List<Long> resultsLb = entityManager.createQuery(queryAllowedByRateProfile).setParameter("id", id).getResultList();

            if (resultsLb.size() > 0 && results.get(0) != null) {
                long allowedByRateProfile = resultsLb.get(0).longValue();

                double s = ((double) (allowedByRateProfile) / maxAllowed) * 100;

                //double result = ((double)a)/b;


                utilization = new Integer((int) (s));
            }
        }
        return utilization;
    }

    // According to Jira:https://jira.mosso.com/browse/SITESLB-219
    public List<Customer> getCustomerList(Object key) {
        List<Customer> customerList = new ArrayList<Customer>();
        LoadBalancer loadBalancer = new LoadBalancer();
        Customer customer = new Customer();
        List results;
        String queryHead = "select l.accountId, l.id, l.name, n.id, n.ipAddress, l.status "
                + "from LoadBalancer l left join l.nodes n join l.host.cluster c ";
        String queryTail = " order by l.accountId, l.id, n.id ";
        String fullQuery;
        int currAid = -1;
        int currLid = -1;

        if (key instanceof Integer) {
            fullQuery = String.format("%s where c.id = :cid %s", queryHead, queryTail);
            results = entityManager.createQuery(fullQuery).setParameter("cid", (Integer) key).getResultList();
        } else if (key instanceof String) {
            fullQuery = String.format("%s where c.name = :cname %s", queryHead, queryTail);
            results = entityManager.createQuery(fullQuery).setParameter("cname", (String) key).getResultList();
        } else {
            throw new RuntimeException("getCustomerList can only handle Integers and Strings please be reasonable");
        }
        for (Object r : results) {
            Object[] row = (Object[]) r;
            Integer aid = (Integer) row[0];
            Integer lid = (Integer) row[1];
            String lname = (String) row[2];
            Integer nid = (Integer) row[3];
            String nip = (String) row[4];
            LoadBalancerStatus status =(LoadBalancerStatus) row[5];

            if (currAid != aid) {
                customer = new Customer();
                customer.setAccountId(aid);
                currAid = aid;
                customerList.add(customer);
            }

            if (currLid != lid) {
                loadBalancer = new LoadBalancer();
                loadBalancer.setAccountId(aid);
                loadBalancer.setName(lname);
                loadBalancer.setStatus(status);
                loadBalancer.setId(lid);
                loadBalancer.setLoadBalancerJoinVipSet(null);
                loadBalancer.setNodes(null);
                customer.getLoadBalancers().add(loadBalancer);

                currLid = lid;
            }

             //SITESLB-918 removed nodes
           /* if (nid != null) {
                Node node = new Node();
                node.setId(nid);
                node.setIpAddress(nip);
                node.setWeight(null);
                if (loadBalancer.getNodes() == null) {
                    loadBalancer.setNodes(new HashSet<Node>());
                }
                loadBalancer.addNode(node);
            }  */

        }
        return customerList;
    }

    public List<VirtualIpAvailabilityReport> getVirtualIpAvailabilityReport(Integer cid) {
        StringBuilder qStr = new StringBuilder();
        List<VirtualIpAvailabilityReport> vipReportList;
        List<Integer> keys;
        List<Cluster> clusters;
        VirtualIpAvailabilityReport vipReport;
        VipMap totalIps;
        VipMap clearIps;
        VipMap holdingIps;
        VipMap allocated24hours;
        VipMap allocated7days;

        Map<Integer, VirtualIpAvailabilityReport> vipReportMap;

        vipReportList = new ArrayList<VirtualIpAvailabilityReport>();
        keys = new ArrayList<Integer>();
        vipReportMap = new HashMap<Integer, VirtualIpAvailabilityReport>();
        qStr.append("from Cluster c ");
        if(cid != null) {
            qStr.append("where c.id = :cid ");
        }
        qStr.append(" order by c.id");
        Query q = entityManager.createQuery(qStr.toString());
        if(cid != null) {
            q = q.setParameter("cid",cid);
        }
        clusters = q.getResultList();
        for (Cluster c : clusters) {
            vipReport = new VirtualIpAvailabilityReport();
            vipReport.setClusterId(c.getId());
            vipReport.setClusterName(c.getName());
            keys.add(c.getId());
            vipReportMap.put(c.getId(), vipReport);
        }

        totalIps = getTotalIps();
        clearIps = getClearIps();
        holdingIps = getHoldingIps();
        allocated24hours = getAllocated24hours();
        allocated7days = getAllocated7days();

        for (Integer clusterId : keys) {
            VirtualIpAvailabilityReport vrm = vipReportMap.get(clusterId);
            vrm.setTotalPublicIpAddresses(totalIps.get(clusterId).getPublicCount());
            vrm.setTotalServiceNetAddresses(totalIps.get(clusterId).getServiceNetCount());
            vrm.setFreeAndClearPublicIpAddresses(clearIps.get(clusterId).getPublicCount());
            vrm.setFreeAndClearServiceNetIpAddresses(clearIps.get(clusterId).getServiceNetCount());
            vrm.setPublicIpAddressesInHolding(holdingIps.get(clusterId).getPublicCount());
            vrm.setServiceNetIpAddressesInHolding(holdingIps.get(clusterId).getServiceNetCount());
            vrm.setPublicIpAddressesAllocatedToday(allocated24hours.get(clusterId).getPublicCount());
            vrm.setServiceNetIpAddressesAllocatedToday(allocated24hours.get(clusterId).getServiceNetCount());
            vrm.setAllocatedPublicIpAddressesInLastSevenDays(allocated7days.get(clusterId).getPublicCount());
            vrm.setAllocatedServiceNetIpAddressesInLastSevenDays(allocated7days.get(clusterId).getServiceNetCount());

            Double public7days = new Double(vrm.getAllocatedPublicIpAddressesInLastSevenDays());
            Double publicClear = new Double(vrm.getFreeAndClearPublicIpAddresses());
            
            Double service7days = new Double(vrm.getAllocatedServiceNetIpAddressesInLastSevenDays());
            Double serviceClear = new Double(vrm.getFreeAndClearServiceNetIpAddresses());
            
            Double daysLeftPublic = (7.0*publicClear)/public7days;
            Double daysLeftService = (7.0*serviceClear)/service7days;
            
            vrm.setRemainingDaysOfPublicIpAddresses(daysLeftPublic);
            vrm.setRemainingDaysOfServiceNetIpAddresses(daysLeftService);
            vipReportList.add(vrm);
        }
        return vipReportList;
    }

    private VipMap getAllocated24hours() {
        VipMap vm;
        List<Object> results;
        Calendar oneday = Calendar.getInstance();
        oneday.add(Calendar.SECOND, 0 - 24 * 60 * 60);
        String qStr = "select v.cluster.id,v.vipType,count(v) from VirtualIp v "
                + "where v.lastAllocation > :oneday "
                + "group by v.vipType, v.cluster.id "
                + "order by v.cluster.id ";
        results = entityManager.createQuery(qStr).setParameter("oneday", oneday).getResultList();
        vm = new VipMap(results);
        return vm;
    }

    private VipMap getAllocated7days() {
        VipMap vm;
        List<Object> results;
        Calendar sevendays = Calendar.getInstance();
        sevendays.add(Calendar.SECOND, 0 - 7 * 24 * 60 * 60);
        String qStr = "select v.cluster.id,v.vipType,count(v) from VirtualIp v "
                + "where v.lastAllocation > :oneday "
                + "group by v.vipType, v.cluster.id "
                + "order by v.cluster.id ";
        results = entityManager.createQuery(qStr).setParameter("oneday", sevendays).getResultList();
        vm = new VipMap(results);
        return vm;
    }

    private VipMap getTotalIps() {
        VipMap vm;
        Integer ccid; // Current Cluster id
        List<Object> results;
        String qStr = "select v.cluster.id,v.vipType,count(v) from VirtualIp v "
                + "group by v.vipType, v.cluster.id "
                + "order by v.cluster.id ";
        results = entityManager.createQuery(qStr).getResultList();
        vm = new VipMap(results);
        return vm;
    }

    private VipMap getHoldingIps() {
        VipMap vm;
        Integer ccid; // Current Cluster id
        List<Object> results;
        Calendar oneday = Calendar.getInstance();
        oneday.add(Calendar.SECOND, 0 - 24 * 60 * 60);
        String qStr = "select v.cluster.id,v.vipType,count(v) from VirtualIp v "
                + "where size(v.loadBalancerJoinVipSet) = 0 and "
                + "    v.lastDeallocation >= :oneday "
                + "group by v.vipType, v.cluster.id "
                + "order by v.cluster.id ";

        results = entityManager.createQuery(qStr).setParameter("oneday", oneday).getResultList();
        vm = new VipMap(results);
        return vm;
    }

    private VipMap getClearIps() {
        VipMap vm;
        List<Object> results;
        Calendar oneday = Calendar.getInstance();
        oneday.add(Calendar.SECOND, 0 - 24 * 60 * 60);
        String qStr = "select v.cluster.id,v.vipType,count(v) from VirtualIp v "
                + "where size(v.loadBalancerJoinVipSet) = 0 and "
                + "   (v.lastDeallocation < :oneday or "
                + "    v.lastDeallocation is null) "
                + "group by v.vipType, v.cluster.id "
                + "order by v.cluster.id ";
        results = entityManager.createQuery(qStr).setParameter("oneday", oneday).getResultList();
        vm = new VipMap(results);
        return vm;
    }

    public Cluster getActiveCluster() throws ClusterStatusException {
        List<Cluster> cls = entityManager.createQuery("SELECT cl FROM Cluster cl where cl.clusterStatus = :status").setParameter("status", ClusterStatus.ACTIVE).getResultList();
        if (cls != null && cls.size() > 0) {
            return cls.get(0);
        } else {
            String errMsg = "Active cluster not found, please contact support...";
            LOG.warn(errMsg);
            throw new ClusterStatusException(errMsg);
        }
    }

    public Cluster getCluster() {
        List<Cluster> cls = entityManager.createQuery("SELECT cl FROM Cluster cl").getResultList();
        if (cls != null && cls.size() > 0) {
            return cls.get(0);
        } else {
            return null;
        }
    }

    public class VipMap {

        private List<Integer> keys = new ArrayList<Integer>();
        private Map<Integer, VipCount> vipCounts = new HashMap<Integer, VipCount>();

        public VipMap() {
        }

        public VipMap(List<Object> objs) {
            for (Object r : objs) {
                Object[] row = (Object[]) r;
                Integer tClusterId = (Integer) row[0];
                VirtualIpType tType = (VirtualIpType) row[1];
                Long tCount = (Long) row[2];
                switch (tType) {
                    case PUBLIC:
                        put(tClusterId, tCount, null);
                        break;
                    case SERVICENET:
                        put(tClusterId, null, tCount);
                        break;
                    default:
                        break;
                }
            }
        }

        public boolean put(Integer cid, Long publicCount, Long serviceNetCount) {
            boolean out;
            VipCount vc;
            if (!vipCounts.containsKey(cid)) {
                keys.add(cid);
                vc = new VipCount();
                out = true;
            } else {
                vc = vipCounts.get(cid);
                out = false;
            }
            if (publicCount != null) {
                vc.setPublicCount(publicCount);
            }
            if (serviceNetCount != null) {
                vc.setServiceNetCount(serviceNetCount);
            }
            vipCounts.put(cid, vc);
            return out;
        }

        public VipCount get(Integer cid) {
            if (!vipCounts.containsKey(cid)) {
                return new VipCount();

            } else {
                return vipCounts.get(cid);
            }
        }

        public List<Integer> getKeys() {
            return this.keys;
        }
    }

    public class VipCount {

        private Long serviceNetCount = new Long(0);
        private Long publicCount = new Long(0);

        public VipCount() {
        }

        public Long getServiceNetCount() {
            return serviceNetCount;
        }

        public void setServiceNetCount(Long serviceNetCount) {
            this.serviceNetCount = serviceNetCount;
        }

        public Long getPublicCount() {
            return publicCount;
        }

        public void setPublicCount(Long publicCount) {
            this.publicCount = publicCount;
        }
    }
}
