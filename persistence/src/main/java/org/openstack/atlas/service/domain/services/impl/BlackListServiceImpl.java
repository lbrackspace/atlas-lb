package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.BlacklistItem;
import org.openstack.atlas.service.domain.entities.BlacklistType;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.BlackListService;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.ip.exception.IpTypeMissMatchException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlackListServiceImpl extends BaseService implements BlackListService {

    final Log LOG = LogFactory.getLog(BlackListServiceImpl.class);

    @Override
    @Transactional
    public void deleteBlackList(BlacklistItem msgBlacklist) throws Exception {
        LOG.debug("Entering " + getClass());

        BlacklistItem dbBlacklist = null;

        LOG.debug(String.format("del black list[%d]\n", msgBlacklist.getId()));

        try {
            dbBlacklist = blacklistRepository.getById(msgBlacklist.getId());
        } catch (EntityNotFoundException enfe) {
            throw new EntityNotFoundException(String.format("BlackListItem with id #%d not found ", msgBlacklist.getId()));
        }


        LOG.debug("Deleting the blacklistitem");

        blacklistRepository.delete(dbBlacklist);

    }

    @Override
    @Transactional
    public Node getBlackListedItemNode(Set<Node> nodes) throws IPStringConversionException, IpTypeMissMatchException {
        Node out;
        out = blackListedItemNode(nodes);
        return out;
    }

    @Override
    @Transactional
    public List<BlacklistItem> createBlacklist(List<BlacklistItem> list) {
        Map<String, List<BlacklistItem>> map = blacklistRepository.getBlacklistItemsCidrHashMap(list);
        List<BlacklistItem> goodList = new ArrayList<BlacklistItem>();
        List<BlacklistItem> badList = new ArrayList<BlacklistItem>();
        Boolean hasAccessList, hasNode, hasItem;
        hasAccessList = false;
        hasNode = false;
        hasItem = false;

        for (BlacklistItem item : list) {
            if (map.containsKey(item.getCidrBlock()) && map.get(item.getCidrBlock()).size() >= 2) {
                badList.add(item);
            } else if (map.containsKey(item.getCidrBlock()) && map.get(item.getCidrBlock()).size() == 1) {
                for (BlacklistItem blitem : map.get(item.getCidrBlock())) {
                    if (item.getBlacklistType() == null) {
                        if (blitem.getBlacklistType().equals(BlacklistType.ACCESSLIST)) {
                            hasAccessList = true;
                        } else {
                            hasNode = true;
                        }
                    } else if (item.getBlacklistType().equals(blitem.getBlacklistType())) {
                        hasItem = true;
                    }
                }
                if (hasAccessList) {
                    item.setBlacklistType(BlacklistType.NODE);
                    goodList.add(item);
                } else if (hasNode) {
                    item.setBlacklistType(BlacklistType.ACCESSLIST);
                    goodList.add(item);
                }
            }
            if (!hasItem && !hasAccessList && !hasNode) {
                if (item.getBlacklistType() == null) {
                    goodList.add(setBlacklistItemFields(item, BlacklistType.ACCESSLIST));
                    goodList.add(setBlacklistItemFields(item, BlacklistType.NODE));
                } else {
                    goodList.add(setBlacklistItemFields(item, item.getBlacklistType()));
                }
            } else if ((hasAccessList && hasNode) || hasItem) {
                badList.add(item);
            }
        }

        if (badList.isEmpty()) {
            blacklistRepository.saveBlacklist(goodList);
        }

        return badList;
    }

    private BlacklistItem setBlacklistItemFields(BlacklistItem item, BlacklistType type) {
        BlacklistItem blItem = new BlacklistItem();
        blItem.setBlacklistType(type);
        blItem.setCidrBlock(item.getCidrBlock());
        blItem.setIpVersion(item.getIpVersion());
        blItem.setUserName(item.getUserName());
        return blItem;
    }

//    private class Blacklists {
//        private List<BlacklistItem> goodList = new ArrayList<BlacklistItem>();
//        private List<BlacklistItem> badList = new ArrayList<BlacklistItem>();
//
//        public List<BlacklistItem> getBadList() {
//            return this.badList;
//        }
//
//        public List<BlacklistItem> getGoodList() {
//            return this.goodList;
//        }
//
//        public void setGoodList(List<BlacklistItem> goodList) {
//            this.goodList = goodList;
//        }
//
//        public void setBadList(List<BlacklistItem> badList) {
//            this.badList = badList;
//        }
//    }
}
