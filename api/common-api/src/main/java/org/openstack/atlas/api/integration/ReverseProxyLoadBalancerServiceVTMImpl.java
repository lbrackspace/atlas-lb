package org.openstack.atlas.api.integration;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.exceptions.VTMRollBackException;
import org.openstack.atlas.adapter.helpers.IpHelper;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerVTMAdapter;
import org.openstack.atlas.api.helpers.CacheKeyGen;
import org.openstack.atlas.api.helpers.DateHelpers;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.service.domain.cache.AtlasCache;
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
import org.openstack.atlas.util.debug.Debug;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.springframework.stereotype.Component;


import java.net.MalformedURLException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.*;

import static java.util.Calendar.getInstance;

@Component
public class ReverseProxyLoadBalancerServiceVTMImpl implements ReverseProxyLoadBalancerVTMService {

    final Log LOG = LogFactory.getLog(ReverseProxyLoadBalancerServiceVTMImpl.class);
    private ReverseProxyLoadBalancerVTMAdapter reverseProxyLoadBalancerVTMAdapter;
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
    public void createLoadBalancer(LoadBalancer lb) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.createLoadBalancer(config, lb);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateLoadBalancer(LoadBalancer lb, LoadBalancer queLb) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.updateLoadBalancer(config, lb, queLb);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteLoadBalancer(LoadBalancer lb) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.deleteLoadBalancer(config, lb);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void setRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.setRateLimit(config, loadBalancer, rateLimit);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteRateLimit(LoadBalancer loadBalancer) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.deleteRateLimit(config, loadBalancer);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.updateRateLimit(config, loadBalancer, rateLimit);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void changeHostForLoadBalancers(List<LoadBalancer> lbs, Host newHost) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException, RemoteException {
        // All LBs should be guaranteed to be on the same host, so get the old config based on the first one
        LoadBalancerEndpointConfiguration configOld = getConfigbyLoadBalancerId(lbs.get(0).getId());
        LoadBalancerEndpointConfiguration configNew = getConfigHost(newHost);
        int retryCount;
        try{
            retryCount = Integer.parseInt(configuration.getString(PublicApiServiceConfigurationKeys.rest_api_retries));
        } catch (Exception e) { // NumberFormatException or ConfigurationInitializationException
            retryCount = 5;
        }

        try {
            reverseProxyLoadBalancerVTMAdapter.changeHostForLoadBalancers(configOld, configNew, lbs, retryCount);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(configOld, af);
            checkAndSetIfRestEndPointBad(configNew, af);
            throw af;
        }
    }

