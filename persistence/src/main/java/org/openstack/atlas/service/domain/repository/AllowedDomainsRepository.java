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
import java.util.Set;
import java.util.HashSet;

@Repository
@Transactional
public class AllowedDomainsRepository {

    final Log LOG = LogFactory.getLog(AllowedDomainsRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public Set<String> getAllowedDomains() {
        Set<String> domains = new HashSet<String>();
        Query q = entityManager.createQuery("SELECT ad FROM AllowedDomain ad");
        List<AllowedDomain> res = q.getResultList();
        for (AllowedDomain ad : res) {
            domains.add(ad.getName());
        }
        return domains;
    }

    public boolean add(String name) {
        return dbSave(name);
    }

    public boolean remove(String name){
        return dbDel(name);
    }

    public boolean hasDomain(String name) {
        List<AllowedDomain> res = entityManager.createQuery("SELECT ad.name from AllowedDomain ad where name=:name").setParameter("name", name).getResultList();
        return (res.size() > 0) ? true : false;
    }

    private boolean dbDel(String name) {
        List<AllowedDomain> adl = entityManager.createQuery("SELECT ad from AllowedDomain ad where name=:name").setParameter("name", name).getResultList();
        if (adl.size() <= 0) {
            return false;
        }
        entityManager.remove(adl.get(0));
        return true;
    }

    private boolean dbSave(String name) {
        if (hasDomain(name)) {
            return false;
        }
        AllowedDomain ad = new AllowedDomain();
        ad.setName(name);
        entityManager.persist(ad);
        return true;
    }
}
