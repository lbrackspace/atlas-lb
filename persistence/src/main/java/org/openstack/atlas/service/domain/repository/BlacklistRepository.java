package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.BlacklistItem;
import org.openstack.atlas.service.domain.entities.BlacklistType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
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

    public List<BlacklistItem> saveBlacklist(List<BlacklistItem> blackListItems) {
        List<BlacklistItem> goodList = new ArrayList<BlacklistItem>();
        List<BlacklistItem> badList = new ArrayList<BlacklistItem>();

        List<BlacklistItem> blacklist = getAllBlacklistItemsWithCIDRBlocks(blackListItems);
        
        if (badList.isEmpty())
            for (BlacklistItem bli : goodList)
                persist(bli);

        return badList;
    }

    public List<BlacklistItem> getAllBlacklistItemsWithCIDRBlocks(List<BlacklistItem> list) {
        List<BlacklistItem> goodList = new ArrayList<BlacklistItem>();
        List<BlacklistItem> badList = new ArrayList<BlacklistItem>();
        List<String> cidrBlocks = new ArrayList<String>();
        for (BlacklistItem item : list) {
            cidrBlocks.add(item.getCidrBlock());
        }
        String query = "SELECT b FROM BlacklistItem b WHERE b.cidrBlock in (:cidrBlocks)";
        List<BlacklistItem> dbList = entityManager.createQuery(query).setParameter("cidrBlocks", cidrBlocks).getResultList();
        if (dbList.size() > 0) {
            for (BlacklistItem item : dbList) {
                for (BlacklistItem blitem : list) {

                    // TODO: Finish logic
                    if (item.getCidrBlock().equals(blitem.getCidrBlock())) {
                        if (blitem.getBlacklistType() == null) {
                            if (item.getBlacklistType().equals(BlacklistType.NODE)) {
                                blitem.setBlacklistType(BlacklistType.ACCESSLIST);
                            } else if (item.getBlacklistType().equals(BlacklistType.ACCESSLIST)) {
                                blitem.setBlacklistType(BlacklistType.NODE);
                            } else {
                                BlacklistItem newItem = new BlacklistItem();
                                newItem.setBlacklistType(BlacklistType.NODE);
                                newItem.setCidrBlock(blitem.getCidrBlock());
                                newItem.setIpVersion(blitem.getIpVersion());
                                newItem.setUserName(blitem.getUserName());
                                goodList.add(newItem);
                                blitem.setBlacklistType(BlacklistType.ACCESSLIST);
                            }
                            goodList.add(blitem);
                        } else if (blitem.getBlacklistType().equals(BlacklistType.ACCESSLIST)) {
                            
                        } else {

                        }
                    }
                }
            }
        }
        return dbList;
    }

    public void persist(Object obj) {
        entityManager.persist(obj);
    }
}
