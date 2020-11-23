package org.openstack.atlas.adapter.service;

import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.exceptions.VTMRollBackException;
import org.openstack.atlas.service.domain.entities.CertificateMapping;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.RateLimit;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.pojos.Stats;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;

import java.rmi.RemoteException;
import java.util.List;

public interface ReverseProxyLoadBalancerVTMAdapter {

    public void createLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, RollBackException;

    public void updateLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, LoadBalancer queLb)
            throws InsufficientRequestException, VTMRollBackException;

    public void deleteLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, RollBackException;

    public void addSuspension(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws InsufficientRequestException, VTMRollBackException;

    public void removeSuspension(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws InsufficientRequestException, RollBackException;

    public void updateCertificateMapping(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, CertificateMapping certificateMapping)
            throws InsufficientRequestException, RollBackException;

    public void updateCertificateMappings(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, RollBackException;

    public void deleteCertificateMapping(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, CertificateMapping certificateMapping)
            throws InsufficientRequestException, RollBackException;

    public void updateSslTermination(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, ZeusSslTermination sslTermination)
            throws InsufficientRequestException, RollBackException;

    public void removeSslTermination(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws InsufficientRequestException, RollBackException;

    public void setNodes(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws InsufficientRequestException, RollBackException;

    public void removeNodes(LoadBalancerEndpointConfiguration config, LoadBalancer lb, List<Node> doomedNodes)
            throws InsufficientRequestException, RollBackException;

    public void removeNode(LoadBalancerEndpointConfiguration config, LoadBalancer lb, Node nodeToDelete)
            throws InsufficientRequestException, RollBackException;

    public void updateVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, RollBackException;

    public void deleteVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, List<Integer> vipId)
            throws InsufficientRequestException, RollBackException;

    public void changeHostForLoadBalancers(LoadBalancerEndpointConfiguration configOld, LoadBalancerEndpointConfiguration configNew, List<LoadBalancer> loadBalancers, Integer retryCount)
            throws InsufficientRequestException, RollBackException, RemoteException;

    public void updateProtection(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, RollBackException;

    public void deleteProtection(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, RollBackException;

    public void updateHealthMonitor(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, RollBackException;

    public void deleteHealthMonitor(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, VTMRollBackException;

    public void updateAccessList(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, VTMRollBackException;

    public void deleteAccessList(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, List<Integer> accessListToDelete)
            throws InsufficientRequestException, VTMRollBackException;

    public void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, VTMRollBackException;

    public void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, VTMRollBackException;

    public void setErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, String content)
            throws InsufficientRequestException, VTMRollBackException;

    public void deleteErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, VTMRollBackException;

    public void uploadDefaultErrorFile(LoadBalancerEndpointConfiguration config, String content)
            throws InsufficientRequestException, RollBackException;

    public void deleteRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, RollBackException;

    public void setRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, RateLimit rateLimit)
            throws InsufficientRequestException, RollBackException;

    public void updateRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, RateLimit rateLimit)
            throws InsufficientRequestException, RollBackException;

    public void updateSessionPersistence(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, LoadBalancer queLb)
            throws InsufficientRequestException, RollBackException;

    public void deleteSessionPersistence(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, LoadBalancer queLb) throws VTMRollBackException, InsufficientRequestException;

    public Stats getVirtualServerStats(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, VTMRestClientObjectNotFoundException, VTMRestClientException;

    String getSslCiphersByVhost(LoadBalancerEndpointConfiguration conf, Integer accountId, Integer loadbalancerId)
            throws RemoteException, EntityNotFoundException, RollBackException, InsufficientRequestException, VTMRestClientObjectNotFoundException, VTMRestClientException;

    public String getSsl3Ciphers(LoadBalancerEndpointConfiguration config) throws RemoteException, RollBackException, InsufficientRequestException, VTMRestClientObjectNotFoundException, VTMRestClientException;

    public void setSslCiphersByVhost(LoadBalancerEndpointConfiguration config, Integer accountId, Integer loadbalancerId, String ciphers)
            throws RemoteException, EntityNotFoundException, RollBackException, InsufficientRequestException, VTMRestClientObjectNotFoundException, VTMRestClientException;

    public void enableDisableTLS_10(LoadBalancerEndpointConfiguration conf, LoadBalancer loadBalancer, boolean isEnabled)
            throws RemoteException, InsufficientRequestException, RollBackException, VTMRestClientObjectNotFoundException, VTMRestClientException;

    public void enableDisableTLS_11(LoadBalancerEndpointConfiguration conf, LoadBalancer loadBalancer, boolean isEnabled)
            throws RemoteException, InsufficientRequestException, RollBackException, VTMRestClientObjectNotFoundException, VTMRestClientException;

    public void setSubnetMappings(LoadBalancerEndpointConfiguration config, Hostssubnet hostssubnet)
            throws VTMRollBackException;

    public void deleteSubnetMappings(LoadBalancerEndpointConfiguration config, Hostssubnet hostssubnet)
            throws VTMRollBackException;

    public Hostssubnet getSubnetMappings(LoadBalancerEndpointConfiguration config, String host)
            throws VTMRollBackException;

    public boolean isEndPointWorking(LoadBalancerEndpointConfiguration config)
            throws VTMRollBackException;

    // Host stats
    int getTotalCurrentConnectionsForHost(LoadBalancerEndpointConfiguration config)
            throws RemoteException, VTMRestClientObjectNotFoundException, VTMRestClientException;

    Long getHostBytesIn(LoadBalancerEndpointConfiguration config)
            throws RemoteException, VTMRestClientObjectNotFoundException, VTMRestClientException;

    Long getHostBytesOut(LoadBalancerEndpointConfiguration config)
            throws RemoteException, VTMRestClientObjectNotFoundException, VTMRestClientException;

    // Host Backup

    void createHostBackup(LoadBalancerEndpointConfiguration config, String backupName)
            throws RemoteException,VTMRestClientObjectNotFoundException, VTMRestClientException, VTMRollBackException;

    void restoreHostBackup(LoadBalancerEndpointConfiguration config, String backupName)
            throws RemoteException,VTMRestClientObjectNotFoundException, VTMRestClientException, VTMRollBackException;

    void deleteHostBackup(LoadBalancerEndpointConfiguration config, String backupName)
            throws RemoteException, VTMRestClientObjectNotFoundException, VTMRestClientException, VTMRollBackException;

}
