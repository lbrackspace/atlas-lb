package org.openstack.atlas.adapter.service;

import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.AdapterRollBackException;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;


import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ReverseProxyLoadBalancerAdapter {

    public void createLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException;

    public void deleteLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException;

    public void updateProtocol(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, LoadBalancerProtocol protocol)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException;

    public void updatePort(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Integer port)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException;

    public void setLoadBalancingAlgorithm(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, LoadBalancerAlgorithm algorithm)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException;

    public void addVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException;

    public void deleteVirtualIp(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Integer vipId)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException;

    public void deleteVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, List<Integer> vipId)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException;

    public void changeHostForLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Host newHost)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException;

    public void setNodes(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Collection<Node> nodes)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException;

    public void removeNodes(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Collection<Node> nodes)
            throws InsufficientRequestException, AdapterRollBackException;

    public void removeNode(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, String ipAddress, Integer port)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException;

    public void setNodeWeights(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Collection<Node> nodes)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException;

    public void setSessionPersistence(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, SessionPersistence mode)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException;

    public void removeSessionPersistence(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException;

    public void updateConnectionLogging(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, boolean isConnectionLogging, LoadBalancerProtocol protocol)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException;

    public void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, ConnectionLimit throttle)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException;

    public void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException;

    public void updateHealthMonitor(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, HealthMonitor healthMonitor)
            throws RemoteException, InsufficientRequestException;

    public void removeHealthMonitor(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId)
            throws RemoteException, InsufficientRequestException;

    public void updateAccessList(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Collection<AccessList> accessListItems)
            throws RemoteException, InsufficientRequestException;

    public void deleteAccessList(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId)
            throws RemoteException, InsufficientRequestException;

    public void suspendLoadBalancer(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId)
            throws RemoteException, InsufficientRequestException;

    public void removeSuspension(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId)
            throws RemoteException, InsufficientRequestException;

    public void createHostBackup(LoadBalancerEndpointConfiguration config, String backupName)
            throws RemoteException;

    public void deleteHostBackup(LoadBalancerEndpointConfiguration config, String backupName)
            throws RemoteException;

    public void restoreHostBackup(LoadBalancerEndpointConfiguration config, String backupName)
            throws RemoteException;

    public void setSubnetMappings(LoadBalancerEndpointConfiguration config, Hostssubnet hostssubnet)
            throws RemoteException;

    public void deleteSubnetMappings(LoadBalancerEndpointConfiguration config, Hostssubnet hostssubnet)
            throws RemoteException;

    public Hostssubnet getSubnetMappings(LoadBalancerEndpointConfiguration config, String host)
            throws RemoteException;

    public List<String> getStatsSystemLoadBalancerNames(LoadBalancerEndpointConfiguration config)
            throws RemoteException;

    public Map<String, Integer> getLoadBalancerCurrentConnections(LoadBalancerEndpointConfiguration config, List<String> names)
            throws RemoteException;

    public int getTotalCurrentConnectionsForHost(LoadBalancerEndpointConfiguration config)
            throws RemoteException;

    public Map<String, Long> getLoadBalancerBytesIn(LoadBalancerEndpointConfiguration config, List<String> names)
            throws RemoteException;

    public Map<String, Long> getLoadBalancerBytesOut(LoadBalancerEndpointConfiguration config, List<String> names)
            throws RemoteException;

    public Long getHostBytesIn(LoadBalancerEndpointConfiguration config)
            throws RemoteException;

    public Long getHostBytesOut(LoadBalancerEndpointConfiguration config)
            throws RemoteException;

    public boolean isEndPointWorking(LoadBalancerEndpointConfiguration config)
            throws RemoteException;

    public void deleteRateLimit(LoadBalancerEndpointConfiguration config, int id, int accountId)
            throws RemoteException, InsufficientRequestException;

    public void setRateLimit(LoadBalancerEndpointConfiguration config, int id, int accountId, RateLimit rateLimit)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException;

    public void updateRateLimit(LoadBalancerEndpointConfiguration config, int id, int accountId, RateLimit rateLimit)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException;
}
