package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.entities.BlacklistItem;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
@Transactional
public class BlacklistRepository {
    final Log LOG = LogFactory.getLog(BlacklistRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

     public BlacklistRepository() {
    }

    public BlacklistRepository(EntityManager em) {
        this.entityManager = em;
    }


    public BlacklistItem getById(Integer id) throws EntityNotFoundException {
        BlacklistItem bl = entityManager.find(BlacklistItem.class, id);
        if (bl == null) {
            String errMsg = String.format("Cannot access blacklistitem {id=%d}", id);
            LOG.warn(errMsg);
            throw new EntityNotFoundException(errMsg);
        }
        return bl;
    }

    public void delete(BlacklistItem blacklistItem) {
        blacklistItem = entityManager.merge(blacklistItem);
        entityManager.remove(blacklistItem);

    }

    public List<BlacklistItem> getAllBlacklistItems() {
        Query query = entityManager.createQuery("SELECT b FROM BlacklistItem b");
        return query.getResultList();
    }

    public void saveBlacklist(List<BlacklistItem> blackListItems) {
        for (BlacklistItem bli : blackListItems) {
            persist(bli);
        }
    }

    public void persist(Object obj) {
        entityManager.persist(obj);
    }
}
