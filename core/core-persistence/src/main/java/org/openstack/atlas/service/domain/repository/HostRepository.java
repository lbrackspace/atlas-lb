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

//    public Host getById(Integer id) throws EntityNotFoundException {
//        Host host = entityManager.find(Host.class, id);
//        if (host == null) {
//            String errMsg = String.format("Cannot access host {id=%d}", id);
//            LOG.warn(errMsg);
//            throw new EntityNotFoundException(errMsg);
//        }
//        return host;
//    }

    public Host getEndPointHost(Integer clusterId) {
        String hqlStr = "from Host h where h.endpointActive = 1 "
                + "and h.hostStatus in ('ACTIVE_TARGET', 'FAILOVER') "
                + "and h.cluster.id = :clusterId "
                + "order by h.hostStatus desc, h.id asc";
        Query query = entityManager.createQuery(hqlStr).setParameter("clusterId", clusterId).setMaxResults(1);
        List<Host> results = query.getResultList();
        if (results.size() < 1) {
            LOG.error(String.format("Error no more endpoints left for ClusterId %d.", clusterId));
            return null;
        }
        return results.get(0);
    }

/*    public String getEndPoint(Integer clusterId) {
        Host host = getEndPointHost(clusterId);
        if(host==null) {
            return null;
        }
        return host.getEndpoint();
    }*/

    public Host update(Host host) {
        LOG.info("Updating Host " + host.getId() + "...");
        host = entityManager.merge(host);
        entityManager.flush();
        return host;
    }

    public List<String> getFailoverHostNames(Integer clusterId) {
        String hql = "select h.hostName from Host h where h.hostStatus = 'FAILOVER' and h.cluster.id = :clusterId";

        Query query = entityManager.createQuery(hql).setParameter("clusterId", clusterId);
        List<String> results = query.getResultList();
        return results;
    }

    public List<Host> getHostsMinimumMaxConnections() {
        String sql = "SELECT h from Host h where h.maxConcurrentConnections =  ( select min(i.maxConcurrentConnections) from Host i)";

        Query query = entityManager.createQuery(sql);
        List<Host> hosts = query.getResultList();
        return hosts;
    }

    public long countLoadBalancersInHost(Host host) {
        String query = "select count(*) from LoadBalancer lb where lb.host.id = :id";

        List<Long> lbsInHost = entityManager.createQuery(query).setParameter("id", host.getId()).getResultList();
        long count = lbsInHost.get(0).longValue();
        return count;

    }

    public Host getHostWithMinimumLoadBalancers(List<Host> hosts) {

        long mincount = 0;
        Host hostWithMinimumLoadBalancers = null;
        for (Host host : hosts) {
            long count = countLoadBalancersInHost(host);
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
