package org.openstack.atlas.service.domain.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.Host;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.service.HostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HostServiceImpl implements HostService {
    private final Log LOG = LogFactory.getLog(HealthMonitorServiceImpl.class);

    @Autowired
    private HostRepository hostRepository;

    @Override
    public Host getDefaultActiveHost() throws PersistenceServiceException {
        List<Host> hosts = hostRepository.getHosts();
        if (hosts == null || hosts.size() <= 0) {
            throw new EntityNotFoundException("ACTIVE_TARGET host not found");
        }
        if (hosts.size() == 1) {
            return (hosts.get(0));
        } else {
            return hostRepository.getHostWithMinimumLoadBalancers(hosts);
        }
    }

}