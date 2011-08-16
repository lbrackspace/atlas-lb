package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.Host;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;

import java.util.List;

public interface HostService {
    Host getDefaultActiveHost() throws PersistenceServiceException;

    /*Host update(Host host) throws EntityNotFoundException;
*/
    void updateHost(Host queueHost) throws EntityNotFoundException;

    /*Host getEndPointHost(Integer clusterId);

    List<String> getFailoverHostNames(Integer clusterId);*/


}
