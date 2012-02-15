package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.BlacklistItem;
import org.openstack.atlas.service.domain.entities.IpVersion;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.util.ip.IPv6Cidr;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
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
        return toHashMap(entityManager.createQuery("SELECT b FROM BlacklistItem b").getResultList());
    }

    private Map<String, List<BlacklistItem>> toHashMap(List<BlacklistItem> list) {
        Map<String, List<BlacklistItem>> map = new HashMap<String, List<BlacklistItem>>();
        String cidrBlock;

        for (BlacklistItem item : list) {
            if (item.getIpVersion() == IpVersion.IPV6) {
                try {
                    cidrBlock = new IPv6Cidr().getExpandedIPv6Cidr(item.getCidrBlock());
                } catch (IPStringConversionException e) {
                    LOG.error("Attempt to expand IPv6 string from CidrBlock " + item.getCidrBlock() + ": " + e.getMessage());
                    throw new IllegalArgumentException(e);
                }
            } else {
                cidrBlock = item.getCidrBlock();
            }
            
            if (!map.containsKey(cidrBlock)) {
                map.put(cidrBlock, new ArrayList<BlacklistItem>());
            }
            map.get(cidrBlock).add(item);
        }
        return map;
    }

    public void persist(Object obj) {
        entityManager.persist(obj);
    }
}
