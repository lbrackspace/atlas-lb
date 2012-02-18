package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Meta;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@org.springframework.stereotype.Repository
@Transactional
public class MetadataRepository {
    final Log LOG = LogFactory.getLog(MetadataRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public Set<Meta> addMetas(LoadBalancer loadBalancer, Collection<Meta> metas) {
        Set<Meta> newMetas = new HashSet<Meta>();

        for (Meta meta : metas) {
            meta.setLoadbalancer(loadBalancer);
            newMetas.add(entityManager.merge(meta));
        }

        loadBalancer.setUpdated(Calendar.getInstance());
        loadBalancer = entityManager.merge(loadBalancer);
        entityManager.flush();
        return newMetas;
    }
}
