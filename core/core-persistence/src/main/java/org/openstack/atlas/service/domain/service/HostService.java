package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.Host;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;

import java.util.List;

public interface HostService {
    Host getById(Integer id) throws EntityNotFoundException;

    List<Host> getAll(Integer... p);

    List<Host> getAllActive();

    List<Host> getAllHosts();

    List<Integer> getLoadBalancerIdsForHost(Integer hostId);

    List<LoadBalancer> getLoadBalancers(Integer hostId);

    List<LoadBalancer> getLoadBalancersWithStatus(Integer hostId, LoadBalancerStatus status);

    void create(Host host) throws UnprocessableEntityException, EntityNotFoundException;

    void save(Host host);

    void delete(Host host);

    Host update(Host host);

    Host getEndPointHost(Integer clusterId);

    List<String> getFailoverHostNames(Integer clusterId);

    String getEndPoint(Integer clusterId);

    Host getHostsByLoadBalancerId(Integer loadBalancerId);

    long getHostsConnectionsForCluster(Integer clusterId);

    Integer getNumberOfUniqueAccountsForHost(Integer id);

    long getActiveLoadBalancerForHost(Integer id);

    void updateLoadBalancerSticky(LoadBalancer lb) throws EntityNotFoundException;

    void deleteLoadBalancerSticky(LoadBalancer lb) throws EntityNotFoundException;

    void activateHost(Host host) throws Exception;

    void inActivateHost(Host host) throws Exception;

    void updateHost(Host queueHost) throws EntityNotFoundException;

    void deleteHost(Host queueHost) throws Exception;

    Host getDefaultActiveHost() throws EntityNotFoundException;

    boolean isActiveHost(Host host) throws EntityNotFoundException;

}
