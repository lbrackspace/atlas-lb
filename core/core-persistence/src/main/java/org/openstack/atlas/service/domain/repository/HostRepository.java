package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.Host;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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

    public Host getEndPointHost(Integer clusterId) {
        String hqlStr = "from Host h where h.endpointActive = 1 "
                + "and h.hostStatus in ('ACTIVE_TARGET', 'FAILOVER') "
                + "and h.cluster.id = :clusterId "
                + "order by h.hostStatus desc, h.id asc";
        Query q = entityManager.createQuery(hqlStr).setParameter("clusterId", clusterId).setMaxResults(1);
        List<Host> results = q.getResultList();
        if (results.size() < 1) {
            LOG.error(String.format("Error no more endpoints left for ClusterId %d.", clusterId));
            return null;
        }
        return results.get(0);
    }

    public Host update(Host host) {
        LOG.info("Updating Host " + host.getId() + "...");
        host = entityManager.merge(host);
        entityManager.flush();
        return host;
    }
/*    public List<String> getFailoverHostNames (Integer clusterId){
        String hql = "select h.name from Host h where h.hostStatus = 'FAILOVER' and h.cluster.id = :clusterId";
        Query q = entityManager.createQuery(hql).setParameter("clusterId",clusterId);
        List<String> results = q.getResultList();
        return results;
    }*/
/*

    public String getEndPoint(Integer clusterId) {
        Host host = getEndPointHost(clusterId);
        if(host==null) {
            return null;
        }
        return host.getEndpoint();
    }
*/

    public Host getDefaultActiveHost() throws EntityNotFoundException {
        //get a host based on the following algorithm
        //status = ACTIVE_TARGET, fewest concurrent connections and fewest number of assigned loadbalanders.
        /*String sql = "SELECT h from Host h where h.hostStatus = '"
                       + HostStatus.ACTIVE_TARGET + "'"
                       + " AND h.maxConcurrentConnections =  ( select min(i.maxConcurrentConnections) from Host i where i.hostStatus = '"
                       + HostStatus.ACTIVE_TARGET + "')";*/


        String sql = "SELECT h from Host h where h.maxConcurrentConnections =  ( select min(i.maxConcurrentConnections) from Host i)";

        Query qry = entityManager.createQuery(sql);
        List<Host> hosts = qry.getResultList();

        if (hosts == null || hosts.size() <= 0) {
            throw new EntityNotFoundException("ACTIVE_TARGET host not found");
        }

        if (hosts.size() == 1) {
            return (hosts.get(0));
        } else {
            Host hostWithMinimumLoadBalancers = null;
            long mincount = 0;
            long count = 0;
            //fewest number
            for (Host host : hosts) {
                String query = "select count(*) from LoadBalancer lb where lb.host.id = :id";
                List<Long> lbsInHost = entityManager.createQuery(query).setParameter("id", host.getId()).getResultList();
                count = lbsInHost.get(0).longValue();
                if (count == 0) {
                    return host;
                } else {
                    if (mincount == 0) {
                        mincount = count;
                        hostWithMinimumLoadBalancers = host;
                    } else if (mincount <= count) {
                        //do nothing
                    } else {
                        mincount = count;
                        hostWithMinimumLoadBalancers = host;
                    }
                }
            }
            return hostWithMinimumLoadBalancers;
        }
    }

    public List<String> getFailoverHostNames(Integer clusterId) {
        String hql = "select h.hostName from Host h where h.hostStatus = 'FAILOVER' and h.cluster.id = :clusterId";
        Query q = entityManager.createQuery(hql).setParameter("clusterId", clusterId);
        List<String> results = q.getResultList();
        return results;
    }

    public List<Host> getHostsMinimumMaxConnections() {
        String sql = "SELECT h from Host h where h.maxConcurrentConnections =  ( select min(i.maxConcurrentConnections) from Host i)";

        Query qry = entityManager.createQuery(sql);
        List<Host> hosts = qry.getResultList();
        return hosts;
    }

    public long countLoadBalancersInHost(Host host) {
        String query = "select count(*) from LoadBalancer lb where lb.host.id = :id";
        List<Long> lbsInHost = entityManager.createQuery(query).setParameter("id", host.getId()).getResultList();
        long count = lbsInHost.get(0).longValue();
        return count;

    }

    public Host getHostWithMinimumLoadBalancers(List<Host> hosts) {
        Host hostWithMinimumLoadBalancers = null;

        long mincount = 0;
        long count = 0;

        //fewest number
        for (Host host : hosts) {
            String query = "select count(*) from LoadBalancer lb where lb.host.id = :id";
            List<Long> lbsInHost = entityManager.createQuery(query).setParameter("id", host.getId()).getResultList();
            count = lbsInHost.get(0).longValue();
            if (count == 0) {
                return host;
            } else {
                if (mincount == 0) {
                    mincount = count;
                    hostWithMinimumLoadBalancers = host;
                } else if (mincount <= count) {
                    //do nothing
                } else {
                    mincount = count;
                    hostWithMinimumLoadBalancers = host;
                }
            }
        }
        return hostWithMinimumLoadBalancers;
    }

}
