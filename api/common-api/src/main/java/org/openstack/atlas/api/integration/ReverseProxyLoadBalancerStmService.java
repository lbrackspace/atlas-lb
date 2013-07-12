package org.openstack.atlas.api.integration;

import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.ObjectExistsException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.RateLimit;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.util.crypto.exception.DecryptException;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.List;

public interface ReverseProxyLoadBalancerStmService {
    public LoadBalancerEndpointConfiguration getConfig(Host host) throws DecryptException, MalformedURLException;

    public LoadBalancerEndpointConfiguration getConfigHost(Host host) throws DecryptException, MalformedURLException;

    void createLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException;

    void updateLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException;

    void deleteLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException;

    void changeHostForLoadBalancer(LoadBalancer lb, Host newHost) throws ObjectExistsException, RemoteException, InsufficientRequestException, Exception;

    void suspendLoadBalancer(LoadBalancer lb) throws Exception;

    void removeSuspension(LoadBalancer lb) throws Exception;

    void updateSslTermination(LoadBalancer loadBalancer, ZeusSslTermination sslTermination) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException;

    void removeSslTermination(LoadBalancer lb) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException;

    void setNodes(LoadBalancer lb) throws Exception;

    void removeNode(LoadBalancer lb, Node node) throws Exception;

    void removeNodes(LoadBalancer lb, List<Node> doomedNodes) throws Exception;

    void addVirtualIps(Integer id, Integer accountId, LoadBalancer loadBalancer) throws Exception;

    void deleteAccessList(LoadBalancer lb) throws Exception;

    void deleteVirtualIps(LoadBalancer lb, List<Integer> ids) throws Exception;

    void updateAccessList(LoadBalancer loadBalancer) throws Exception;

    void updateConnectionThrottle(LoadBalancer loadbalancer) throws Exception;

    void deleteConnectionThrottle(LoadBalancer loadBalancer) throws Exception;

    void updateHealthMonitor(LoadBalancer loadBalancer) throws Exception;

    void removeHealthMonitor(LoadBalancer loadBalancer) throws Exception;

    void uploadDefaultErrorFile(Integer clusterId, String content) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException;

    public void setRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) throws Exception;

    public void deleteRateLimit(LoadBalancer loadBalancer) throws Exception;

    public void updateRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) throws Exception;

    boolean isEndPointWorking(Host host) throws Exception;

    Hostssubnet getSubnetMappings(Host host) throws Exception;

    void setSubnetMappings(Host host, Hostssubnet hostssubnet) throws Exception;

    void deleteSubnetMappings(Host host, Hostssubnet hostssubnet) throws Exception;


}