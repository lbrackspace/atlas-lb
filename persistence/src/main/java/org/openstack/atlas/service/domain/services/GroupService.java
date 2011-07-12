package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.AccountGroup;
import org.openstack.atlas.service.domain.entities.GroupRateLimit;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

public interface GroupService {

    public void insertAccountGroup(AccountGroup lb) throws EntityNotFoundException, BadRequestException;

    public void updateGroup(GroupRateLimit limitGroup) throws EntityNotFoundException;

    public void deleteGroup(GroupRateLimit limitGroup) throws EntityNotFoundException, BadRequestException;

    public void createGroup(GroupRateLimit lbs) throws EntityNotFoundException, BadRequestException;

    public void updateGroupDefaults(GroupRateLimit limitGroup);
}
