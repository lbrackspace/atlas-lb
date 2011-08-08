package org.openstack.atlas.adapter.Null;

import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.AdapterRollBackException;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.Cidr;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.pojos.Hostsubnet;
import org.openstack.atlas.service.domain.pojos.NetInterface;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.rmi.RemoteException;
import java.security.Security;
import java.util.*;

import static org.openstack.atlas.service.domain.entities.SessionPersistence.NONE;

public class NullAdapterImpl implements ReverseProxyLoadBalancerAdapter {
    public static Log LOG = LogFactory.getLog(NullAdapterImpl.class.getName());
    private static String SOURCE_IP = "SOURCE_IP";
    private static String HTTP_COOKIE = "HTTP_COOKIE";
    private static LoadBalancerAlgorithm DEFAULT_ALGORITHM = LoadBalancerAlgorithm.ROUND_ROBIN;


    @Override
    public void createLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException {
        // NOP
    }

    @Override
    public void deleteLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException {
        // NOP
    }


    @Override
    public void updateProtocol(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, LoadBalancerProtocol protocol)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException {
        // NOP
    }

    @Override
    public void updatePort(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Integer port)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException {
        // NOP
    }

    @Override
    public void setLoadBalancingAlgorithm(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, LoadBalancerAlgorithm algorithm)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException {
      // NOP
    }

    @Override
    public void addVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException {
      // NOP
    }

    @Override
    public void deleteVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer lb, List<Integer> vipIds)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException {
      // NOP
    }

    @Override
    public void deleteVirtualIp(LoadBalancerEndpointConfiguration config, LoadBalancer lb, Integer vipId)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException {
      // NOP
    }

    @Override
    public void setRateLimit(LoadBalancerEndpointConfiguration config, int id, int accountId, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, AdapterRollBackException {
      // NOP
    }

    @Override
    public void updateRateLimit(LoadBalancerEndpointConfiguration config, int id, int accountId, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, AdapterRollBackException {
      // NOP
    }

    @Override
    public void deleteRateLimit(LoadBalancerEndpointConfiguration config, int id, int accountId) throws RemoteException, InsufficientRequestException {
      // NOP
    }

    @Override
    public void changeHostForLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb, Host newHost) throws RemoteException, InsufficientRequestException, AdapterRollBackException {
      // NOP
    }

    @Override
    public void setNodes(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Collection<Node> nodes)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException {
      // NOP
    }

    @Override
    public void removeNodes(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Collection<Node> nodes)
            throws InsufficientRequestException, AdapterRollBackException {
      // NOP
    }

    @Override
    public void removeNode(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, String ipAddress, Integer port)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException {
      // NOP
    }

    @Override
    public void setNodeWeights(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Collection<Node> nodes)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException {
      // NOP
    }

    @Override
    public void setSessionPersistence(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, SessionPersistence mode)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException {
      // NOP
    }

    @Override
    public void removeSessionPersistence(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException {
      // NOP
    }

    @Override
    public void updateConnectionLogging(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, boolean isConnectionLogging, LoadBalancerProtocol protocol)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException {
       // NOP
    }

    @Override
    public void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, ConnectionLimit throttle)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException {
        // NOP         
    }

    @Override
    public void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId)
            throws RemoteException, InsufficientRequestException, AdapterRollBackException {
        // NOP
    }


    // TODO: Rollback properly
    @Override
    public void updateHealthMonitor(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, HealthMonitor healthMonitor)
            throws RemoteException, InsufficientRequestException {
        // NOP
    }

    @Override
    public void removeHealthMonitor(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId)
            throws RemoteException, InsufficientRequestException {
        // NOP
    }


    @Override
    public void updateAccessList(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Collection<AccessList> accessListItems)
            throws RemoteException, InsufficientRequestException {
       // NOP
    }

    @Override
    public void deleteAccessList(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId)
            throws RemoteException, InsufficientRequestException {
       // NOP
    }

    @Override
    public void suspendLoadBalancer(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId)
            throws RemoteException, InsufficientRequestException {
       // NOP
    }

    @Override
    public void removeSuspension(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId)
            throws RemoteException, InsufficientRequestException {
       // NOP
    }

    @Override
    public void createHostBackup(LoadBalancerEndpointConfiguration config, String backupName) throws RemoteException {
       // NOP
    }

    @Override
    public void deleteHostBackup(LoadBalancerEndpointConfiguration config, String backupName) throws RemoteException {
       // NOP
    }

    @Override
    public void restoreHostBackup(LoadBalancerEndpointConfiguration config, String backupName) throws RemoteException {
       // NOP
    }

    @Override
    public void setSubnetMappings(LoadBalancerEndpointConfiguration config, Hostssubnet hostssubnet) throws RemoteException {
       // NOP
    }

    @Override
    public void deleteSubnetMappings(LoadBalancerEndpointConfiguration config, Hostssubnet hostssubnet) throws RemoteException {
       // NOP
    }

    @Override
    public Hostssubnet getSubnetMappings(LoadBalancerEndpointConfiguration config, String host) throws RemoteException {
        return null;
    }

    @Override
    public List<String> getStatsSystemLoadBalancerNames(LoadBalancerEndpointConfiguration config) throws RemoteException {
        return null;
    }

    @Override
    public Map<String, Integer> getLoadBalancerCurrentConnections(LoadBalancerEndpointConfiguration config, List<String> names)
            throws RemoteException {

        return null;
    }

    @Override
    public int getTotalCurrentConnectionsForHost(LoadBalancerEndpointConfiguration config) throws RemoteException {
        return 0;
    }

    @Override
    public Map<String, Long> getLoadBalancerBytesIn(LoadBalancerEndpointConfiguration config, List<String> names) throws RemoteException {

        return null;
    }

    @Override
    public Map<String, Long> getLoadBalancerBytesOut(LoadBalancerEndpointConfiguration config, List<String> names) throws RemoteException {

        return null;
    }

    @Override
    public Long getHostBytesIn(LoadBalancerEndpointConfiguration config) throws RemoteException {
        return null;
    }

    @Override
    public Long getHostBytesOut(LoadBalancerEndpointConfiguration config) throws RemoteException {
        return null;
    }

    @Override
    public boolean isEndPointWorking(LoadBalancerEndpointConfiguration config) throws RemoteException {
        return true; 
    }
}
