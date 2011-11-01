package org.openstack.atlas.service.domain.service;


import org.openstack.atlas.service.domain.entity.Node;
import org.openstack.atlas.service.domain.exception.BadRequestException;

import java.util.Set;

public interface BlacklistService {

    void verifyNoBlacklistNodes(Set<Node> nodes) throws BadRequestException;

}
