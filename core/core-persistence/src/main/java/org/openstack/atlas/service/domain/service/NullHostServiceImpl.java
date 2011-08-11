package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.Host;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NullHostServiceImpl implements HostService {
    @Override
    public Host getById(Integer id) throws EntityNotFoundException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Host> getAll(Integer... p) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Host> getAllActive() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Host> getAllHosts() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Integer> getLoadBalancerIdsForHost(Integer hostId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<LoadBalancer> getLoadBalancers(Integer hostId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<LoadBalancer> getLoadBalancersWithStatus(Integer hostId, LoadBalancerStatus status) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void create(Host host) throws UnprocessableEntityException, EntityNotFoundException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void save(Host host) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void delete(Host host) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Host update(Host host) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Host getEndPointHost(Integer clusterId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getFailoverHostNames(Integer clusterId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getEndPoint(Integer clusterId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Host getHostsByLoadBalancerId(Integer loadBalancerId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getHostsConnectionsForCluster(Integer clusterId) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Integer getNumberOfUniqueAccountsForHost(Integer id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getActiveLoadBalancerForHost(Integer id) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateLoadBalancerSticky(LoadBalancer lb) throws EntityNotFoundException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteLoadBalancerSticky(LoadBalancer lb) throws EntityNotFoundException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void activateHost(Host host) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void inActivateHost(Host host) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateHost(Host queueHost) throws EntityNotFoundException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteHost(Host queueHost) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Host getDefaultActiveHost() throws EntityNotFoundException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isActiveHost(Host host) throws EntityNotFoundException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
