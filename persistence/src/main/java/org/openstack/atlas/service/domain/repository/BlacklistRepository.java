package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.BlacklistItem;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class BlacklistRepository {
    final Log LOG = LogFactory.getLog(BlacklistRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;


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
        for (BlacklistItem item : blackListItems) {
            persist(item);
        }
    }

    public Map<String, List<BlacklistItem>> getBlacklistItemsCidrHashMap(List<BlacklistItem> list) {
        List<String> cidrBlocks = new ArrayList<String>();
        for (BlacklistItem item : list) {
            cidrBlocks.add(item.getCidrBlock());
        }
        String query = "SELECT b FROM BlacklistItem b WHERE b.cidrBlock in (:cidrBlocks)";

        return toHashMap(entityManager.createQuery(query).setParameter("cidrBlocks", cidrBlocks).getResultList());
    }

    private Map<String, List<BlacklistItem>> toHashMap(List<BlacklistItem> list) {
        Map<String, List<BlacklistItem>> map = new HashMap<String, List<BlacklistItem>>();

        for (BlacklistItem item : list) {
            if (!map.containsKey(item.getCidrBlock())) {
                map.put(item.getCidrBlock(), new ArrayList<BlacklistItem>());
            }
            map.get(item.getCidrBlock()).add(item);
        }
        return map;
    }

    public void persist(Object obj) {
        entityManager.persist(obj);
    }
}
