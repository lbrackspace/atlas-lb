package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.pojos.Customer;
import org.openstack.atlas.service.domain.pojos.LoadBalancerCountByAccountIdHostId;

import java.util.List;

public interface HostService {
    public Cluster getClusterById(Integer id) throws EntityNotFoundException;

    public Host getById(Integer id) throws EntityNotFoundException;

    public List<Host> getAll(Integer... p);

    public List<Host> getAllOnline();

    public List<Backup> getAllBackups();

    public List<Host> getAllActive();

    public List<Backup> getBackupsForHost(Integer hostId, Integer... p) throws EntityNotFoundException;

    public List<Host> getAllHosts();

    public List<Integer> getLoadBalancerIdsForHost(Integer hostId);

    public List<LoadBalancer> getLoadBalancers(Integer hostId);

    public List<LoadBalancer> getLoadBalancersWithStatus(Integer hostId, LoadBalancerStatus status);

    public void create(Host host) throws UnprocessableEntityException, EntityNotFoundException;

    public void save(Host host);

    public void delete(Host host);

    public Host update(Host host);

    public Host getEndPointHost(Integer clusterId);

    public Host getRestEndPointHost(Integer clusterId);

    public List<String> getFailoverHostNames(Integer clusterId);

    public List<Host> getFailoverHosts(Integer clusterId);

    public Host getFirstAvailableSoapEndPointHost() throws EntityNotFoundException;

    public Host getFirstAvailableRestEndPointHost() throws EntityNotFoundException;

    public String getEndPoint(Integer clusterId);

    public Host getHostsByLoadBalancerId(Integer loadBalancerId);

    public Backup getBackupByHostIdAndBackupId(Integer hostId, Integer backupId) throws EntityNotFoundException;

    public Backup createBackup(Host host, Backup backup) throws EntityNotFoundException, ImmutableEntityException;

    public void deleteBackup(Backup backup);

    public List<LoadBalancerCountByAccountIdHostId> getAccountsInHost(Integer id);

    public List<Customer> getCustomerList(Object key);

    public long getHostsConnectionsForCluster(Integer clusterId);

    public Integer getNumberOfUniqueAccountsForHost(Integer id);

    public long getActiveLoadBalancerForHost(Integer id);

    public void updateLoadBalancerSticky(LoadBalancer lb) throws EntityNotFoundException;

    public void deleteLoadBalancerSticky(LoadBalancer lb) throws EntityNotFoundException;

    public void activateHost(Host host) throws Exception;

    public void inActivateHost(Host host) throws Exception;

    public void updateHost(Host queueHost) throws EntityNotFoundException;

    public void deleteHost(Host queueHost) throws Exception;

    public Host getDefaultActiveHostAndActiveCluster(Integer accountId, boolean hasPublicVip) throws EntityNotFoundException, ClusterStatusException, NoAvailableClusterException;

    public Host getDefaultActiveHostAndActiveCluster(Integer accountId, boolean hasPublicVip, Integer hostId) throws ClusterStatusException, EntityNotFoundException, NoAvailableClusterException;

    public boolean isActiveHost(Host host) throws EntityNotFoundException;

}
