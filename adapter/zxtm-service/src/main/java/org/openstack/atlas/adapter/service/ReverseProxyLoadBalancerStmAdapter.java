package org.openstack.atlas.adapter.service;

import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.Stats;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.counters.VirtualServerStats;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;

import java.net.URI;
import java.util.List;
import java.util.Set;

public interface ReverseProxyLoadBalancerStmAdapter {

//    public StingrayRestClient loadSTMRestClient(LoadBalancerEndpointConfiguration config)
//            throws RollBackException, InsufficientRequestException;

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

    public void deleteVirtualIp(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Integer vipId)
            throws InsufficientRequestException, RollBackException;

    public void deleteVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, List<Integer> vipId)
            throws InsufficientRequestException, RollBackException;

    public void changeHostForLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Host newHost)
            throws InsufficientRequestException, RollBackException;

    public void updateProtection(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, RollBackException;

    public void deleteProtection(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, RollBackException;

    public void updateHealthMonitor(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, RollBackException;

    public void deleteHealthMonitor(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer)
            throws InsufficientRequestException, StmRollBackException;

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

//    public void setSubnetMappings(LoadBalancerEndpointConfiguration config, Hostssubnet hostssubnet)
//            throws StmRollBackException;
//
//    public void deleteSubnetMappings(LoadBalancerEndpointConfiguration config, Hostssubnet hostssubnet)
//            throws StmRollBackException;
//
//    public Hostssubnet getSubnetMappings(LoadBalancerEndpointConfiguration config, String host)
//            throws StmRollBackException;

    public boolean isEndPointWorking(LoadBalancerEndpointConfiguration config)
            throws StmRollBackException;

}
