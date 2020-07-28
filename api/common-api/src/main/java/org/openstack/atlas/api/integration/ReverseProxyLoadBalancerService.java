package org.openstack.atlas.api.integration;

import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.ObjectExistsException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
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

    void createLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException;

    void deleteLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException;

    void syncLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException;

    void updateAlgorithm(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException;

    void updatePort(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, Exception;

    void updateTimeout(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, Exception;

    void updateProtocol(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, Exception;

    void updateHalfClosed(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, Exception;

    void updateHttpsRedirect(LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, Exception;

    void changeHostForLoadBalancer(LoadBalancer lb, Host newHost) throws ObjectExistsException, RemoteException, InsufficientRequestException, Exception;

    void updateConnectionLogging(LoadBalancer lb) throws ObjectExistsException, RemoteException, InsufficientRequestException, Exception;

    void updateContentCaching(LoadBalancer lb) throws ObjectExistsException, RemoteException, InsufficientRequestException, Exception;

    void setNodes(LoadBalancer lb) throws Exception;

    void removeNode(Integer id, Integer accountId, Node node) throws Exception;

    void removeNodes(Integer lbId, Integer accountId, Collection<Node> nodes) throws Exception;

    void setNodeWeights(Integer id, Integer accountId, Set<Node> nodes) throws Exception;

    void updateAccessList(LoadBalancer loadBalancer) throws Exception;

    void updateConnectionThrottle(LoadBalancer loadbalancer) throws Exception;

    void deleteConnectionThrottle(LoadBalancer loadBalancer) throws Exception;

    void updateSessionPersistence(Integer id, Integer accountId, SessionPersistence persistenceMode) throws Exception;

    void removeSessionPersistence(Integer id, Integer accountId) throws Exception;

    void updateHealthMonitor(LoadBalancer loadBalancer) throws Exception;

    void removeHealthMonitor(LoadBalancer loadBalancer) throws Exception;

    void createHostBackup(Host host, String backupName) throws Exception;

    void restoreHostBackup(Host host, String backupName) throws Exception;

    void deleteHostBackup(Host host, String backupName) throws Exception;

    void suspendLoadBalancer(LoadBalancer lb) throws Exception;

    void removeSuspension(LoadBalancer lb) throws Exception;

    void addVirtualIps(Integer id, Integer accountId, LoadBalancer loadBalancer) throws Exception;

    void deleteAccessList(Integer id, Integer accountId) throws Exception;

    void deleteVirtualIp(LoadBalancer lb, Integer id) throws Exception;

    void deleteVirtualIps(LoadBalancer lb, List<Integer> ids) throws Exception;

    void setErrorFile(LoadBalancer loadBalancer, String content) throws Exception, DecryptException, MalformedURLException;

    int getTotalCurrentConnectionsForHost(Host host) throws Exception;

    Integer getLoadBalancerCurrentConnections(LoadBalancer lb, boolean isSsl) throws Exception;

    Long getLoadBalancerBytesIn(LoadBalancer lb, boolean isSsl) throws Exception;

    Long getLoadBalancerBytesOut(LoadBalancer lb, boolean isSsl) throws Exception;

    Stats getLoadBalancerStats(LoadBalancer loadBalancer) throws Exception;

    Hostssubnet getSubnetMappings(Host host) throws Exception;

    void setSubnetMappings(Host host, Hostssubnet hostssubnet) throws Exception;

    void deleteSubnetMappings(Host host, Hostssubnet hostssubnet) throws Exception;

    LoadBalancerEndpointConfiguration getConfig(Host host) throws DecryptException, MalformedURLException;

    LoadBalancerEndpointConfiguration getConfigHost(Host host) throws DecryptException, MalformedURLException;

    boolean isEndPointWorking(Host host) throws Exception;

    void setRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) throws Exception;

    void deleteRateLimit(LoadBalancer loadBalancer) throws Exception;

    void updateRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) throws Exception;

    void removeAndSetDefaultErrorFile(LoadBalancer loadBalancer) throws EntityNotFoundException, MalformedURLException, DecryptException, RemoteException, InsufficientRequestException;

    void deleteErrorFile(LoadBalancer loadBalancer) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException;

    void uploadDefaultErrorFile(Integer clusterId, String content) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException;

    void setDefaultErrorFile(LoadBalancer loadBalancer) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException;

    void updateSslTermination(LoadBalancer loadBalancer, ZeusSslTermination sslTermination) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException;

    void removeSslTermination(LoadBalancer lb) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException;

    void enableDisableSslTermination(LoadBalancer loadBalancer, boolean isSslTermination) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException;

    void setNodesPriorities(String poolName, LoadBalancer lb) throws DecryptException, EntityNotFoundException, MalformedURLException, RemoteException;

    void updateCertificateMapping(Integer lbId, Integer accountId, CertificateMapping certMappingToUpdate) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, RollBackException, InsufficientRequestException;

    void removeCertificateMapping(Integer lbId, Integer accountId, CertificateMapping certMappingToDelete) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, RollBackException, InsufficientRequestException;

    String getSslCiphers(Integer accountId, Integer loadbalancerId) throws RemoteException, EntityNotFoundException, MalformedURLException, DecryptException;

    void setSslCiphers(Integer accountId, Integer loadbalancerId,String ciphersStr) throws RemoteException, EntityNotFoundException, MalformedURLException, DecryptException;

    String getSsl3Ciphers() throws RemoteException, EntityNotFoundException, MalformedURLException, DecryptException;

    String getSsl3CiphersForLB(Integer loadBalancerID) throws RemoteException, EntityNotFoundException, MalformedURLException, DecryptException;
}