    @Override
    public void addVirtualIps(Integer lbId, Integer accountId, LoadBalancer loadBalancer) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerVTMAdapter.updateVirtualIps(config, loadBalancer);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void setNodes(LoadBalancer lb) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.setNodes(config, lb);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeNode(LoadBalancer lb, Node node) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.removeNode(config, lb, node);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeNodes(LoadBalancer lb, List<Node> nodesToRemove) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.removeNodes(config, lb, nodesToRemove);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateAccessList(LoadBalancer loadBalancer) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.updateAccessList(config, loadBalancer);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;

        }
    }

    @Override
    public void deleteAccessList(LoadBalancer lb, List<Integer> accessListToDelete) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.deleteAccessList(config, lb, accessListToDelete);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateConnectionThrottle(LoadBalancer loadBalancer) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.updateConnectionThrottle(config, loadBalancer);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteConnectionThrottle(LoadBalancer loadBalancer) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.deleteConnectionThrottle(config, loadBalancer);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateHealthMonitor(LoadBalancer loadBalancer) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.updateHealthMonitor(config, loadBalancer);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeHealthMonitor(LoadBalancer loadBalancer) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.deleteHealthMonitor(config, loadBalancer);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateSessionPersistence(LoadBalancer loadBalancer, LoadBalancer quelb) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.updateSessionPersistence(config, loadBalancer, quelb);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeSessionPersistence(LoadBalancer loadBalancer, LoadBalancer quelb) throws VTMRollBackException, EntityNotFoundException, DecryptException, InsufficientRequestException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.deleteSessionPersistence(config, loadBalancer, quelb);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void suspendLoadBalancer(LoadBalancer lb) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.addSuspension(config, lb);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeSuspension(LoadBalancer lb) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.removeSuspension(config, lb);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteVirtualIps(LoadBalancer lb, List<Integer> ids, UserPages up) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.deleteVirtualIps(config, lb, ids);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }

    }

    @Override
    public boolean isEndPointWorking(Host host) throws RollBackException, MalformedURLException, DecryptException {
        boolean out;
        LoadBalancerEndpointConfiguration hostConfig = getConfigHost(host);
        out = reverseProxyLoadBalancerVTMAdapter.isEndPointWorking(hostConfig);
        return out;
    }

    // Host subnet mappings
    @Override
    public Hostssubnet getSubnetMappings(Host host) throws RollBackException, MalformedURLException, DecryptException {
        LoadBalancerEndpointConfiguration hostConfig = getConfigHost(host);
        String hostName = host.getTrafficManagerName();
        Hostssubnet hostssubnet;
        try {
            hostssubnet = reverseProxyLoadBalancerVTMAdapter.getSubnetMappings(hostConfig, hostName);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(hostConfig, af);
            throw af;
        }
        return hostssubnet;
    }

    @Override
    public void setSubnetMappings(Host host, Hostssubnet hostssubnet) throws RollBackException, MalformedURLException, DecryptException {
        LoadBalancerEndpointConfiguration hostConfig = getConfigHost(host);
        try {
            reverseProxyLoadBalancerVTMAdapter.setSubnetMappings(hostConfig, hostssubnet);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(hostConfig, af);
            throw af;
        }
    }

    @Override
    public void deleteSubnetMappings(Host host, Hostssubnet hostssubnet) throws RollBackException, MalformedURLException, DecryptException {
        LoadBalancerEndpointConfiguration hostConfig = getConfigHost(host);
        try {
            reverseProxyLoadBalancerVTMAdapter.deleteSubnetMappings(hostConfig, hostssubnet);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(hostConfig, af);
            throw af;
        }
    }

    @Override
    public void deleteErrorFile(LoadBalancer loadBalancer, UserPages up) throws EntityNotFoundException, DecryptException, InsufficientRequestException, VTMRollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.deleteErrorFile(config, loadBalancer);
        } catch (VTMRollBackException ex) {
            checkAndSetIfRestEndPointBad(config, ex);
            throw ex;
        }
    }

    @Override
    public void setErrorFile(LoadBalancer loadBalancer, String content)
            throws InsufficientRequestException, VTMRollBackException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.setErrorFile(config, loadBalancer, content);
        } catch (VTMRollBackException ex) {
            checkAndSetIfRestEndPointBad(config, ex);
            throw ex;
        }
    }

    @Override
    public void uploadDefaultErrorFile(Integer clusterId, String content) throws EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyClusterId(clusterId);
        try {
            reverseProxyLoadBalancerVTMAdapter.uploadDefaultErrorFile(config, content);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    public void setReverseProxyLoadBalancerVTMAdapter(ReverseProxyLoadBalancerVTMAdapter reverseProxyLoadBalancerVTMAdapter) {
        this.reverseProxyLoadBalancerVTMAdapter = reverseProxyLoadBalancerVTMAdapter;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    private void checkAndSetIfRestEndPointBad(LoadBalancerEndpointConfiguration config, VTMRollBackException ex) {
        Host configuredHost = config.getEndpointUrlHost();
        if (IpHelper.isNetworkConnectionException(ex)) {
            LOG.error(String.format("REST endpoint %s went bad marking host[%d] as bad. Exception was %s", configuredHost.getRestEndpoint(), configuredHost.getId(), Debug.getExtendedStackTrace(ex)));
            configuredHost.setRestEndpointActive(Boolean.FALSE);
            hostService.update(configuredHost);
        } else {
            LOG.warn(String.format("REST endpoint %s on host[%d] throw an backend Fault but not marking as bad as it was not a network connection error: Exception was %s", configuredHost.getRestEndpoint(), configuredHost.getId(), Debug.getExtendedStackTrace(ex)));
        }
    }

    private void checkAndSetIfRestEndPointBad(LoadBalancerEndpointConfiguration config, RollBackException af) {
        Host configuredHost = config.getEndpointUrlHost();
        if (IpHelper.isNetworkConnectionException(af)) {
            LOG.error(String.format("REST endpoint %s went bad marking host[%d] as bad. Exception was %s", configuredHost.getRestEndpoint(), configuredHost.getId(), Debug.getExtendedStackTrace(af)));
            configuredHost.setRestEndpointActive(Boolean.FALSE);
            hostService.update(configuredHost);
        }
        LOG.warn(String.format("REST endpoint %s on host[%d] throw an RollBackException but not marking as bad as it was not a network connection error: Exception was %s", configuredHost.getRestEndpoint(), configuredHost.getId(), Debug.getExtendedStackTrace(af)));
    }

    @Override
    public void updateCertificateMappings(LoadBalancer loadBalancer) throws EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.updateCertificateMappings(config, loadBalancer);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateCertificateMapping(LoadBalancer loadBalancer, CertificateMapping certificateMapping) throws EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.updateCertificateMapping(config, loadBalancer, certificateMapping);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteCertificateMapping(LoadBalancer loadBalancer, CertificateMapping certificateMapping) throws EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.deleteCertificateMapping(config, loadBalancer, certificateMapping);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateSslTermination(LoadBalancer loadBalancer, ZeusSslTermination sslTermination) throws EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.updateSslTermination(config, loadBalancer, sslTermination);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeSslTermination(LoadBalancer lb) throws EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerVTMAdapter.removeSslTermination(config, lb);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public Stats getVirtualServerStats(LoadBalancer loadBalancer) throws EntityNotFoundException, MalformedURLException, DecryptException, InsufficientRequestException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        Integer accountId = loadBalancer.getAccountId();
        Integer loadbalancerId = loadBalancer.getId();
        LoadBalancerEndpointConfiguration config = getConfigHost(loadBalancerService.get(loadbalancerId).getHost());
        String key = CacheKeyGen.generateKeyName(accountId, loadbalancerId);
        Stats stats;

        long cal = getInstance().getTimeInMillis();
        stats = (Stats) atlasCache.get(key);
        if (stats == null) {
            stats = reverseProxyLoadBalancerVTMAdapter.getVirtualServerStats(config, loadBalancer);
            LOG.info("Date:" + DateHelpers.getDate(Calendar.getInstance().getTime()) + " AccountID: " + accountId + " GetLoadBalancerStats, Missed from cache, retrieved from api... Time taken: " + DateHelpers.getTotalTimeTaken(cal) + " ms");
            atlasCache.set(key, stats);
        } else {
            LOG.info("Date:" + DateHelpers.getDate(Calendar.getInstance().getTime()) + " AccountID: " + accountId + " GetLoadBalancerStats, retrieved from cache... Time taken: " + DateHelpers.getTotalTimeTaken(cal) + " ms");
            return stats;
        }
        return stats;
    }

    @Override
    public String getSslCiphers(Integer accountId, Integer loadbalancerId) throws EntityNotFoundException, RemoteException, MalformedURLException, DecryptException, RollBackException, InsufficientRequestException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadbalancerId);
        String ciphers = null;
        try {
            ciphers = reverseProxyLoadBalancerVTMAdapter.getSslCiphersByVhost(config, accountId, loadbalancerId);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
        return ciphers;
    }

    @Override
    public void setSslCiphers(Integer accountId, Integer loadbalancerId, String ciphers) throws EntityNotFoundException, RemoteException, MalformedURLException, DecryptException, RollBackException, InsufficientRequestException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadbalancerId);
        try {
            reverseProxyLoadBalancerVTMAdapter.setSslCiphersByVhost(config, accountId, loadbalancerId, ciphers);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public String getSsl3Ciphers() throws RemoteException, EntityNotFoundException, DecryptException, RollBackException, InsufficientRequestException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        String globalCiphers = null;
        LoadBalancerEndpointConfiguration config = getConfigFirstAvaliableRest();
        try {
            globalCiphers = reverseProxyLoadBalancerVTMAdapter.getSsl3Ciphers(config);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
        return globalCiphers;
    }

    @Override
    public String getSsl3CiphersForLB(Integer loadBalancerId) throws RemoteException, EntityNotFoundException, DecryptException, RollBackException, InsufficientRequestException, VTMRestClientObjectNotFoundException, VTMRestClientException, MalformedURLException {
        String globalCiphers = null;
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancerId);
        try {
            globalCiphers = reverseProxyLoadBalancerVTMAdapter.getSsl3Ciphers(config);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
        return globalCiphers;
    }

    // Host stats
    @Override
    public int getTotalCurrentConnectionsForHost(Host host) throws RemoteException, VTMRestClientObjectNotFoundException, VTMRestClientException, MalformedURLException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigHost(host);
        return reverseProxyLoadBalancerVTMAdapter.getTotalCurrentConnectionsForHost(config);
    }

    @Override
    public Long getHostBytesIn(Host host) throws RemoteException, VTMRestClientObjectNotFoundException, VTMRestClientException, MalformedURLException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigHost(host);
        return getHostBytesIn(config);
    }

    @Override
    public Long getHostBytesOut(Host host) throws RemoteException, VTMRestClientObjectNotFoundException, VTMRestClientException, MalformedURLException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigHost(host);
        return getHostBytesOut(config);
    }

    @Override
    public Long getHostBytesIn(LoadBalancerEndpointConfiguration config) throws RemoteException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        return reverseProxyLoadBalancerVTMAdapter.getHostBytesIn(config);
    }

    @Override
    public Long getHostBytesOut(LoadBalancerEndpointConfiguration config) throws RemoteException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        return reverseProxyLoadBalancerVTMAdapter.getHostBytesOut(config);
    }

    public void setAtlasCache(AtlasCache atlasCache) {
        this.atlasCache = atlasCache;
    }

    public AtlasCache getAtlasCache() {
        return atlasCache;
    }

    @Override
    public void createHostBackup(Host host, String backupName) throws RemoteException, DecryptException, RollBackException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        LoadBalancerEndpointConfiguration config = getConfigHost(host);
        try {
            reverseProxyLoadBalancerVTMAdapter.createHostBackup(config, backupName);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void restoreHostBackup(Host host, String backupName) throws RemoteException, DecryptException, RollBackException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        LoadBalancerEndpointConfiguration config = getConfigHost(host);

        try {
            reverseProxyLoadBalancerVTMAdapter.restoreHostBackup(config, backupName);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteHostBackup(Host host, String backupName) throws RemoteException, MalformedURLException, DecryptException, RollBackException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        LoadBalancerEndpointConfiguration config = getConfigHost(host);
        try {
            reverseProxyLoadBalancerVTMAdapter.deleteHostBackup(config, backupName);
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }


    LoadBalancerEndpointConfiguration getConfigbyClusterId(Integer clusterId) throws EntityNotFoundException, DecryptException {
        Cluster cluster = hostService.getClusterById(clusterId);
        Host restEndPointHost = hostService.getRestEndPointHost(cluster.getId());
        List<String> failoverHostNames = hostService.getFailoverHostNames(cluster.getId());
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);
        List<Host> failoverHosts = hostService.getFailoverHosts(cluster.getId());

        return new LoadBalancerEndpointConfiguration(restEndPointHost, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), restEndPointHost, failoverHostNames, logFileLocation, failoverHosts);
    }

    @Override
    public LoadBalancerEndpointConfiguration getConfig(Host host) throws DecryptException {
        Cluster cluster = host.getCluster();
        Host restEndpointHost = hostService.getRestEndPointHost(cluster.getId());
        List<Host> failoverHosts = new ArrayList<>();
        List<String> failoverHostNames = new ArrayList<>();
        for (Host h : hostService.getFailoverHosts(cluster.getId())) {
            if (h.getZone().toString().equalsIgnoreCase(host.getZone().toString())) {
                failoverHostNames.add(h.getTrafficManagerName());
                failoverHosts.add(h);
            }
        }
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);

        return new LoadBalancerEndpointConfiguration(restEndpointHost, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHostNames, logFileLocation, failoverHosts);
    }

    @Override
    public LoadBalancerEndpointConfiguration getConfigHost(Host host) throws DecryptException {
        Cluster cluster = host.getCluster();
        List<Host> failoverHosts = new ArrayList<>();
        List<String> failoverHostNames = new ArrayList<>();
        for (Host h : hostService.getFailoverHosts(cluster.getId())) {
            if (h.getZone().toString().equalsIgnoreCase(host.getZone().toString())) {
                failoverHostNames.add(h.getTrafficManagerName());
                failoverHosts.add(h);
            }
        }
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);

        return new LoadBalancerEndpointConfiguration(host, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHostNames, logFileLocation, failoverHosts);
    }

    public LoadBalancerEndpointConfiguration getConfigbyLoadBalancerId(Integer lbId) throws EntityNotFoundException, DecryptException {
        LoadBalancer loadBalancer = loadBalancerService.get(lbId);
        Host host = loadBalancer.getHost();
        Cluster cluster = host.getCluster();
        Host restEndpointHost = hostService.getRestEndPointHost(cluster.getId());
        List<Host> failoverHosts = new ArrayList<>();
        List<String> failoverHostNames = new ArrayList<>();
        for (Host h : hostService.getFailoverHosts(cluster.getId())) {
            if (h.getZone().toString().equalsIgnoreCase(host.getZone().toString())) {
                failoverHostNames.add(h.getTrafficManagerName());
                failoverHosts.add(h);
            }
        }
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);
        return new LoadBalancerEndpointConfiguration(restEndpointHost, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHostNames, logFileLocation, failoverHosts);
    }

    public LoadBalancerEndpointConfiguration getConfigFirstAvaliableRest() throws EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = null;
        Host restHost = hostService.getFirstAvailableRestEndPointHost();
        Cluster cluster = restHost.getCluster();
        Integer clusterId = cluster.getId();
        List<String> failoverHostNames = hostService.getFailoverHostNames(clusterId);
        List<Host> failoverHosts = hostService.getFailoverHosts(clusterId);
        String userName = cluster.getUsername();
        String cipherText = cluster.getPassword();
        String passwd = CryptoUtil.decrypt(cipherText);
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);
        config = new LoadBalancerEndpointConfiguration(restHost, userName, passwd, restHost, failoverHostNames, logFileLocation, failoverHosts);
        return config;
    }
}
