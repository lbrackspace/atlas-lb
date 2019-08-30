package org.openstack.atlas.api.integration;

import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.Stats;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.counters.VirtualServerStats;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;

import java.net.MalformedURLException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

public interface ReverseProxyLoadBalancerStmService {
    LoadBalancerEndpointConfiguration getConfig(Host host) throws Exception;

    LoadBalancerEndpointConfiguration getConfigHost(Host host) throws Exception;

    void createLoadBalancer(LoadBalancer lb) throws Exception;

    void updateLoadBalancer(LoadBalancer loadBalancer, LoadBalancer queLb) throws Exception;

    void deleteLoadBalancer(LoadBalancer lb) throws Exception;

    void changeHostForLoadBalancers(List<LoadBalancer> lbs, Host newHost) throws Exception;

    void suspendLoadBalancer(LoadBalancer lb) throws Exception;

    void removeSuspension(LoadBalancer lb) throws Exception;

    void updateSslTermination(LoadBalancer loadBalancer, ZeusSslTermination sslTermination) throws Exception;

    void removeSslTermination(LoadBalancer lb) throws Exception;

    void updateCertificateMapping(LoadBalancer loadBalancer, CertificateMapping certificateMapping) throws Exception;

    void deleteCertificateMapping(LoadBalancer loadBalancer, CertificateMapping certificateMapping) throws Exception;

    void setNodes(LoadBalancer lb) throws Exception;

    void removeNode(LoadBalancer lb, Node node) throws Exception;

    void removeNodes(LoadBalancer lb, List<Node> doomedNodes) throws Exception;

    void addVirtualIps(Integer id, Integer accountId, LoadBalancer loadBalancer) throws Exception;

    void deleteAccessList(LoadBalancer lb, List<Integer> accessListToDelete) throws Exception;

    void deleteVirtualIps(LoadBalancer lb, List<Integer> ids, UserPages up) throws Exception;

    void updateAccessList(LoadBalancer loadBalancer) throws Exception;

    void updateConnectionThrottle(LoadBalancer loadbalancer) throws Exception;

    void deleteConnectionThrottle(LoadBalancer loadBalancer) throws Exception;

    void updateHealthMonitor(LoadBalancer loadBalancer) throws Exception;

    void removeHealthMonitor(LoadBalancer loadBalancer) throws Exception;

    void uploadDefaultErrorFile(Integer clusterId, String content) throws Exception;

    void deleteErrorFile(LoadBalancer loadBalancer, UserPages up) throws Exception;

    void setErrorFile(LoadBalancer loadBalancer, String content) throws Exception;

    void setRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) throws Exception;

    void deleteRateLimit(LoadBalancer loadBalancer) throws Exception;

    void updateRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) throws Exception;

    Stats getVirtualServerStats(LoadBalancer loadBalancer) throws Exception;

    boolean isEndPointWorking(Host host) throws Exception;

    String getSslCiphers(Integer accountId, Integer loadbalancerId) throws RemoteException, EntityNotFoundException, MalformedURLException, DecryptException, RollBackException, InsufficientRequestException, StingrayRestClientException, StingrayRestClientObjectNotFoundException;

    void setSslCiphers(Integer accountId, Integer loadbalancerId,String ciphersStr) throws RemoteException, EntityNotFoundException, MalformedURLException, DecryptException, RollBackException, InsufficientRequestException, StingrayRestClientException, StingrayRestClientObjectNotFoundException;

    String getSsl3Ciphers() throws RemoteException, EntityNotFoundException, MalformedURLException, DecryptException, RollBackException, InsufficientRequestException, StingrayRestClientException, StingrayRestClientObjectNotFoundException;

    //Deprecated

//    Hostssubnet getSubnetMappings(Host host) throws Exception;
//
//    void setSubnetMappings(Host host, Hostssubnet hostssubnet) throws Exception;
//
//    void deleteSubnetMappings(Host host, Hostssubnet hostssubnet) throws Exception;


}