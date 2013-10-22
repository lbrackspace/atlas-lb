package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.Customer;
import org.openstack.atlas.service.domain.pojos.LoadBalancerCountByAccountIdHostId;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Repository
@Transactional
public class HostRepository {

    final Log LOG = LogFactory.getLog(HostRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public Host getById(Integer id) throws EntityNotFoundException {
        Host host = entityManager.find(Host.class, id);
        if (host == null) {
            String errMsg = String.format("Cannot access host {id=%d}", id);
            LOG.warn(errMsg);
            throw new EntityNotFoundException(errMsg);
        }
        return host;
    }

    public List<Host> getAllHosts() {
        String hqlStr = "from Host h where h.hostStatus in ('ACTIVE_TARGET', 'FAILOVER') ";
        List<Host> hosts;
        hosts = entityManager.createQuery(hqlStr).getResultList();
        return hosts;

    }

    public List<Host> getAllOnline() {
        String hqlStr = "from Host h where h.hostStatus not in ('OFFLINE', 'SOAP_API_ENDPOINT', 'REST_API_ENDPOINT') ";
        List<Host> hosts;
        hosts = entityManager.createQuery(hqlStr).getResultList();
        return hosts;
    }

    public List<Host> getOnlineHostsByLoadBalancerHostCluster(LoadBalancer lb) throws EntityNotFoundException {
        String hqlStr = "from Host h where h.hostStatus not in ('OFFLINE', 'SOAP_API_ENDPOINT', 'REST_API_ENDPOINT') and h.cluster.id = :clusterId";
        List<Host> hosts;
        hosts = entityManager.createQuery(hqlStr).setParameter("clusterId", lb.getHost().getCluster().getId()).getResultList();
        return hosts;
    }

    public List<Host> getAll(Integer... p) {
        List<Host> hosts = new ArrayList<Host>();
        Query query = entityManager.createQuery("SELECT h FROM Host h order by h.id");
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
        hosts = query.getResultList();
        return hosts;
    }

    public List<Backup> getAllBackups() {
        Query query = entityManager.createQuery("SELECT b FROM Backup b");
        List<Backup> allBackups = (List<Backup>) query.getResultList();
        if (allBackups == null) {
            return new ArrayList<Backup>();
        }
        return allBackups;
    }

    public List<Host> getAllActive() {
        List<Host> allHosts = getAll();
        List<Host> activeHosts = new ArrayList<Host>();

        for (Host host : allHosts) {
            if (host.getHostStatus().equals(HostStatus.ACTIVE) || host.getHostStatus().equals(HostStatus.ACTIVE_TARGET)) {
                activeHosts.add(host);
            }
        }

        return activeHosts;
    }

    public List<Backup> getBackupsForHost(Integer hostId, Integer... p) throws EntityNotFoundException {
        getById(hostId); // Need lazy loading!!! <-- Host is already Lazy loading your already good. :)
        Query query = entityManager.createQuery("SELECT b FROM Backup b WHERE b.host.id = :hostId").setParameter("hostId", hostId);

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

        return query.getResultList();
    }

    public List<Integer> getLoadBalancerIdsForHost(Integer hostId) {
        List<Integer> ids = new ArrayList<Integer>();
        String sql = "SELECT lb.id from LoadBalancer lb where lb.host.id = :id)";
        Query qry = entityManager.createQuery(sql);
        qry.setParameter("id", hostId);
        List values = qry.getResultList();
        for (Object i : values) {
            ids.add((Integer) (i));
        }
        return ids;
    }

    public List<LoadBalancer> getLoadBalancers(Integer hostId) {
        return entityManager.createQuery("SELECT lb FROM LoadBalancer lb where lb.host.id = :hostId").setParameter("hostId", hostId).getResultList();
    }

    /* Used by usage poller */
    public List<LoadBalancer> getLoadBalancersWithStatus(Integer hostId, LoadBalancerStatus status) {
        List<Object> loadBalancerTuples;
        List<LoadBalancer> loadBalancers = new ArrayList<LoadBalancer>();

        loadBalancerTuples = entityManager.createNativeQuery("SELECT lb.id, lb.account_id FROM loadbalancer lb where lb.host_id = :hostId and lb.status = :status").setParameter("hostId", hostId).setParameter("status", status.name()).getResultList();

        for (Object loadBalancerTuple : loadBalancerTuples) {
            Object[] row = (Object[]) loadBalancerTuple;
            LoadBalancer lb = new LoadBalancer();
            lb.setId((Integer) row[0]);
            lb.setAccountId((Integer) row[1]);
            loadBalancers.add(lb);
        }

        return loadBalancers;
    }

    /* Used by usage poller */
    public List<LoadBalancer> getSslLoadBalancersWithStatus(Integer hostId, LoadBalancerStatus status) {
        List<Object> loadBalancerTuples;
        List<LoadBalancer> loadBalancers = new ArrayList<LoadBalancer>();

        loadBalancerTuples = entityManager.createNativeQuery("SELECT lb.id, lb.account_id FROM loadbalancer lb, lb_ssl s where lb.host_id = :hostId and lb.id = s.loadbalancer_id and lb.status = :status").setParameter("hostId", hostId).setParameter("status", status.name()).getResultList();

        for (Object loadBalancerTuple : loadBalancerTuples) {
            Object[] row = (Object[]) loadBalancerTuple;
            LoadBalancer lb = new LoadBalancer();
            lb.setId((Integer) row[0]);
            lb.setAccountId((Integer) row[1]);
            loadBalancers.add(lb);
        }

        return loadBalancers;
    }

    public void save(Host host) {
        entityManager.persist(host);
    }

    public void delete(Host host) {
        host = entityManager.merge(host);
        entityManager.remove(host);
    }

    public Host update(Host host) {
        LOG.info("Updating Host " + host.getId() + "...");
        host = entityManager.merge(host);
        entityManager.flush();
        return host;
    }

    public Host getEndPointHost(Integer clusterId) {
        String hqlStr = "from Host h where h.soapEndpointActive = 1 "
                + "and h.hostStatus in ('ACTIVE_TARGET', 'FAILOVER','SOAP_API_ENDPOINT') "
                + "and h.cluster.id = :clusterId "
                + "order by h.hostStatus desc, h.id asc";
        Query q = entityManager.createQuery(hqlStr).setParameter("clusterId", clusterId).setMaxResults(1);
        List<Host> results = q.getResultList();
        if (results.size() < 1) {
            LOG.error(String.format("Error no more SOAP endpoints left for ClusterId %d.", clusterId));
            return null;
        }
        return results.get(0);
    }

    public List<String> getFailoverHostNames(Integer clusterId) {
        String hql = "select h.trafficManagerName from Host h where h.hostStatus = 'FAILOVER' and h.cluster.id = :clusterId";
        Query q = entityManager.createQuery(hql).setParameter("clusterId", clusterId);
        List<String> results = q.getResultList();
        return results;
    }

    public String getEndPoint(Integer clusterId) {
        Host host = getEndPointHost(clusterId);
        if (host == null) {
            return null;
        }
        return host.getEndpoint();
    }

    public Host getHostsByLoadBalancerId(Integer loadBalancerId) {
        LoadBalancer lb = entityManager.find(LoadBalancer.class, loadBalancerId);
        if (lb != null) {
            return lb.getHost();
        } else {
            return null;
        }
    }

    public Backup getBackupByHostIdAndBackupId(Integer hostId, Integer backupId) throws EntityNotFoundException {
        Query query = entityManager.createQuery("SELECT b FROM Backup b WHERE b.host.id = :hostId and b.id = :backupId").setParameter("hostId", hostId).setParameter("backupId", backupId);
        List<Backup> backups = query.getResultList();
        if (backups == null || backups.isEmpty()) {
            throw new EntityNotFoundException("Backup could not be found");
        }
        return backups.get(0);
    }

    public Cluster getClusterById(Integer clusterId) {
        Cluster cluster = entityManager.find(Cluster.class, clusterId);
        return cluster;
    }

    public Backup createBackup(Host host, Backup backup) {
        backup.setHost(host);
        backup.setBackupTime(Calendar.getInstance());
        backup = entityManager.merge(backup);
        return backup;
    }

    public void deleteBackup(Backup backup) {
        backup = entityManager.find(Backup.class, backup.getId());
        entityManager.remove(backup);
    }

    // For Jira https://jira.mosso.com/browse/SITESLB-232
    public List<LoadBalancerCountByAccountIdHostId> getAccountsInHost(Integer id) {
        List<LoadBalancerCountByAccountIdHostId> lbCounts = new ArrayList<LoadBalancerCountByAccountIdHostId>();
        List<Object> results;
        String query;
        if (id == null) { // Assume we want to grab all Clusters
            query = "select l.accountId ,h.id,count(*) from LoadBalancer l "
                    + "join l.host h where l.status != 'DELETED' group by l.accountId, h.id";
            results = entityManager.createQuery(query).getResultList();
        } else {
            query = "select l.accountId ,h.id,count(*) from LoadBalancer l "
                    + "join l.host h where h.id = :id and l.status != 'DELETED' group by l.accountId, h.id";
            results = entityManager.createQuery(query).setParameter("id", id).getResultList();
        }
        for (Object r : results) {
            Object[] tuple = (Object[]) r;
            LoadBalancerCountByAccountIdHostId lbCount = new LoadBalancerCountByAccountIdHostId();
            lbCount.setAccountId((Integer) tuple[0]);
            lbCount.setHostId((Integer) tuple[1]);
            lbCount.setLoadBalancerCount((Long) tuple[2]);
            lbCounts.add(lbCount);
        }
        return lbCounts;
    }

    // According to Jira:https://jira.mosso.com/browse/SITESLB-235
    //SITESLB-918 removed nodes modify query once removal of nodes is finalized
    public List<Customer> getCustomerList(Object key) {

        List<Customer> customerList = new ArrayList<Customer>();
        LoadBalancer loadBalancer = new LoadBalancer();
        Customer customer = new Customer();
        List results;
        String queryHead = "select l.accountId, l.id, l.name, n.id, n.ipAddress, l.status "
                + "from LoadBalancer l left join l.nodes n join l.host h ";
        String queryTail = " order by l.accountId, l.id";
        String fullQuery;
        int currAid = -1;
        int currLid = -1;

        if (key instanceof Integer) {
            fullQuery = String.format("%s where h.id = :hid %s", queryHead, queryTail);
            results = entityManager.createQuery(fullQuery).setParameter("hid", (Integer) key).getResultList();
        } else if (key instanceof String) {
            fullQuery = String.format("%s where h.name = :hname %s", queryHead, queryTail);
            results = entityManager.createQuery(fullQuery).setParameter("hname", (String) key).getResultList();
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
            LoadBalancerStatus status = (LoadBalancerStatus) row[5];

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
            /*
            if (nid != null) {
            Node node = new Node();
            node.setId(nid);
            node.setIpAddress(nip);
            node.setWeight(null);
            if (loadBalancer.getNodes() == null) {
            loadBalancer.setNodes(new HashSet<Node>());
            }
            loadBalancer.addNode(node);
            } */

        }
        return customerList;
    }

    public long getHostsConnectionsForCluster(Integer clusterId) {
        long utilization = 0;
        String queryMaxConnection = "select sum(maxConcurrentConnections) from Host h where h.cluster.id = :id";
        List<Long> results = entityManager.createQuery(queryMaxConnection).setParameter("id", clusterId).getResultList();

        if (results.size() > 0 && results.get(0) != null) {
            utilization = results.get(0).longValue();

        }
        return utilization;
    }

    public Integer getNumberOfUniqueAccountsForHost(Integer id) {
        List<Object> results;
        String query = "SELECT distinct(l.accountId) FROM LoadBalancer l WHERE l.host.id = :id AND l.status != 'DELETED'";
        results = entityManager.createQuery(query).setParameter("id", id).getResultList();
        return results.size();
    }

    public long getActiveLoadBalancerForHost(Integer id) {
        long lbs = 0;
        String query = "SELECT COUNT(*) FROM LoadBalancer l WHERE l.host.id = :id AND l.status != 'DELETED'";
        List<Long> results = entityManager.createQuery(query).setParameter("id", id).getResultList();
        if (results.size() > 0 && results.get(0) != null) {
            lbs = results.get(0).longValue();
        }
        return lbs;
    }

    public Host getDefaultActiveHost(Integer clusterId) throws EntityNotFoundException {
        //get a host based on the following algorithm
        //status = ACTIVE_TARGET, fewest concurrent connections and fewest number of assigned loadbalanders.
        String sql = "SELECT h from Host h where h.cluster.id = :clusterId AND h.hostStatus= :hostStatus "
                + "AND h.maxConcurrentConnections = (select min(i.maxConcurrentConnections) "
                + "from Host i where i.cluster.id = :clusterId AND i.hostStatus = :hostStatus)";

//        String sql = "SELECT h from Host h where h.cluster.id = :clusterId"
//                + " AND h.hostStatus = '"
//                + HostStatus.ACTIVE_TARGET + "'"
//                + " AND h.maxConcurrentConnections =  ( select min(i.maxConcurrentConnections) from Host i where i.hostStatus = '"
//                + HostStatus.ACTIVE_TARGET + "')";

        Query qry = entityManager.createQuery(sql).setParameter("hostStatus", HostStatus.ACTIVE_TARGET).setParameter("clusterId", clusterId);
        List<Host> hosts = qry.getResultList();

        if (hosts != null && hosts.size() > 0) {
            if (hosts.size() == 1) {
                return (hosts.get(0));
            } else {
                Host minhost = null;
                long mincount = 0;
                long count = 0;
                //fewest number
                for (Host h : hosts) {
                    String query = "select count(*) from LoadBalancer lb where lb.host.id = :id";
                    List<Long> lst = entityManager.createQuery(query).setParameter("id", h.getId()).getResultList();
                    count = lst.get(0).longValue();
                    if (count == 0) {
                        return h;
                    } else {
                        if (mincount == 0) {
                            mincount = count;
                            minhost = h;
                        } else if (mincount <= count) {
                            //do nothing
                        } else {
                            mincount = count;
                            minhost = h;
                        }
                    }
                }
                return minhost;
            }
        }
        throw new EntityNotFoundException("ACTIVE_TARGET host not found");
    }
}
