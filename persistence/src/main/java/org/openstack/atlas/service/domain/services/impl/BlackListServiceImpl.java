package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.BlacklistItem;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.BlackListService;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.ip.exception.IpTypeMissMatchException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlackListServiceImpl extends BaseService implements BlackListService {

    final Log LOG = LogFactory.getLog(BlackListServiceImpl.class);

    @Override
    @Transactional
    public List<BlacklistItem> createBlacklist(List<BlacklistItem> list) {
        List<BlacklistItem> newList = blacklistRepository.getBlacklistItemsCidrHashMap(list);
        return new ArrayList<BlacklistItem>();
    }

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

    private class Blacklists {
        List<BlacklistItem> goodList = new ArrayList<BlacklistItem>();
        List<BlacklistItem> badList = new ArrayList<BlacklistItem>();

        public List<BlacklistItem> getBadList() {
            return badList;
        }

        public List<BlacklistItem> getGoodList() {
            return goodList;
        }

        public void setGoodList(List<BlacklistItem> goodList) {
            this.goodList = goodList;
        }

        public void setBadList(List<BlacklistItem> badList) {
            this.badList = badList;
        }
    }
}
