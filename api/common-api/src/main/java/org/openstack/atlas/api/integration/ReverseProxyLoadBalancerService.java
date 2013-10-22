package org.openstack.atlas.api.integration;

import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.ObjectExistsException;
import org.openstack.atlas.adapter.exceptions.ZxtmRollBackException;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.pojos.Stats;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.util.crypto.exception.DecryptException;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ReverseProxyLoadBalancerService {

    public void createLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, EntityNotFoundException, DecryptException, MalformedURLException;

    public void deleteLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, EntityNotFoundException, DecryptException, MalformedURLException;

    public void updateAlgorithm(LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, EntityNotFoundException, DecryptException, MalformedURLException;

    public void updatePort(LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, Exception;

    public void updateTimeout(LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, Exception;

    public void updateProtocol(LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, Exception;

    public void updateHalfClosed(LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, Exception;

    public void updateHttpsRedirect(LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, Exception;

    public void changeHostForLoadBalancer(LoadBalancer lb, Host newHost) throws ObjectExistsException, RemoteException, InsufficientRequestException, Exception;

    public void updateConnectionLogging(LoadBalancer lb) throws ObjectExistsException, RemoteException, InsufficientRequestException, Exception;

    public void updateContentCaching(LoadBalancer lb) throws ObjectExistsException, RemoteException, InsufficientRequestException, Exception;

    public void setNodes(LoadBalancer lb) throws Exception;

    public void removeNode(Integer id, Integer accountId, Node node) throws Exception;

    public void removeNodes(Integer lbId, Integer accountId, Collection<Node> nodes) throws Exception;

    public void setNodeWeights(Integer id, Integer accountId, Set<Node> nodes) throws Exception;

    public void updateAccessList(LoadBalancer loadBalancer) throws Exception;

    public void updateConnectionThrottle(LoadBalancer loadbalancer) throws Exception;

    public void deleteConnectionThrottle(LoadBalancer loadBalancer) throws Exception;

    public void updateSessionPersistence(Integer id, Integer accountId, SessionPersistence persistenceMode) throws Exception;

    public void removeSessionPersistence(Integer id, Integer accountId) throws Exception;

    public void updateHealthMonitor(Integer lbId, Integer accountId, HealthMonitor monitor) throws Exception;

    public void removeHealthMonitor(LoadBalancer loadBalancer) throws Exception;

    public void createHostBackup(Host host, String backupName) throws Exception;

    public void restoreHostBackup(Host host, String backupName) throws Exception;

    public void deleteHostBackup(Host host, String backupName) throws Exception;

    public void suspendLoadBalancer(LoadBalancer lb) throws Exception;

    public void removeSuspension(LoadBalancer lb) throws Exception;

    public void addVirtualIps(Integer id, Integer accountId, LoadBalancer loadBalancer) throws Exception;

    public void deleteAccessList(Integer id, Integer accountId) throws Exception;

    public void deleteVirtualIp(LoadBalancer lb, Integer id) throws Exception;

    public void deleteVirtualIps(LoadBalancer lb, List<Integer> ids) throws Exception;

    public void setErrorFile(LoadBalancer loadBalancer, String content) throws Exception, DecryptException, MalformedURLException;

    public int getTotalCurrentConnectionsForHost(Host host) throws Exception;

    public Integer getLoadBalancerCurrentConnections(LoadBalancer lb, boolean isSsl) throws Exception;

    public Long getLoadBalancerBytesIn(LoadBalancer lb, boolean isSsl) throws Exception;

    public Long getLoadBalancerBytesOut(LoadBalancer lb, boolean isSsl) throws Exception;

    public Stats getLoadBalancerStats(Integer loadbalancerId, Integer accountId) throws Exception;

    public Hostssubnet getSubnetMappings(Host host) throws Exception;

    public void setSubnetMappings(Host host, Hostssubnet hostssubnet) throws Exception;

    public void deleteSubnetMappings(Host host, Hostssubnet hostssubnet) throws Exception;

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

    public void updateSslTermination(LoadBalancer loadBalancer, ZeusSslTermination sslTermination) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, ZxtmRollBackException;

    public void removeSslTermination(LoadBalancer lb) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, ZxtmRollBackException;

    public void enableDisableSslTermination(LoadBalancer loadBalancer, boolean isSslTermination) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, ZxtmRollBackException;

    public void setNodesPriorities(String poolName, LoadBalancer lb) throws DecryptException, EntityNotFoundException, MalformedURLException, RemoteException;
}