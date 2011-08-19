package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.Host;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;

public interface HostService {
    Host getDefaultActiveHost() throws PersistenceServiceException;
}
