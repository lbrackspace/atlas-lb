package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.ClusterStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.pojos.Customer;
import org.openstack.atlas.service.domain.pojos.LoadBalancerCountByAccountIdHostId;
import org.openstack.atlas.service.domain.services.HostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HostServiceImpl extends BaseService implements HostService {

    @Override
    public Host getById(Integer id) throws EntityNotFoundException {
        return hostRepository.getById(id);
    }

    @Override
    public List<Host> getAll(Integer... p) {
        return hostRepository.getAll(p);
    }

    @Override
    public List<Host> getAllOnline() {
        return hostRepository.getAllOnline();
    }

    @Override
    public List<Backup> getAllBackups() {
        return hostRepository.getAllBackups();
    }

    @Override
    public List<Host> getAllActive() {
        return hostRepository.getAllActive();
    }

    @Override
    public Host getDefaultActiveHostAndActiveCluster() throws ClusterStatusException, EntityNotFoundException {
        return hostRepository.getDefaultActiveHost(clusterRepository.getActiveCluster().getId());
    }

    @Override
    @Transactional
    public void updateLoadBalancerSticky(LoadBalancer lb) throws EntityNotFoundException {
        LoadBalancer dbLb = loadBalancerRepository.getById(lb.getId());
        dbLb.setSticky(true);
        loadBalancerRepository.update(dbLb);
    }

    @Override
    @Transactional
    public void deleteHost(Host queueHost) throws Exception {

        List<LoadBalancer> dbLoadBalancers = hostRepository.getLoadBalancers(queueHost.getId());
        if (dbLoadBalancers != null && dbLoadBalancers.size() > 0) {
            throw new UnprocessableEntityException("Host is un-processable - has loadbalancers associated to it");

        }

        hostRepository.delete(queueHost);

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
        if (queueHost.getHostStatus() != null) {

            dbHost.setHostStatus(queueHost.getHostStatus());
        }
        if (queueHost.getManagementIp() != null) {

            dbHost.setManagementIp(queueHost.getManagementIp());
        }
        if (queueHost.getMaxConcurrentConnections() != null) {

            dbHost.setMaxConcurrentConnections(queueHost.getMaxConcurrentConnections());
        }
        if (queueHost.getTrafficManagerName() != null) {

            dbHost.setTrafficManagerName(queueHost.getTrafficManagerName());
        }

        if (queueHost.isSoapEndpointActive() != null) {

            dbHost.setSoapEndpointActive(queueHost.isSoapEndpointActive());
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
    @Transactional
    public void deleteLoadBalancerSticky(LoadBalancer lb) throws EntityNotFoundException {
        LoadBalancer dbLb = loadBalancerRepository.getById(lb.getId());
        dbLb.setSticky(false);
        loadBalancerRepository.update(dbLb);
    }

    @Override
    public List<Host> getAllHosts() {
        List<Host> hosts = hostRepository.getAllHosts();
        return hosts;
    }

    @Override
    public List<Backup> getBackupsForHost(Integer hostId, Integer... p) throws EntityNotFoundException {
        return hostRepository.getBackupsForHost(hostId, p);
    }

    @Override
    public List<Integer> getLoadBalancerIdsForHost(Integer hostId) {
        return hostRepository.getLoadBalancerIdsForHost(hostId);
    }

    @Override
    public List<LoadBalancer> getLoadBalancers(Integer hostId) {
        return hostRepository.getLoadBalancers(hostId);
    }

    @Override
    public List<LoadBalancer> getLoadBalancersWithStatus(Integer hostId, LoadBalancerStatus status) {
        return hostRepository.getLoadBalancersWithStatus(hostId, status);
    }

    @Override
    public void create(Host host) throws UnprocessableEntityException, EntityNotFoundException {
        LOG.debug("Entering " + getClass());

        Cluster dbCluster = clusterRepository.getById(host.getCluster().getId());
        host.setCluster(dbCluster);

        List<Host> allHosts = hostRepository.getAllHosts();
        if (detectDuplicateHosts(allHosts, host)) {

            LOG.warn("Duplicate Host found! Sending failure response back to client...");
            throw new UnprocessableEntityException("Duplicate Host detected. a host is already configured with the specified management ip address. ");
        }

        host.setHostStatus(HostStatus.BURN_IN);
        save(host);
        LOG.debug("Leaving " + getClass());
    }

    @Override
    public void save(Host host) {
        hostRepository.save(host);
    }

    @Override
    public void delete(Host host) {
        hostRepository.delete(host);
    }

    @Override
    public Host update(Host host) {
        return hostRepository.update(host);
    }

    @Override
    public Host getEndPointHost(Integer clusterId) {
        return hostRepository.getEndPointHost(clusterId);
    }

    @Override
    public List<String> getFailoverHostNames(Integer clusterId) {
        return hostRepository.getFailoverHostNames(clusterId);
    }

    @Override
    public String getEndPoint(Integer clusterId) {
        return hostRepository.getEndPoint(clusterId);
    }

    @Override
    public Host getHostsByLoadBalancerId(Integer loadBalancerId) {
        return hostRepository.getHostsByLoadBalancerId(loadBalancerId);
    }

    @Override
    public Backup getBackupByHostIdAndBackupId(Integer hostId, Integer backupId) throws EntityNotFoundException {
        return hostRepository.getBackupByHostIdAndBackupId(hostId, backupId);
    }

    @Override
    @Transactional
    public Backup createBackup(Host host, Backup backup) throws EntityNotFoundException, ImmutableEntityException {
        Host dbHost = hostRepository.getById(host.getId());

        LOG.debug("Adding the backup to the database...");
        backup = hostRepository.createBackup(dbHost, backup);
        LOG.debug("Backup successfully added to the database.");

        return backup;
    }

    @Override
    @Transactional
    public void deleteBackup(Backup backup) {
        hostRepository.deleteBackup(backup);
    }

    @Override
    public List<LoadBalancerCountByAccountIdHostId> getAccountsInHost(Integer id) {
        return hostRepository.getAccountsInHost(id);
    }

    @Override
    public List<Customer> getCustomerList(Object key) {
        return hostRepository.getCustomerList(key);
    }

    @Override
    public long getHostsConnectionsForCluster(Integer clusterId) {
        return hostRepository.getHostsConnectionsForCluster(clusterId);
    }

    @Override
    public Integer getNumberOfUniqueAccountsForHost(Integer id) {
        return hostRepository.getNumberOfUniqueAccountsForHost(id);
    }

    @Override
    public long getActiveLoadBalancerForHost(Integer id) {
        return hostRepository.getActiveLoadBalancerForHost(id);
    }

    @Override
    @Transactional
    public void activateHost(Host host) throws Exception {
        Host dbHost = null;

        try {
            dbHost = hostRepository.getById(host.getId());
        } catch (EntityNotFoundException enfe) {
            throw new EntityNotFoundException(String.format("Cannot find host with id #%d", host.getId()));
        }

        if (!(dbHost.getHostStatus().equals(HostStatus.BURN_IN) || dbHost.getHostStatus().equals(HostStatus.OFFLINE))) {
            throw new ImmutableEntityException(String.format("Host %d is currently active. Canceling request...", host.getId()));
        }

//       TODO: Make Zeus call here       "Activating Host in ZEUS.. TODO: No Zeus call yet :(");
        hostRepository.update(dbHost);

    }

    @Override
    @Transactional
    public void inActivateHost(Host host) throws Exception {
        Host dbHost = null;

        try {
            dbHost = hostRepository.getById(host.getId());
        } catch (EntityNotFoundException enfe) {
            throw new EntityNotFoundException(String.format("Cannot find host with id #%d", host.getId()));
        }

        if (!(dbHost.getHostStatus().equals(HostStatus.BURN_IN) || dbHost.getHostStatus().equals(HostStatus.OFFLINE))) {
            throw new ImmutableEntityException(String.format("Host %d is currently active. Canceling request...", host.getId()));
        }

//       TODO: Make Zeus call here       "In Activating Host in ZEUS.. TODO: No Zeus call yet :(");
        hostRepository.update(dbHost);

    }

    @Override
    public boolean isActiveHost(Host host) throws EntityNotFoundException {
        if (host.getHostStatus() == null) {
            host = hostRepository.getById(host.getId());
        }
        return !(host.getHostStatus().equals(HostStatus.BURN_IN) || host.getHostStatus().equals(HostStatus.OFFLINE));

    }

    public static boolean detectDuplicateHosts(List<Host> allHosts, Host queueHost) {
        for (Host h : allHosts) {
            if (h.getTrafficManagerName().equals(queueHost.getTrafficManagerName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Cluster getClusterById(Integer id) throws EntityNotFoundException {
        return hostRepository.getClusterById(id);
    }
}
