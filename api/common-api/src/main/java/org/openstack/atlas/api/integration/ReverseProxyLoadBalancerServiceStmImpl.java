package org.openstack.atlas.api.integration;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.adapter.helpers.IpHelper;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerStmAdapter;
import org.openstack.atlas.api.helpers.CacheKeyGen;
import org.openstack.atlas.api.helpers.DateHelpers;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.service.domain.cache.AtlasCache;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.Stats;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.service.domain.services.HealthMonitorService;
import org.openstack.atlas.service.domain.services.HostService;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.util.crypto.CryptoUtil;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.openstack.atlas.util.debug.Debug;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.List;

import static java.util.Calendar.getInstance;

public class ReverseProxyLoadBalancerServiceStmImpl implements ReverseProxyLoadBalancerStmService {

    final Log LOG = LogFactory.getLog(ReverseProxyLoadBalancerServiceStmImpl.class);
    private ReverseProxyLoadBalancerStmAdapter reverseProxyLoadBalancerStmAdapter;
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
    public void createLoadBalancer(LoadBalancer lb) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.createLoadBalancer(config, lb);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateLoadBalancer(LoadBalancer lb, LoadBalancer queLb) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.updateLoadBalancer(config, lb, queLb);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.deleteLoadBalancer(config, lb);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void setRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.setRateLimit(config, loadBalancer, rateLimit);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteRateLimit(LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.deleteRateLimit(config, loadBalancer);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.updateRateLimit(config, loadBalancer, rateLimit);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void changeHostForLoadBalancer(LoadBalancer lb, Host newHost) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration configOld = getConfigbyLoadBalancerId(lb.getId());
        LoadBalancerEndpointConfiguration configNew = getConfigHost(newHost);

        try {
            reverseProxyLoadBalancerStmAdapter.changeHostForLoadBalancer(configOld, configNew, lb);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(configOld, af);
            checkAndSetIfRestEndPointBad(configNew, af);
            throw af;
        }
    }

    @Override
    public void addVirtualIps(Integer lbId, Integer accountId, LoadBalancer loadBalancer) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerStmAdapter.updateVirtualIps(config, loadBalancer);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void setNodes(LoadBalancer lb) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.setNodes(config, lb);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeNode(LoadBalancer lb, Node node) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.removeNode(config, lb, node);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeNodes(LoadBalancer lb, List<Node> nodesToRemove) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.removeNodes(config, lb, nodesToRemove);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateAccessList(LoadBalancer loadBalancer) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.updateAccessList(config, loadBalancer);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;

        }
    }

    @Override
    public void deleteAccessList(LoadBalancer lb, List<Integer> accessListToDelete) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.deleteAccessList(config, lb, accessListToDelete);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateConnectionThrottle(LoadBalancer loadBalancer) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.updateConnectionThrottle(config, loadBalancer);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteConnectionThrottle(LoadBalancer loadBalancer) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.deleteConnectionThrottle(config, loadBalancer);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateHealthMonitor(LoadBalancer loadBalancer) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.updateHealthMonitor(config, loadBalancer);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeHealthMonitor(LoadBalancer loadBalancer) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.deleteHealthMonitor(config, loadBalancer);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void suspendLoadBalancer(LoadBalancer lb) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.addSuspension(config, lb);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeSuspension(LoadBalancer lb) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.removeSuspension(config, lb);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteVirtualIps(LoadBalancer lb, List<Integer> ids, UserPages up) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.deleteVirtualIps(config, lb, ids, up);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }

    }

    @Override
    public boolean isEndPointWorking(Host host) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        boolean out;
        LoadBalancerEndpointConfiguration hostConfig = getConfigHost(host);
        out = reverseProxyLoadBalancerStmAdapter.isEndPointWorking(hostConfig);
        return out;
    }

    //Deprecated

