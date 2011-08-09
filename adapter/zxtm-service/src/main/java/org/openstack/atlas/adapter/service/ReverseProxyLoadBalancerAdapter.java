package org.openstack.atlas.adapter.service;

import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.ZxtmRollBackException;
import org.openstack.atlas.adapter.zxtm.ZxtmServiceStubs;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.apache.axis.AxisFault;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ReverseProxyLoadBalancerAdapter {

    public ZxtmServiceStubs getServiceStubs(LoadBalancerEndpointConfiguration config)
            throws AxisFault;

    public void createLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException;

    public void deleteLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException;

    public void updateProtocol(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, LoadBalancerProtocol protocol)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException;

    public void updatePort(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Integer port)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException;

    public void setLoadBalancingAlgorithm(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, LoadBalancerAlgorithm algorithm)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException;

    public void addVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException;

    public void deleteVirtualIp(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Integer vipId)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException;

    public void deleteVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, List<Integer> vipId)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException;

    public void changeHostForLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Host newHost)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException;

    public void setNodes(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Collection<Node> nodes)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException;

    public void removeNodes(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Collection<Node> nodes)
            throws AxisFault, InsufficientRequestException, ZxtmRollBackException;

    public void removeNode(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, String ipAddress, Integer port)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException;

    public void setNodeWeights(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Collection<Node> nodes)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException;

    public void setSessionPersistence(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, SessionPersistence mode)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException;

    public void removeSessionPersistence(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException;

    public void updateConnectionLogging(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, boolean isConnectionLogging, LoadBalancerProtocol protocol)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException;

    public void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, ConnectionLimit throttle)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException;

    public void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException;

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
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException;

    public void updateRateLimit(LoadBalancerEndpointConfiguration config, int id, int accountId, RateLimit rateLimit)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException;

    public void removeAndSetDefaultErrorFile(LoadBalancerEndpointConfiguration config, Integer accountid, Integer loadbalancerId) throws RemoteException, InsufficientRequestException;

    public void setDefaultErrorFile(LoadBalancerEndpointConfiguration config,  Integer loadbalancerId, Integer accountId) throws RemoteException, InsufficientRequestException;

    public void uploadDefaultErrorFile(LoadBalancerEndpointConfiguration config,  String content) throws RemoteException, InsufficientRequestException;

    public void deleteErrorFile(LoadBalancerEndpointConfiguration config, String fileName) throws RemoteException, InsufficientRequestException;

    public void setErrorFile(LoadBalancerEndpointConfiguration conf, Integer loadbalancerId, Integer accountId, String content)
            throws RemoteException;
}
