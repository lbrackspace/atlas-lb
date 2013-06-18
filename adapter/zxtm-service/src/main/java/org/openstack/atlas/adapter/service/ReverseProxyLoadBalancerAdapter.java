package org.openstack.atlas.adapter.service;

import org.apache.axis.AxisFault;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.adapter.zxtm.ZxtmServiceStubs;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.pojos.Stats;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.rackspace.stingray.client.StingrayRestClient;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ReverseProxyLoadBalancerAdapter {

    public StingrayRestClient loadSTMRestClient(LoadBalancerEndpointConfiguration config)
            throws RollBackException;

    public ZxtmServiceStubs getServiceStubs(LoadBalancerEndpointConfiguration config)
            throws AxisFault;

    public void createLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void updateLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void deleteLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void updateProtocol(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void updateHalfClosed(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void updatePort(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Integer port)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void updateTimeout(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void setLoadBalancingAlgorithm(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, LoadBalancerAlgorithm algorithm)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void addVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void deleteVirtualIp(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Integer vipId)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void deleteVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, List<Integer> vipId)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void changeHostForLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Host newHost)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void setNodes(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void removeNodes(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Collection<Node> nodes)
            throws AxisFault, InsufficientRequestException, RollBackException;

    public void removeNode(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, String ipAddress, Integer port)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void setNodeWeights(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Collection<Node> nodes)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void setSessionPersistence(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, SessionPersistence mode)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void removeSessionPersistence(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void setSessionPersistence(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void removeSessionPersistence(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void updateConnectionLogging(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException, StmRollBackException;

    public void updateContentCaching(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void updateHealthMonitor(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    @Deprecated
    public void updateHealthMonitor(LoadBalancerEndpointConfiguration config, int lbId, int accountId, HealthMonitor healthMonitor)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void removeHealthMonitor(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException;

    public void updateAccessList(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException;

    public void deleteAccessList(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId)
            throws RemoteException, InsufficientRequestException;

    public void suspendLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException;

    public void removeSuspension(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
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

    public Integer getLoadBalancerCurrentConnections(LoadBalancerEndpointConfiguration config, Integer accountId, Integer loadBalancerId, boolean isSsl)
            throws RemoteException, InsufficientRequestException;

    public int getTotalCurrentConnectionsForHost(LoadBalancerEndpointConfiguration config)
            throws RemoteException;

    public Stats getLoadBalancerStats(LoadBalancerEndpointConfiguration config, Integer loadbalancerId, Integer accountId)
            throws RemoteException, InsufficientRequestException;

    public Map<String, Long> getLoadBalancerBytesIn(LoadBalancerEndpointConfiguration config, List<String> names)
            throws RemoteException;

    public Long getLoadBalancerBytesIn(LoadBalancerEndpointConfiguration config, Integer accountId, Integer loadBalancerId, boolean isSsl)
            throws RemoteException, InsufficientRequestException;

    public Map<String, Long> getLoadBalancerBytesOut(LoadBalancerEndpointConfiguration config, List<String> names)
            throws RemoteException;

    public Long getLoadBalancerBytesOut(LoadBalancerEndpointConfiguration config, Integer accountId, Integer loadBalancerId, boolean isSsl)
            throws RemoteException, InsufficientRequestException;

    public Long getHostBytesIn(LoadBalancerEndpointConfiguration config)
            throws RemoteException;

    public Long getHostBytesOut(LoadBalancerEndpointConfiguration config)
            throws RemoteException;

    public boolean isEndPointWorking(LoadBalancerEndpointConfiguration config)
            throws RemoteException;

    public void deleteRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException;

    public void setRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, RateLimit rateLimit)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void updateRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, RateLimit rateLimit)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void removeAndSetDefaultErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void setDefaultErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void uploadDefaultErrorFile(LoadBalancerEndpointConfiguration config, String content)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void deleteErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws AxisFault, InsufficientRequestException, RollBackException;

    public void setErrorFile(LoadBalancerEndpointConfiguration conf, LoadBalancer loadBalancer, String content)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void updateSslTermination(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, ZeusSslTermination sslTermination)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void removeSslTermination(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void enableDisableSslTermination(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, boolean isSslTermination)
            throws RemoteException, InsufficientRequestException, RollBackException;

    public void setNodesPriorities(LoadBalancerEndpointConfiguration config, String poolName, LoadBalancer lb) throws RemoteException;
}
