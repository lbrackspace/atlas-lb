package org.openstack.atlas.api.integration;

import org.apache.axis.AxisFault;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.ObjectExistsException;
import org.openstack.atlas.adapter.exceptions.ZxtmRollBackException;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.util.crypto.exception.DecryptException;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ReverseProxyLoadBalancerService {

    void createLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, EntityNotFoundException, DecryptException, MalformedURLException;

    void deleteLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, EntityNotFoundException, DecryptException, MalformedURLException;

    void updateAlgorithm(LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, EntityNotFoundException, DecryptException, MalformedURLException;

    void updatePort(LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, Exception;

    void updateProtocol(LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, Exception;

    void changeHostForLoadBalancer(LoadBalancer lb, Host newHost) throws ObjectExistsException, RemoteException, InsufficientRequestException, Exception;

    void updateConnectionLogging(LoadBalancer lb) throws ObjectExistsException, RemoteException, InsufficientRequestException, Exception;

    void setNodes(Integer id, Integer accountId, Set<Node> nodes) throws Exception;

    void removeNode(Integer id, Integer accountId, Node node) throws Exception;

    void removeNodes(Integer lbId, Integer accountId,Collection<Node> nodes) throws Exception;

    void setNodeWeights(Integer id, Integer accountId, Set<Node> nodes) throws Exception;

    void updateAccessList(Integer id, Integer accountId, Collection<AccessList> accessListItems) throws Exception;

    void updateConnectionThrottle(Integer id, Integer accountId, ConnectionLimit connectionThrottle) throws Exception;

    void deleteConnectionThrottle(Integer id, Integer accountId) throws Exception;

    void updateSessionPersistence(Integer id, Integer accountId, SessionPersistence persistenceMode) throws Exception;

    void removeSessionPersistence(Integer id, Integer accountId) throws Exception;

    void updateHealthMonitor(Integer lbId, Integer accountId, HealthMonitor monitor) throws Exception;

    void removeHealthMonitor(Integer lbId, Integer accountId) throws Exception;

    void createHostBackup(Host host, String backupName) throws Exception;

    void restoreHostBackup(Host host, String backupName) throws Exception;

    void deleteHostBackup(Host host, String backupName) throws Exception;

    void suspendLoadBalancer(Integer id, Integer accountId) throws Exception;

    void removeSuspension(Integer id, Integer accountId) throws Exception;

    void addVirtualIps(Integer id, Integer accountId, LoadBalancer loadBalancer) throws Exception;

    void deleteAccessList(Integer id, Integer accountId) throws Exception;

    void deleteVirtualIp(LoadBalancer lb, Integer id) throws Exception;

    void deleteVirtualIps(LoadBalancer lb, List<Integer> ids) throws Exception;

    void setErrorFile(Integer lid,Integer aid,String content) throws Exception,DecryptException, MalformedURLException;

    int getTotalCurrentConnectionsForHost(Host host) throws Exception;

    Hostssubnet getSubnetMappings(Host host) throws Exception;

    void setSubnetMappings(Host host, Hostssubnet hostssubnet) throws Exception;

    void deleteSubnetMappings(Host host, Hostssubnet hostssubnet) throws Exception;

    public LoadBalancerEndpointConfiguration getConfig(Host host) throws DecryptException, MalformedURLException;

    public LoadBalancerEndpointConfiguration getConfigHost(Host host) throws DecryptException, MalformedURLException;

    public boolean isEndPointWorking(Host host) throws Exception;

    public void setRateLimit(int id, int accountId, RateLimit rateLimit) throws Exception;

    public void deleteRateLimit(int id, int accountId) throws Exception;

    public void updateRateLimit(int id, int accountId, RateLimit rateLimit) throws Exception;

    public void removeAndSetDefaultErrorFile(Integer accountid, Integer loadbalancerId) throws EntityNotFoundException, MalformedURLException, DecryptException, RemoteException, InsufficientRequestException;

    public void deleteErrorFile(Integer loadbalancerId,Integer accountId) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException;

    public void uploadDefaultErrorFile(Integer clusterId, String content) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException;

    public void setDefaultErrorFile(Integer loadbalancerId, Integer accountId) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException;
}