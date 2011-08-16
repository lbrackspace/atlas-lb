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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HostServiceImpl implements HostService {
    private final Log LOG = LogFactory.getLog(HealthMonitorServiceImpl.class);

    @Autowired
    private HostRepository hostRepository;

    @Override
    public Host getDefaultActiveHost() throws PersistenceServiceException {
        List<Host> hosts = hostRepository.getHostsMinimumMaxConnections();
        if (hosts == null || hosts.size() <= 0) {
            throw new EntityNotFoundException("ACTIVE_TARGET host not found");
        }
        if (hosts.size() == 1) {
            return (hosts.get(0));
        } else {
            Host hostWithMinimumLoadBalancers = null;

            long mincount = 0;
            long count = 0;

            //fewest number
            for (Host host : hosts) {
                count = hostRepository.countLoadBalancersInHost(host);
                if (count == 0) {
                    return host;
                } else {
                    if (mincount == 0) {
                        mincount = count;
                        hostWithMinimumLoadBalancers = host;
                    } else if (mincount <= count) {
                        //do nothing
                    } else {
                        mincount = count;
                        hostWithMinimumLoadBalancers = host;
                    }
                }
            }
            return hostRepository.getHostWithMinimumLoadBalancers(hosts);
        }
    }

    @Override
    public Host update(Host host) {
        return hostRepository.update(host);
    }

    @Override
    public List<String> getFailoverHostNames(Integer clusterId) {
        return hostRepository.getFailoverHostNames(clusterId);
    }


    @Override
    @Transactional
    public void updateHost(Host queueHost) throws EntityNotFoundException {


        Host dbHost = hostRepository.getById(queueHost.getId());


        if (queueHost.getName() != null) {

            dbHost.setName(queueHost.getName());
        }
        if (queueHost.getCluster() != null) {

            dbHost.setCluster(queueHost.getCluster());
        }
        if (queueHost.getCoreDeviceId() != null) {

            dbHost.setCoreDeviceId(queueHost.getCoreDeviceId());
        }
        if (queueHost.getEndpoint() != null) {

            dbHost.setEndpoint(queueHost.getEndpoint());
        }
        /*if (queueHost.getHostStatus() != null) {

            dbHost.setHostStatus(queueHost.getHostStatus());
        }*/
        if (queueHost.getManagementIp() != null) {

            dbHost.setManagementIp(queueHost.getManagementIp());
        }
        if (queueHost.getMaxConcurrentConnections() != null) {

            dbHost.setMaxConcurrentConnections(queueHost.getMaxConcurrentConnections());
        }
        if (queueHost.getHostName() != null) {

            dbHost.setHostName(queueHost.getHostName());
        }

        if (queueHost.isEndpointActive() != null) {

            dbHost.setEndpointActive(queueHost.isEndpointActive());
        }

        if (queueHost.getIpv4Public() != null) {

            dbHost.setIpv4Public(queueHost.getIpv4Public());
        }

        if (queueHost.getIpv6Public() != null) {

            dbHost.setIpv6Public(queueHost.getIpv6Public());
        }

        if (queueHost.getIpv4Servicenet() != null) {

            dbHost.setIpv4Servicenet(queueHost.getIpv4Servicenet());
        }
        if (queueHost.getIpv6Servicenet() != null) {

            dbHost.setIpv6Servicenet(queueHost.getIpv6Servicenet());
        }

        hostRepository.update(dbHost);

    }

    @Override
    public Host getEndPointHost(Integer clusterId) {
        return hostRepository.getEndPointHost(clusterId);
    }

}
