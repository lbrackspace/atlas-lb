package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.AccountGroup;
import org.openstack.atlas.service.domain.entities.GroupRateLimit;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.GroupService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupServiceImpl extends BaseService implements GroupService {
    private final Log LOG = LogFactory.getLog(GroupServiceImpl.class);

    @Transactional
    public void insertAccountGroup(AccountGroup lb) throws EntityNotFoundException, BadRequestException {
        LOG.debug("Entering " + getClass());
        GroupRateLimit dbLb = null;

        try {
            dbLb = groupRepository.getByGroupId(lb.getGroupRateLimit().getId());
        } catch (EntityNotFoundException enfe) {
            throw new EntityNotFoundException(String.format("Cannot find group with id #%d", lb.getGroupRateLimit().getId()));

        }

        List<GroupRateLimit> lists = null;
        try {
            //check to see if account already has the group associated with id
            lists = groupRepository.getAssignedGroupsForAccount(lb.getAccountId());
        } catch (Exception e) {
            //Do nothing
        }

        if (lists != null && lists.size() > 0) {
            for (GroupRateLimit lt : lists) {
                if (lt.getId().intValue() == lb.getGroupRateLimit().getId().intValue()) ;
                {
                    throw new BadRequestException(String.format("Account already is assigned to group #%d", lb.getGroupRateLimit().getId()));

                }
            }
        }
        LOG.debug("Inserting acount group relationship in  inDB...");
        groupRepository.save(lb);
    }

    @Transactional
    public void updateGroup(GroupRateLimit limitGroup) throws EntityNotFoundException {
        LOG.debug("Updating a rate limit group in the database...");
        GroupRateLimit db;

        try {
            db = groupRepository.getByGroupId(limitGroup.getId());
        } catch (EntityNotFoundException e) {
            LOG.warn("EntityNotFoundException thrown. Sending error response to client...");
            throw new EntityNotFoundException(String.format("Cannot find rate limit group requested."));
        }

        limitGroup.setName(db.getName());
        limitGroup.setDescription(db.getDescription());

        updateGroupDefaults(limitGroup);
        groupRepository.update(limitGroup);
        LOG.debug(String.format("Successfully updated rate limit group '%s' in the database.", limitGroup.getName()));
    }

    @Transactional
    public void deleteGroup(GroupRateLimit limitGroup) throws EntityNotFoundException, BadRequestException {
        LOG.debug("Deleting rate limit group in the database...");
        GroupRateLimit db;

        try {
            db = groupRepository.getByGroupId(limitGroup.getId());
        } catch (EntityNotFoundException e) {
            LOG.warn("EntityNotFoundException thrown. Sending error response to client...");
            throw new EntityNotFoundException(String.format("Cannot find rate limit group requested."));
        }

        List<GroupRateLimit> limits;
        try {
            limits = groupRepository.groupHasAssociatedAccounts(limitGroup.getId());
        } catch (EntityNotFoundException e) {
            LOG.warn("EntityNotFoundException thrown. Sending error response to client...");
            throw new EntityNotFoundException(String.format("Cannot find rate limit group requested."));
        }

        if (limits != null && limits.size() > 0) {
            throw new BadRequestException(String.format("Groups has accounts associated with it. This group cannot be deleted."));
        }
        groupRepository.delete(db);
        LOG.debug(String.format("Successfully deleted rate limit group '%s' in the database.", db.getName()));
    }

    @Transactional
    public void createGroup(GroupRateLimit lbs) throws EntityNotFoundException, BadRequestException {
        GroupRateLimit dbGroup;
        LOG.debug("Entering " + getClass());

        if (lbs.getName() == null) {
           throw new BadRequestException( String.format("You must specify a name for the group."));
        }

        try{
            dbGroup =  groupRepository.getByGroupName(lbs.getName());
        }
        catch (EntityNotFoundException e) {
            throw new EntityNotFoundException(String.format("cannot find group requested %s", lbs.getName()));
        }

        if (dbGroup != null)  {
             throw new BadRequestException( String.format("Group name already exists"));
        }
        groupRepository.save(lbs);
        LOG.debug("Leaving " + getClass());
    }

    @Override
    public void updateGroupDefaults(GroupRateLimit limitGroup) {
        List<GroupRateLimit> groups = groupRepository.getAll();
        if (limitGroup.getDefault()) {
            for (GroupRateLimit group : groups) {
                if (!group.getId().equals(limitGroup.getId())) {
                    group.setDefault(false);
                    groupRepository.update(group);
                }
            }
        }
    }
}




