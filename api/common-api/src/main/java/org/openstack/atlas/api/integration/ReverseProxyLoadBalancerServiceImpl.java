package org.openstack.atlas.api.integration;

import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.adapter.helpers.IpHelper;
import org.openstack.atlas.util.debug.Debug;
import java.net.SocketException;
import org.openstack.atlas.service.domain.cache.AtlasCache;
import org.openstack.atlas.api.helpers.CacheKeyGen;
import org.openstack.atlas.api.helpers.DateHelpers;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.ZxtmRollBackException;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.pojos.Stats;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.service.domain.services.HealthMonitorService;
import org.openstack.atlas.service.domain.services.HostService;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.util.crypto.CryptoUtil;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import com.zxtm.service.client.ObjectDoesNotExist;
import org.apache.axis.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.util.*;

import static java.util.Calendar.getInstance;

public class ReverseProxyLoadBalancerServiceImpl implements ReverseProxyLoadBalancerService {

    final Log LOG = LogFactory.getLog(ReverseProxyLoadBalancerServiceImpl.class);
    private ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter;
    private LoadBalancerService loadBalancerService;
    private HostService hostService;
    private NotificationService notificationService;
    private HealthMonitorService healthMonitorService;
    private Configuration configuration;
    private AtlasCache atlasCache;

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
    public void createLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.createLoadBalancer(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.deleteLoadBalancer(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void setRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.setRateLimit(config, loadBalancer, rateLimit);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteRateLimit(LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.deleteRateLimit(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateRateLimit(config, loadBalancer, rateLimit);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateAlgorithm(LoadBalancer lb) throws RemoteException, InsufficientRequestException, ZxtmRollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.setLoadBalancingAlgorithm(config, lb.getId(), lb.getAccountId(), lb.getAlgorithm());
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void changeHostForLoadBalancer(LoadBalancer lb, Host newHost) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.changeHostForLoadBalancer(config, lb, newHost);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updatePort(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.updatePort(config, lb.getId(), lb.getAccountId(),
                    lb.getPort());
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateTimeout(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateTimeout(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateProtocol(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateProtocol(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateHalfClosed(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateHalfClosed(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    public void updateHttpsRedirect(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateHttpsRedirect(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateConnectionLogging(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateConnectionLogging(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateContentCaching(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateContentCaching(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void addVirtualIps(Integer lbId, Integer accountId, LoadBalancer loadBalancer) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.addVirtualIps(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void setNodes(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.setNodes(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeNode(Integer lbId, Integer accountId, Node node) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.removeNode(config, lbId, accountId, node.getIpAddress(), node.getPort());
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeNodes(Integer lbId, Integer accountId, Collection<Node> nodes) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.removeNodes(config, lbId, accountId, nodes);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void setNodeWeights(Integer lbId, Integer accountId, Set<Node> nodes) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.setNodeWeights(config, lbId, accountId, nodes);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }

    }

    @Override
    public void updateAccessList(LoadBalancer loadBalancer) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateAccessList(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;

        }
    }

    @Override
    public void deleteAccessList(Integer lbId, Integer accountId) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.deleteAccessList(config, lbId, accountId);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateConnectionThrottle(LoadBalancer loadBalancer) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateConnectionThrottle(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteConnectionThrottle(LoadBalancer loadBalancer) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.deleteConnectionThrottle(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateSessionPersistence(Integer lbId, Integer accountId, SessionPersistence persistenceMode) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.setSessionPersistence(config, lbId, accountId, persistenceMode);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeSessionPersistence(Integer lbId, Integer accountId) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.removeSessionPersistence(config, lbId, accountId);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateHealthMonitor(Integer lbId, Integer accountId, HealthMonitor monitor) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerAdapter.updateHealthMonitor(config, lbId, accountId, monitor);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeHealthMonitor(LoadBalancer loadBalancer) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.removeHealthMonitor(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void createHostBackup(Host host,
            String backupName) throws RemoteException, MalformedURLException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigHost(host);
        try {
            reverseProxyLoadBalancerAdapter.createHostBackup(config, backupName);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void restoreHostBackup(Host host, String backupName) throws RemoteException, ObjectDoesNotExist, MalformedURLException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigHost(host);
        try {
            reverseProxyLoadBalancerAdapter.restoreHostBackup(config, backupName);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteHostBackup(Host host, String backupName) throws RemoteException, ObjectDoesNotExist, MalformedURLException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigHost(host);
        try {
            reverseProxyLoadBalancerAdapter.deleteHostBackup(config, backupName);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void suspendLoadBalancer(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.suspendLoadBalancer(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeSuspension(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.removeSuspension(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public int getTotalCurrentConnectionsForHost(Host host) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigHost(host);
        int conn;
        try {
            conn = reverseProxyLoadBalancerAdapter.getTotalCurrentConnectionsForHost(config);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
        return conn;
    }

    @Override
    public Integer getLoadBalancerCurrentConnections(LoadBalancer lb, boolean isSsl) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigHost(lb.getHost());
        int conn;
        try {
            conn = reverseProxyLoadBalancerAdapter.getLoadBalancerCurrentConnections(config, lb.getAccountId(), lb.getId(), isSsl);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
        return conn;
    }

    @Override
    public Long getLoadBalancerBytesIn(LoadBalancer lb, boolean isSsl) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigHost(lb.getHost());
        long bytesIn;
        try {
            bytesIn = reverseProxyLoadBalancerAdapter.getLoadBalancerBytesIn(config, lb.getAccountId(), lb.getId(), isSsl);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
        return bytesIn;
    }

    @Override
    public Long getLoadBalancerBytesOut(LoadBalancer lb, boolean isSsl) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigHost(lb.getHost());
        long bytesOut;
        try {
            bytesOut = reverseProxyLoadBalancerAdapter.getLoadBalancerBytesOut(config, lb.getAccountId(), lb.getId(), isSsl);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
        return bytesOut;
    }

    @Override
    public Stats getLoadBalancerStats(Integer loadbalancerId, Integer accountId) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigHost(loadBalancerService.get(loadbalancerId).getHost());
        String key = CacheKeyGen.generateKeyName(accountId, loadbalancerId);
        Stats lbStats;

        long cal = getInstance().getTimeInMillis();
        lbStats = (Stats) atlasCache.get(key);
        if (lbStats == null) {
            try {
                lbStats = reverseProxyLoadBalancerAdapter.getLoadBalancerStats(config, loadbalancerId, accountId);
                LOG.info("Date:" + DateHelpers.getDate(Calendar.getInstance().getTime()) + " AccountID: " + accountId + " GetLoadBalancerStats, Missed from cache, retrieved from api... Time taken: " + DateHelpers.getTotalTimeTaken(cal) + " ms");
                atlasCache.set(key, lbStats);
            } catch (AxisFault af) {
                checkAndSetIfSoapEndPointBad(config, af);
                throw af;
            }
        } else {
            LOG.info("Date:" + DateHelpers.getDate(Calendar.getInstance().getTime()) + " AccountID: " + accountId + " GetLoadBalancerStats, retrieved from cache... Time taken: " + DateHelpers.getTotalTimeTaken(cal) + " ms");
            return lbStats;
        }
        return lbStats;
    }

    @Override
    public void deleteVirtualIp(LoadBalancer lb, Integer id) {
        try {
            LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
            try {
                reverseProxyLoadBalancerAdapter.deleteVirtualIp(config, lb, id);
            } catch (AxisFault af) {
                checkAndSetIfSoapEndPointBad(config, af);
                throw af;
            }
        } catch (Exception e) {
            LOG.error("Error during removal of the virtualIp:", e);
        }
    }

    @Override
    public void setErrorFile(LoadBalancer loadBalancer, String content) throws DecryptException, MalformedURLException, RemoteException, EntityNotFoundException, InsufficientRequestException {
        LoadBalancer lb = loadBalancerService.get(loadBalancer.getId(), loadBalancer.getAccountId());
        Host host = lb.getHost();
        LoadBalancerEndpointConfiguration config = getConfig(host);
        try {
            reverseProxyLoadBalancerAdapter.setErrorFile(config, loadBalancer, content);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteVirtualIps(LoadBalancer lb, List<Integer> ids) {
        try {
            LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
            try {
                reverseProxyLoadBalancerAdapter.deleteVirtualIps(config, lb, ids);
            } catch (AxisFault af) {
                checkAndSetIfSoapEndPointBad(config, af);
                throw af;
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
        String hostName = host.getTrafficManagerName();
        Hostssubnet hostssubnet;
        try {
            hostssubnet = reverseProxyLoadBalancerAdapter.getSubnetMappings(getConfig(host), hostName);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(hostConfig, af);
            throw af;
        }
        return hostssubnet;
    }

    @Override
    public void setSubnetMappings(Host host, Hostssubnet hostssubnet) throws Exception {
        LoadBalancerEndpointConfiguration hostConfig = getConfig(host);
        String hostName = host.getTrafficManagerName();
        try {
            reverseProxyLoadBalancerAdapter.setSubnetMappings(hostConfig, hostssubnet);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(hostConfig, af);
            throw af;
        }
    }

    @Override
    public void deleteSubnetMappings(Host host, Hostssubnet hostssubnet) throws Exception {
        LoadBalancerEndpointConfiguration hostConfig = getConfig(host);
        String hostName = host.getTrafficManagerName();
        try {
            reverseProxyLoadBalancerAdapter.deleteSubnetMappings(hostConfig, hostssubnet);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(hostConfig, af);
            throw af;
        }
    }

    @Override
    public void removeAndSetDefaultErrorFile(LoadBalancer loadBalancer) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.removeAndSetDefaultErrorFile(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void setDefaultErrorFile(LoadBalancer loadBalancer) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException {
        LoadBalancer lb = loadBalancerService.get(loadBalancer.getId(), loadBalancer.getAccountId());
        LoadBalancerEndpointConfiguration config = getConfig(lb.getHost());
        try {
            reverseProxyLoadBalancerAdapter.setDefaultErrorFile(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void uploadDefaultErrorFile(Integer clusterId, String content) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException {
        LoadBalancerEndpointConfiguration config = getConfigbyClusterId(clusterId);
        try {
            reverseProxyLoadBalancerAdapter.uploadDefaultErrorFile(config, content);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteErrorFile(LoadBalancer loadBalancer) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException {
        LoadBalancer lb = loadBalancerService.get(loadBalancer.getId(), loadBalancer.getAccountId());
        LoadBalancerEndpointConfiguration config = getConfig(lb.getHost());
        try {
            reverseProxyLoadBalancerAdapter.deleteErrorFile(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    LoadBalancerEndpointConfiguration getConfigbyClusterId(Integer clusterId) throws EntityNotFoundException, DecryptException {
        Cluster cluster = hostService.getClusterById(clusterId);
        Host soapEndpointHost = hostService.getEndPointHost(cluster.getId());
        List<String> failoverHosts = hostService.getFailoverHostNames(cluster.getId());
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);
        return new LoadBalancerEndpointConfiguration(soapEndpointHost, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), soapEndpointHost, failoverHosts, logFileLocation);
    }

    // Send request to proper SOAPEndpoint(Calculated by the database) for host's traffic manager
    @Override
    public LoadBalancerEndpointConfiguration getConfig(Host host) throws DecryptException, MalformedURLException {
        Cluster cluster = host.getCluster();
        Host soapEndpointHost = hostService.getEndPointHost(cluster.getId());
        List<String> failoverHosts = hostService.getFailoverHostNames(cluster.getId());
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);
        return new LoadBalancerEndpointConfiguration(soapEndpointHost, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHosts, logFileLocation);
    }

    // Send SOAP request directly to the hosts traffic manager.
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
        Host soapEndpointHost = hostService.getEndPointHost(cluster.getId());
        List<String> failoverHosts = hostService.getFailoverHostNames(cluster.getId());
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);
        return new LoadBalancerEndpointConfiguration(soapEndpointHost, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHosts, logFileLocation);
    }

    public void setReverseProxyLoadBalancerAdapter(ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter) {
        this.reverseProxyLoadBalancerAdapter = reverseProxyLoadBalancerAdapter;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    private void checkAndSetIfSoapEndPointBad(LoadBalancerEndpointConfiguration config, AxisFault af) throws AxisFault {
        Host configuredHost = config.getEndpointUrlHost();
        if (IpHelper.isNetworkConnectionException(af)) {
            LOG.error(String.format("SOAP endpoint %s went bad marking host[%d] as bad. Exception was %s", configuredHost.getEndpoint(), configuredHost.getId(), Debug.getExtendedStackTrace(af)));
            configuredHost.setSoapEndpointActive(Boolean.FALSE);
            hostService.update(configuredHost);
        }
        LOG.warn(String.format("SOAP endpoint %s on host[%d] throw an AxisFault but not marking as bad as it was not a network connection error: Exception was %s", configuredHost.getEndpoint(), configuredHost.getId(), Debug.getExtendedStackTrace(af)));
    }

    @Override
    public void updateSslTermination(LoadBalancer loadBalancer, ZeusSslTermination sslTermination) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, ZxtmRollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.updateSslTermination(config, loadBalancer, sslTermination);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeSslTermination(LoadBalancer lb) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, ZxtmRollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.removeSslTermination(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void enableDisableSslTermination(LoadBalancer loadBalancer, boolean isSslTermination) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, ZxtmRollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerAdapter.enableDisableSslTermination(config, loadBalancer, isSslTermination);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }

    }

    @Override
    public void setNodesPriorities(String poolName, LoadBalancer lb) throws DecryptException, EntityNotFoundException, MalformedURLException, RemoteException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerAdapter.setNodesPriorities(config, poolName, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }

    }

    public void setAtlasCache(AtlasCache atlasCache) {
        this.atlasCache = atlasCache;
    }

    public AtlasCache getAtlasCache() {
        return atlasCache;
    }
}
