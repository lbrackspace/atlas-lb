package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.AllowedDomain;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
@Transactional
public class AllowedDomainsRepository {
    final Log LOG = LogFactory.getLog(AllowedDomainsRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;


    public void delete(AllowedDomain allowedDomain) {
        allowedDomain = entityManager.merge(allowedDomain);
        entityManager.remove(allowedDomain);
    }

    public List<AllowedDomain> getAllAllowedDomains() {
        Query query = entityManager.createQuery("SELECT ad FROM AllowedDomain ad");
        return query.getResultList();
    }

    public AllowedDomain getAllowedDomain(int id) {
        Query query = entityManager.createQuery("SELECT ad FROM AllowedDomain ad WHERE ad.id = :id").setParameter("id", id);
        return (AllowedDomain)query.getResultList().get(0);
    }

    public void createAllowedDomains(List<AllowedDomain> allowedDomains) {
        for (AllowedDomain item : allowedDomains) {
            persist(item);
        }
    }

    public void persist(Object obj) {
        entityManager.persist(obj);
    }
}
