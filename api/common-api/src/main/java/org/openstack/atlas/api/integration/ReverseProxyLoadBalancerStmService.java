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
import java.util.Set;

public interface ReverseProxyLoadBalancerStmService {

    void createLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException;

    void updateLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException;

    void deleteLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException;

    void setNodes(LoadBalancer lb) throws Exception;

    void removeNode(LoadBalancer lb, Node node) throws Exception;

    void removeNodes(LoadBalancer lb) throws Exception;

    void setNodeWeights(Integer id, Integer accountId, Set<Node> nodes) throws Exception;

    void addVirtualIps(Integer id, Integer accountId, LoadBalancer loadBalancer) throws Exception;

    void deleteAccessList(LoadBalancer lb) throws Exception;

    void deleteVirtualIps(LoadBalancer lb, List<Integer> ids) throws Exception;

    void updateAccessList(LoadBalancer loadBalancer) throws Exception;

    void updateAlgorithm(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException;

    void updatePort(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, Exception;

    void updateTimeout(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, Exception;

    void updateProtocol(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, Exception;

    void updateHalfClosed(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, Exception;

    void changeHostForLoadBalancer(LoadBalancer lb, Host newHost) throws ObjectExistsException, RemoteException, InsufficientRequestException, Exception;

    void updateConnectionLogging(LoadBalancer lb) throws ObjectExistsException, RemoteException, InsufficientRequestException, Exception;

    void updateContentCaching(LoadBalancer lb) throws ObjectExistsException, RemoteException, InsufficientRequestException, Exception;

    void updateConnectionThrottle(LoadBalancer loadbalancer) throws Exception;

    void deleteConnectionThrottle(LoadBalancer loadBalancer) throws Exception;

    void updateSessionPersistence(LoadBalancer lb) throws Exception;

    void removeSessionPersistence(LoadBalancer lb) throws Exception;

    void updateHealthMonitor(LoadBalancer loadBalancer) throws Exception;

    void removeHealthMonitor(LoadBalancer loadBalancer) throws Exception;

    void suspendLoadBalancer(LoadBalancer lb) throws Exception;

    void removeSuspension(LoadBalancer lb) throws Exception;

    void setErrorFile(LoadBalancer loadBalancer, String content) throws Exception,DecryptException, MalformedURLException;

    Hostssubnet getSubnetMappings(Host host) throws Exception;

    void setSubnetMappings(Host host, Hostssubnet hostssubnet) throws Exception;

    void deleteSubnetMappings(Host host, Hostssubnet hostssubnet) throws Exception;

    public LoadBalancerEndpointConfiguration getConfig(Host host) throws DecryptException, MalformedURLException;

    public LoadBalancerEndpointConfiguration getConfigHost(Host host) throws DecryptException, MalformedURLException;

    public boolean isEndPointWorking(Host host) throws Exception;

    public void setRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) throws Exception;

    public void deleteRateLimit(LoadBalancer loadBalancer) throws Exception;

    public void updateRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) throws Exception;

    public void removeAndSetDefaultErrorFile(LoadBalancer loadBalancer) throws EntityNotFoundException, MalformedURLException, DecryptException, RemoteException, InsufficientRequestException;

    public void deleteErrorFile(LoadBalancer loadBalancer) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException;

    public void uploadDefaultErrorFile(Integer clusterId, String content) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException;

    public void setDefaultErrorFile(LoadBalancer loadBalancer) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException;

    public void updateSslTermination(LoadBalancer loadBalancer, ZeusSslTermination sslTermination) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException;

    public void removeSslTermination(LoadBalancer lb) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException;

    public void enableDisableSslTermination(LoadBalancer loadBalancer, boolean isSslTermination) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException;

    public void setNodesPriorities(String poolName, LoadBalancer lb) throws DecryptException, EntityNotFoundException, MalformedURLException, RemoteException;
}