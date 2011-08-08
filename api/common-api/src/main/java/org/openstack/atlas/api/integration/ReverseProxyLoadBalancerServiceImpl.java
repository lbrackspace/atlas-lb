package org.openstack.atlas.api.integration;

import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.AdapterRollBackException;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.services.HealthMonitorService;
import org.openstack.atlas.service.domain.services.HostService;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.util.crypto.CryptoUtil;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.openstack.atlas.adapter.exceptions.ObjectNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ReverseProxyLoadBalancerServiceImpl implements ReverseProxyLoadBalancerService {

    final Log LOG = LogFactory.getLog(ReverseProxyLoadBalancerServiceImpl.class);

    private ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter;

    private LoadBalancerService loadBalancerService;

    private HostService hostService;

    private NotificationService notificationService;

    private HealthMonitorService healthMonitorService;

    private Configuration configuration;

    public void setLoadBalancerService(LoadBalancerService loadBalancerService) {
        this.loadBalancerService = loadBalancerService;
    }

    public void setHostService(HostService hostService) {
        this.hostService = hostService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setHealthMonitorService(HealthMonitorService healthMonitorService) {
        this.healthMonitorService = healthMonitorService;
    }

    @Override
    public void createLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, AdapterRollBackException, EntityNotFoundException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.createLoadBalancer(config, lb);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void deleteLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, AdapterRollBackException, EntityNotFoundException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.deleteLoadBalancer(config, lb);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }


    @Override
    public void setRateLimit(int id, int accountId, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, AdapterRollBackException, EntityNotFoundException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(id);
        try {
            reverseProxyLoadBalancerAdapter.setRateLimit(config, id, accountId, rateLimit);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void deleteRateLimit(int id, int accountId) throws RemoteException, InsufficientRequestException, AdapterRollBackException, EntityNotFoundException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(id);
        try {
            reverseProxyLoadBalancerAdapter.deleteRateLimit(config, id, accountId);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void updateRateLimit(int id, int accountId, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, AdapterRollBackException, EntityNotFoundException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(id);
        try {
            reverseProxyLoadBalancerAdapter.updateRateLimit(config, id, accountId, rateLimit);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void updateAlgorithm(LoadBalancer lb) throws RemoteException, InsufficientRequestException, AdapterRollBackException, EntityNotFoundException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.setLoadBalancingAlgorithm(config, lb.getId(), lb.getAccountId(), lb.getAlgorithm());
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void changeHostForLoadBalancer(LoadBalancer lb, Host newHost) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.changeHostForLoadBalancer(config, lb, newHost);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void updatePort(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.updatePort(config, lb.getId(), lb.getAccountId(),
                    lb.getPort());
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void updateProtocol(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateProtocol(config, lb.getId(), lb.getAccountId(),
                    lb.getProtocol());
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void updateConnectionLogging(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateConnectionLogging(config, lb.getId(), lb.getAccountId(), lb.isConnectionLogging(), lb.getProtocol());
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void addVirtualIps(Integer lbId, Integer accountId, LoadBalancer loadBalancer) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.addVirtualIps(config, loadBalancer);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void setNodes(Integer lbId, Integer accountId, Set<Node> nodes) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.setNodes(config, lbId, accountId, nodes);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void removeNode(Integer lbId, Integer accountId, Node node) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.removeNode(config, lbId, accountId, node.getIpAddress(), node.getPort());
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }


    @Override
    public void removeNodes(Integer lbId, Integer accountId,Collection<Node> nodes) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.removeNodes(config, lbId, accountId,nodes);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }


    @Override
    public void setNodeWeights(Integer lbId, Integer accountId, Set<Node> nodes) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.setNodeWeights(config, lbId, accountId, nodes);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }

    }

    @Override
    public void updateAccessList(Integer lbId, Integer accountId, Collection<AccessList> accessListItems) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.updateAccessList(config, lbId, accountId, accessListItems);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;

        }
    }

    @Override
    public void deleteAccessList(Integer lbId, Integer accountId) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.deleteAccessList(config, lbId, accountId);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void updateConnectionThrottle(Integer lbId, Integer accountId,
                                         ConnectionLimit connectionThrottle) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.updateConnectionThrottle(config, lbId, accountId,
                    connectionThrottle);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void deleteConnectionThrottle(Integer lbId, Integer accountId) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.deleteConnectionThrottle(config, lbId, accountId);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }


    @Override
    public void updateSessionPersistence(Integer lbId, Integer accountId, SessionPersistence persistenceMode) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.setSessionPersistence(config, lbId, accountId, persistenceMode);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void removeSessionPersistence(Integer lbId, Integer accountId) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.removeSessionPersistence(config, lbId, accountId);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void updateHealthMonitor(Integer lbId, Integer accountId, HealthMonitor monitor) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.updateHealthMonitor(config, lbId, accountId, monitor);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void removeHealthMonitor(Integer lbId, Integer accountId) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.removeHealthMonitor(config, lbId, accountId);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void createHostBackup(Host host,
                                 String backupName) throws RemoteException, MalformedURLException, DecryptException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigHost(host);
        try {
            reverseProxyLoadBalancerAdapter.createHostBackup(config, backupName);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void restoreHostBackup(Host host, String backupName) throws RemoteException, ObjectNotFoundException, MalformedURLException, DecryptException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigHost(host);
        try {
            reverseProxyLoadBalancerAdapter.restoreHostBackup(config, backupName);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void deleteHostBackup(Host host, String backupName) throws RemoteException, ObjectNotFoundException, MalformedURLException, DecryptException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigHost(host);
        try {
            reverseProxyLoadBalancerAdapter.deleteHostBackup(config, backupName);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void suspendLoadBalancer(Integer lbId, Integer accountId) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.suspendLoadBalancer(config, lbId, accountId);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void removeSuspension(Integer id, Integer accountId) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(id);
        try {
            reverseProxyLoadBalancerAdapter.removeSuspension(config, id, accountId);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public int getTotalCurrentConnectionsForHost(Host host) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigHost(host);
        int conn;
        try {
            conn = reverseProxyLoadBalancerAdapter.getTotalCurrentConnectionsForHost(config);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
        return conn;
    }

    @Override
    public void deleteVirtualIp(LoadBalancer lb, Integer id) {
        try {
            LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
            try {
                reverseProxyLoadBalancerAdapter.deleteVirtualIp(config, lb, id);
            } catch (Exception exc) {
                checkAndSetIfEndPointBad(config, exc);
                throw exc;
            }
        } catch (Exception e) {
            LOG.error("Error during removal of the virtualIp:", e);
        }
    }

    @Override
    public void deleteVirtualIps(LoadBalancer lb, List<Integer> ids) {
        try {
            LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
            try {
                reverseProxyLoadBalancerAdapter.deleteVirtualIps(config, lb, ids);
            } catch (Exception exc) {
                checkAndSetIfEndPointBad(config, exc);
                throw exc;
            }
        } catch (Exception e) {
            LOG.error("Error during removal of the virtualIp:", e);
        }
    }

    @Override
    public boolean isEndPointWorking(Host host) throws Exception {
        boolean out;
        LoadBalancerEndpointConfiguration hostConfig = getConfigHost(host);
        out = reverseProxyLoadBalancerAdapter.isEndPointWorking(hostConfig);
        return out;
    }

    @Override
    public Hostssubnet getSubnetMappings(Host host) throws Exception {
        LoadBalancerEndpointConfiguration hostConfig = getConfig(host);
        String hostName = host.getHostName();
        Hostssubnet hostssubnet;
        try {
            hostssubnet = reverseProxyLoadBalancerAdapter.getSubnetMappings(getConfig(host), hostName);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(hostConfig, exc);
            throw exc;
        }
        return hostssubnet;
    }

    @Override
    public void setSubnetMappings(Host host, Hostssubnet hostssubnet) throws Exception {
        LoadBalancerEndpointConfiguration hostConfig = getConfig(host);
        String hostName = host.getHostName();
        try {
            reverseProxyLoadBalancerAdapter.setSubnetMappings(hostConfig, hostssubnet);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(hostConfig, exc);
            throw exc;
        }
    }

    @Override
    public void deleteSubnetMappings(Host host, Hostssubnet hostssubnet) throws Exception {
        LoadBalancerEndpointConfiguration hostConfig = getConfig(host);
        String hostName = host.getHostName();
        try {
            reverseProxyLoadBalancerAdapter.deleteSubnetMappings(hostConfig, hostssubnet);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(hostConfig, exc);
            throw exc;
        }
    }

    @Override
    public LoadBalancerEndpointConfiguration getConfig(Host host) throws DecryptException, MalformedURLException {
        Cluster cluster = host.getCluster();
        Host endpointHost = hostService.getEndPointHost(cluster.getId());
        List<String> failoverHosts = hostService.getFailoverHostNames(cluster.getId());
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);
        return new LoadBalancerEndpointConfiguration(endpointHost, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHosts, logFileLocation);
    }

    @Override
    public LoadBalancerEndpointConfiguration getConfigHost(Host host) throws DecryptException, MalformedURLException {
        Cluster cluster = host.getCluster();
        List<String> failoverHosts = hostService.getFailoverHostNames(cluster.getId());
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);
        return new LoadBalancerEndpointConfiguration(host, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHosts, logFileLocation);
    }

    private LoadBalancerEndpointConfiguration getConfigbyLoadBalancerId(Integer lbId) throws EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancer loadBalancer = loadBalancerService.get(lbId);
        Host host = loadBalancer.getHost();
        Cluster cluster = host.getCluster();
        Host endpointHost = hostService.getEndPointHost(cluster.getId());
        List<String> failoverHosts = hostService.getFailoverHostNames(cluster.getId());
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);
        return new LoadBalancerEndpointConfiguration(endpointHost, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHosts, logFileLocation);
    }

    public void setReverseProxyLoadBalancerAdapter(ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter) {
        this.reverseProxyLoadBalancerAdapter = reverseProxyLoadBalancerAdapter;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    private boolean isConnectionExcept(Exception exc) {
        String faultString = exc.getMessage();
        if (faultString == null) {
            return false;
        }
        if (faultString.split(":")[0].equals("java.net.ConnectException")) {
            return true;
        }
        return false;
    }

    private void checkAndSetIfEndPointBad(LoadBalancerEndpointConfiguration config, Exception exc) throws Exception {
        Host badHost = config.getHost();
        if (isConnectionExcept(exc)) {
            LOG.error(String.format("Endpoint %s went bad marking host[%d] as bad.", badHost.getEndpoint(), badHost.getId()));
            badHost.setEndpointActive(Boolean.FALSE);
            hostService.update(badHost);
        }
    }
}
