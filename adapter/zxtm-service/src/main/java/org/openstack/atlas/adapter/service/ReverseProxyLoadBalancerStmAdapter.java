package org.openstack.atlas.adapter.service;

import org.apache.axis.AxisFault;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.pojos.Stats;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;

import java.rmi.RemoteException;
import java.util.List;

public interface ReverseProxyLoadBalancerStmAdapter {

    public void createLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, RollBackException;

    public void updateLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, LoadBalancer queLb)
            throws InsufficientRequestException, StmRollBackException;

    public void deleteLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, RollBackException;

    public void addSuspension(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws InsufficientRequestException, StmRollBackException;

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
            throws InsufficientRequestException, StmRollBackException;

    public void updateSessionPersistence(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, LoadBalancer queLb)
            throws InsufficientRequestException, RollBackException;

    public void deleteSessionPersistence(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, LoadBalancer queLb) throws StmRollBackException, InsufficientRequestException;

    public void updateAccessList(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, StmRollBackException;

    public void deleteAccessList(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, List<Integer> accessListToDelete)
            throws InsufficientRequestException, StmRollBackException;

    public void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, StmRollBackException;

    public void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, StmRollBackException;

    public void setErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, String content)
            throws InsufficientRequestException, StmRollBackException;

    public void deleteErrorFile(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, StmRollBackException;

    public void uploadDefaultErrorFile(LoadBalancerEndpointConfiguration config, String content)
            throws InsufficientRequestException, RollBackException;

    public void deleteRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, RollBackException;

    public void setRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, RateLimit rateLimit)
            throws InsufficientRequestException, RollBackException;

    public void updateRateLimit(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, RateLimit rateLimit)
            throws InsufficientRequestException, RollBackException;

    public Stats getVirtualServerStats(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, StingrayRestClientObjectNotFoundException, StingrayRestClientException;

    String getSslCiphersByVhost(LoadBalancerEndpointConfiguration conf, Integer accountId, Integer loadbalancerId)
            throws RemoteException, EntityNotFoundException, RollBackException, InsufficientRequestException, StingrayRestClientObjectNotFoundException, StingrayRestClientException;

    public String getSsl3Ciphers(LoadBalancerEndpointConfiguration config) throws RemoteException, RollBackException, InsufficientRequestException, StingrayRestClientObjectNotFoundException, StingrayRestClientException;

    public void setSslCiphersByVhost(LoadBalancerEndpointConfiguration config, Integer accountId, Integer loadbalancerId, String ciphers)
            throws RemoteException, EntityNotFoundException, RollBackException, InsufficientRequestException, StingrayRestClientObjectNotFoundException, StingrayRestClientException;

    public void enableDisableTLS_10(LoadBalancerEndpointConfiguration conf, LoadBalancer loadBalancer, boolean isEnabled)
            throws RemoteException, InsufficientRequestException, RollBackException, StingrayRestClientObjectNotFoundException, StingrayRestClientException;

    public void enableDisableTLS_11(LoadBalancerEndpointConfiguration conf, LoadBalancer loadBalancer, boolean isEnabled)
            throws RemoteException, InsufficientRequestException, RollBackException, StingrayRestClientObjectNotFoundException, StingrayRestClientException;

    public void setSubnetMappings(LoadBalancerEndpointConfiguration config, Hostssubnet hostssubnet)
            throws StmRollBackException, InsufficientRequestException;

    public void deleteSubnetMappings(LoadBalancerEndpointConfiguration config, Hostssubnet hostssubnet)
            throws StmRollBackException, InsufficientRequestException;

    public Hostssubnet getSubnetMappings(LoadBalancerEndpointConfiguration config, String host)
            throws StmRollBackException, InsufficientRequestException;

    public boolean isEndPointWorking(LoadBalancerEndpointConfiguration config)
            throws StmRollBackException;

    // Host stats
    int getTotalCurrentConnectionsForHost(LoadBalancerEndpointConfiguration config)
            throws InsufficientRequestException, StmRollBackException;

    Long getHostBytesIn(LoadBalancerEndpointConfiguration config)
            throws InsufficientRequestException, StmRollBackException;

    Long getHostBytesOut(LoadBalancerEndpointConfiguration config)
            throws InsufficientRequestException, StmRollBackException;


}