//    @Override
//    public Hostssubnet getSubnetMappings(Host host) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
//        LoadBalancerEndpointConfiguration hostConfig = getConfig(host);
//        String hostName = host.getTrafficManagerName();
//        Hostssubnet hostssubnet;
//        try {
//            hostssubnet = reverseProxyLoadBalancerStmAdapter.getSubnetMappings(getConfig(host), hostName);
//        } catch (RollBackException af) {
//            checkAndSetIfSoapEndPointBad(hostConfig, af);
//            throw af;
//        }
//        return hostssubnet;
//    }
//
//    @Override
//    public void setSubnetMappings(Host host, Hostssubnet hostssubnet) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
//        LoadBalancerEndpointConfiguration hostConfig = getConfig(host);
//        String hostName = host.getTrafficManagerName();
//        try {
//            reverseProxyLoadBalancerStmAdapter.setSubnetMappings(hostConfig, hostssubnet);
//        } catch (RollBackException af) {
//            checkAndSetIfSoapEndPointBad(hostConfig, af);
//            throw af;
//        }
//    }
//
//    @Override
//    public void deleteSubnetMappings(Host host, Hostssubnet hostssubnet) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
//        LoadBalancerEndpointConfiguration hostConfig = getConfig(host);
//        String hostName = host.getTrafficManagerName();
//        try {
//            reverseProxyLoadBalancerStmAdapter.deleteSubnetMappings(hostConfig, hostssubnet);
//        } catch (RollBackException af) {
//            checkAndSetIfSoapEndPointBad(hostConfig, af);
//            throw af;
//        }
//    }

    @Override
    public void deleteErrorFile(LoadBalancer loadBalancer, UserPages up) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException, StmRollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.deleteErrorFile(config, loadBalancer, up);
        } catch (StmRollBackException ex) {
            checkAndSetIfRestEndPointBad(config, ex);
            throw ex;
        }
    }

    @Override
    public void setErrorFile(LoadBalancer loadBalancer, String content)
            throws InsufficientRequestException, StmRollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.setErrorFile(config, loadBalancer, content);
        } catch (StmRollBackException ex) {
            checkAndSetIfRestEndPointBad(config, ex);
            throw ex;
        }
    }

    @Override
    public void uploadDefaultErrorFile(Integer clusterId, String content) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException, RollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyClusterId(clusterId);
        try {
            reverseProxyLoadBalancerStmAdapter.uploadDefaultErrorFile(config, content);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    LoadBalancerEndpointConfiguration getConfigbyClusterId(Integer clusterId) throws EntityNotFoundException, DecryptException {
        Cluster cluster = hostService.getClusterById(clusterId);
        Host soapEndpointHost = hostService.getRestEndPointHost(cluster.getId());
        List<String> failoverHostNames = hostService.getFailoverHostNames(cluster.getId());
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);
        List<Host> failoverHosts = hostService.getFailoverHosts(cluster.getId());

        return new LoadBalancerEndpointConfiguration(soapEndpointHost, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), soapEndpointHost, failoverHostNames, logFileLocation, failoverHosts);
    }

    // Send request to proper SOAPEndpoint(Calculated by the database) for host's traffic manager
    @Override
    public LoadBalancerEndpointConfiguration getConfig(Host host) throws DecryptException, MalformedURLException {
        Cluster cluster = host.getCluster();
        Host soapEndpointHost = hostService.getRestEndPointHost(cluster.getId());
        List<String> failoverHostNames = hostService.getFailoverHostNames(cluster.getId());
        List<Host> failoverHosts = hostService.getFailoverHosts(cluster.getId());
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);

        return new LoadBalancerEndpointConfiguration(soapEndpointHost, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHostNames, logFileLocation, failoverHosts);
    }

    // Send SOAP request directly to the hosts traffic manager.
    @Override
    public LoadBalancerEndpointConfiguration getConfigHost(Host host) throws DecryptException, MalformedURLException {
        Cluster cluster = host.getCluster();
        List<String> failoverHostNames = hostService.getFailoverHostNames(cluster.getId());
        List<Host> failoverHosts = hostService.getFailoverHosts(cluster.getId());
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);

        return new LoadBalancerEndpointConfiguration(host, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHostNames, logFileLocation, failoverHosts);
    }

    private LoadBalancerEndpointConfiguration getConfigbyLoadBalancerId(Integer lbId) throws EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancer loadBalancer = loadBalancerService.get(lbId);
        Host host = loadBalancer.getHost();
        Cluster cluster = host.getCluster();
        Host soapEndpointHost = hostService.getRestEndPointHost(cluster.getId());
        List<String> failoverHostNames = hostService.getFailoverHostNames(cluster.getId());
        List<Host> failoverHosts = hostService.getFailoverHosts(cluster.getId());
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);
        return new LoadBalancerEndpointConfiguration(soapEndpointHost, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHostNames, logFileLocation, failoverHosts);
    }

    public void setReverseProxyLoadBalancerStmAdapter(ReverseProxyLoadBalancerStmAdapter reverseProxyLoadBalancerStmAdapter) {
        this.reverseProxyLoadBalancerStmAdapter = reverseProxyLoadBalancerStmAdapter;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    private void checkAndSetIfRestEndPointBad(LoadBalancerEndpointConfiguration config, StmRollBackException ex) {
        Host configuredHost = config.getEndpointUrlHost();
        if (IpHelper.isNetworkConnectionException(ex)) {
            LOG.error(String.format("STM endpoint %s went bad marking host[%d] as bad. Exception was %s", configuredHost.getEndpoint(), configuredHost.getId(), Debug.getExtendedStackTrace(ex)));
            configuredHost.setRestEndpointActive(Boolean.FALSE);
            hostService.update(configuredHost);
        } else {
            LOG.warn(String.format("STM endpoint %s on host[%d] throw an STM Fault but not marking as bad as it was not a network connection error: Exception was %s", configuredHost.getEndpoint(), configuredHost.getId(), Debug.getExtendedStackTrace(ex)));
        }
    }

    private void checkAndSetIfRestEndPointBad(LoadBalancerEndpointConfiguration config, RollBackException af) {
        Host configuredHost = config.getEndpointUrlHost();
        if (IpHelper.isNetworkConnectionException(af)) {
            LOG.error(String.format("SOAP endpoint %s went bad marking host[%d] as bad. Exception was %s", configuredHost.getEndpoint(), configuredHost.getId(), Debug.getExtendedStackTrace(af)));
            configuredHost.setRestEndpointActive(Boolean.FALSE);
            hostService.update(configuredHost);
        }
        LOG.warn(String.format("SOAP endpoint %s on host[%d] throw an RollBackException but not marking as bad as it was not a network connection error: Exception was %s", configuredHost.getEndpoint(), configuredHost.getId(), Debug.getExtendedStackTrace(af)));
    }

    @Override
    public void updateSslTermination(LoadBalancer loadBalancer, ZeusSslTermination sslTermination, UserPages up) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.updateSslTermination(config, loadBalancer, sslTermination);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeSslTermination(LoadBalancer lb) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.removeSslTermination(config, lb);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public Stats getVirtualServerStats(LoadBalancer loadBalancer) throws EntityNotFoundException, MalformedURLException, DecryptException, InsufficientRequestException, StingrayRestClientObjectNotFoundException, StingrayRestClientException {
        Integer accountId = loadBalancer.getAccountId();
        Integer loadbalancerId = loadBalancer.getId();
        LoadBalancerEndpointConfiguration config = getConfigHost(loadBalancerService.get(loadbalancerId).getHost());
        String key = CacheKeyGen.generateKeyName(accountId, loadbalancerId);
        Stats stats;

        long cal = getInstance().getTimeInMillis();
        stats = (Stats) atlasCache.get(key);
        if (stats == null) {
            stats = reverseProxyLoadBalancerStmAdapter.getVirtualServerStats(config, loadBalancer);
            LOG.info("Date:" + DateHelpers.getDate(Calendar.getInstance().getTime()) + " AccountID: " + accountId + " GetLoadBalancerStats, Missed from cache, retrieved from api... Time taken: " + DateHelpers.getTotalTimeTaken(cal) + " ms");
            atlasCache.set(key, stats);
        } else {
            LOG.info("Date:" + DateHelpers.getDate(Calendar.getInstance().getTime()) + " AccountID: " + accountId + " GetLoadBalancerStats, retrieved from cache... Time taken: " + DateHelpers.getTotalTimeTaken(cal) + " ms");
            return stats;
        }
        return stats;
    }

    public void setAtlasCache(AtlasCache atlasCache) {
        this.atlasCache = atlasCache;
    }

    public AtlasCache getAtlasCache() {
        return atlasCache;
    }
}
