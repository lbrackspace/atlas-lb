package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.util.Constants;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Repository
@Transactional
public class SslTerminationRepository {

    final Log LOG = LogFactory.getLog(SslTerminationRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;/**/


    public boolean removeSslTermination(Integer lid, Integer aid) {
        SslTermination up = getSslTermination(lid, aid);
        if (up == null) {
            return false;
        } else {
            entityManager.remove(up);
            return true;
        }
    }

    public SslTermination getSslTermination(Integer lid, Integer aid) {
        SslTermination sslTermination = new SslTermination();
        String qStr = "FROM SslTermination u where u.loadbalancer.id = :lid";
        Query q = entityManager.createQuery(qStr).setParameter("lid", lid);
        try {
            if (!q.getResultList().isEmpty()) {
                sslTermination = (SslTermination) q.getResultList().get(0);
            }
        } catch (IndexOutOfBoundsException iex) {
            return new SslTermination();
        }
        return sslTermination;
    }

    public SslTermination setSslTermination(Integer lid, Integer aid, SslTermination sslTermination) throws EntityNotFoundException {
        LoadBalancer lb = getLbById(lid);
        sslTermination.setLoadbalancer(lb);
        entityManager.merge(sslTermination);
        entityManager.flush();
        return sslTermination;
    }

    private LoadBalancer getLbById(Integer id) throws EntityNotFoundException {
        LoadBalancer lb = entityManager.find(LoadBalancer.class, id);
        if (lb == null) {
            String message = Constants.LoadBalancerNotFound;
            LOG.warn(message);
            throw new EntityNotFoundException(message);
        }
        return lb;
    }
}
