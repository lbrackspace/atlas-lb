package org.openstack.atlas.adapter.service;

import org.apache.axis.AxisFault;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.adapter.exceptions.ZxtmRollBackException;
import org.openstack.atlas.adapter.zxtm.ZxtmServiceStubs;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.pojos.Stats;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ReverseProxyLoadBalancerAdapter {

    ZxtmServiceStubs getServiceStubs(LoadBalancerEndpointConfiguration config)
            throws AxisFault;

    ZxtmServiceStubs getStatsStubs(URL endpoint, LoadBalancerEndpointConfiguration config)
            throws AxisFault;

    void createLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void updateLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void deleteLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void updateProtocol(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void updateHalfClosed(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void updateHttpsRedirect(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException, ZxtmRollBackException;

    void updatePort(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Integer port)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void updateTimeout(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void setLoadBalancingAlgorithm(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, LoadBalancerAlgorithm algorithm)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void addVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void deleteVirtualIp(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Integer vipId)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void deleteVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, List<Integer> vipId)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void changeHostForLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Host newHost)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void setNodes(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void removeNodes(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Collection<Node> nodes)
            throws AxisFault, InsufficientRequestException, RollBackException;

    void removeNode(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, String ipAddress, Integer port)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void setNodeWeights(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Collection<Node> nodes)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void setSessionPersistence(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, SessionPersistence mode)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void removeSessionPersistence(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void setSessionPersistence(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void removeSessionPersistence(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void updateConnectionLogging(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException, StmRollBackException;

    void updateContentCaching(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void updateHealthMonitor(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void removeHealthMonitor(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, StmRollBackException;

    void updateAccessList(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, StmRollBackException;

    void deleteAccessList(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId)
            throws RemoteException, InsufficientRequestException;

    void deleteAccessList(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, StmRollBackException, StingrayRestClientObjectNotFoundException, StingrayRestClientException;

    void suspendLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException;

    void removeSuspension(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException;

    void createHostBackup(LoadBalancerEndpointConfiguration config, String backupName)
            throws RemoteException;

    void deleteHostBackup(LoadBalancerEndpointConfiguration config, String backupName)
            throws RemoteException;

    void restoreHostBackup(LoadBalancerEndpointConfiguration config, String backupName)
            throws RemoteException;

    void setSubnetMappings(LoadBalancerEndpointConfiguration config, Hostssubnet hostssubnet)
            throws RemoteException;

    void deleteSubnetMappings(LoadBalancerEndpointConfiguration config, Hostssubnet hostssubnet)
            throws RemoteException;

    Hostssubnet getSubnetMappings(LoadBalancerEndpointConfiguration config, String host)
            throws RemoteException;

    List<String> getStatsSystemLoadBalancerNames(LoadBalancerEndpointConfiguration config)
            throws RemoteException;

    Map<String, Integer> getLoadBalancerCurrentConnections(LoadBalancerEndpointConfiguration config, List<String> names)
            throws RemoteException;

    Integer getLoadBalancerCurrentConnections(LoadBalancerEndpointConfiguration config, Integer accountId, Integer loadBalancerId, boolean isSsl)
            throws RemoteException, InsufficientRequestException;

    int getTotalCurrentConnectionsForHost(LoadBalancerEndpointConfiguration config)
            throws RemoteException;

    Stats getLoadBalancerStats(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException;

    Map<String, Long> getLoadBalancerBytesIn(LoadBalancerEndpointConfiguration config, List<String> names)
            throws RemoteException;

    Long getLoadBalancerBytesIn(LoadBalancerEndpointConfiguration config, Integer accountId, Integer loadBalancerId, boolean isSsl)
            throws RemoteException, InsufficientRequestException;

    Map<String, Long> getLoadBalancerBytesOut(LoadBalancerEndpointConfiguration config, List<String> names)
            throws RemoteException;

    Long getLoadBalancerBytesOut(LoadBalancerEndpointConfiguration config, Integer accountId, Integer loadBalancerId, boolean isSsl)
            throws RemoteException, InsufficientRequestException;

    Long getHostBytesIn(LoadBalancerEndpointConfiguration config)
            throws RemoteException;

    Long getHostBytesOut(LoadBalancerEndpointConfiguration config)
            throws RemoteException;

    boolean isEndPointWorking(LoadBalancerEndpointConfiguration config)
            throws RemoteException;

    void deleteRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void setRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, RateLimit rateLimit)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void updateRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, RateLimit rateLimit)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void removeAndSetDefaultErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void setDefaultErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void uploadDefaultErrorFile(LoadBalancerEndpointConfiguration config, String content)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void deleteErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws AxisFault, InsufficientRequestException, RollBackException;

    void setErrorFile(LoadBalancerEndpointConfiguration conf, LoadBalancer loadBalancer, String content)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void updateSslTermination(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, ZeusSslTermination sslTermination)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void removeSslTermination(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void enableDisableSslTermination(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, boolean isSslTermination)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void setNodesPriorities(LoadBalancerEndpointConfiguration config, String poolName, LoadBalancer lb)
            throws RemoteException;

    void updateCertificateMapping(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, CertificateMapping certMappingToUpdate)
            throws RemoteException, InsufficientRequestException, RollBackException;

    void deleteCertificateMapping(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Integer certificateMappingId)
            throws RemoteException, InsufficientRequestException, RollBackException;
}
