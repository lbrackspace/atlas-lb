package org.openstack.atlas.api.integration;


import org.apache.axis.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.adapter.helpers.IpHelper;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerStmAdapter;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.service.domain.cache.AtlasCache;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.service.domain.services.HealthMonitorService;
import org.openstack.atlas.service.domain.services.HostService;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.util.crypto.CryptoUtil;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.openstack.atlas.util.debug.Debug;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.List;

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
    public void createLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.createLoadBalancer(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override public void updateLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.updateLoadBalancer(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        } catch (StmRollBackException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void deleteLoadBalancer(LoadBalancer lb) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.deleteLoadBalancer(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void setRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.setRateLimit(config, loadBalancer, rateLimit);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteRateLimit(LoadBalancer loadBalancer) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.deleteRateLimit(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.updateRateLimit(config, loadBalancer, rateLimit);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void changeHostForLoadBalancer(LoadBalancer lb, Host newHost) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.changeHostForLoadBalancer(config, lb, newHost);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void addVirtualIps(Integer lbId, Integer accountId, LoadBalancer loadBalancer) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            reverseProxyLoadBalancerStmAdapter.addVirtualIps(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void setNodes(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.setNodes(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeNode(LoadBalancer lb, Node node) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.removeNode(config, lb, node);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeNodes(LoadBalancer lb, List<Node> nodesToRemove) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.removeNodes(config, lb, nodesToRemove);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateAccessList(LoadBalancer loadBalancer) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.updateAccessList(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;

        }
    }

    @Override
    public void deleteAccessList(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.deleteAccessList(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateConnectionThrottle(LoadBalancer loadBalancer) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.updateConnectionThrottle(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteConnectionThrottle(LoadBalancer loadBalancer) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.deleteConnectionThrottle(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateHealthMonitor(LoadBalancer loadBalancer) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.updateHealthMonitor(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeHealthMonitor(LoadBalancer loadBalancer) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.removeHealthMonitor(config, loadBalancer);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void suspendLoadBalancer(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.suspendLoadBalancer(config, lb);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeSuspension(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.removeSuspension(config, lb);
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
                reverseProxyLoadBalancerStmAdapter.deleteVirtualIps(config, lb, ids);
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
        out = reverseProxyLoadBalancerStmAdapter.isEndPointWorking(hostConfig);
        return out;
    }

    @Override
    public Hostssubnet getSubnetMappings(Host host) throws Exception {
        LoadBalancerEndpointConfiguration hostConfig = getConfig(host);
        String hostName = host.getTrafficManagerName();
        Hostssubnet hostssubnet;
        try {
            hostssubnet = reverseProxyLoadBalancerStmAdapter.getSubnetMappings(getConfig(host), hostName);
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
            reverseProxyLoadBalancerStmAdapter.setSubnetMappings(hostConfig, hostssubnet);
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
            reverseProxyLoadBalancerStmAdapter.deleteSubnetMappings(hostConfig, hostssubnet);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(hostConfig, af);
            throw af;
        }
    }

    @Override
    public void deleteErrorFile(LoadBalancer loadBalancer) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException, StmRollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.deleteErrorFile(config, loadBalancer);
        } catch (StmRollBackException ex) {
            checkAndSetIfSoapEndPointBad(config, ex);
            throw ex;
        } catch (RollBackException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void setErrorFile(LoadBalancer loadBalancer, String content)
            throws RemoteException, InsufficientRequestException, StmRollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.setErrorFile(config, loadBalancer, content);
        } catch (StmRollBackException ex) {
            checkAndSetIfSoapEndPointBad(config, ex);
            throw ex;
        } catch (RollBackException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void uploadDefaultErrorFile(Integer clusterId, String content) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException {
        LoadBalancerEndpointConfiguration config = getConfigbyClusterId(clusterId);
        try {
            reverseProxyLoadBalancerStmAdapter.uploadDefaultErrorFile(config, content);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        } catch (RollBackException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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

    public void setreverseProxyLoadBalancerStmAdapter(ReverseProxyLoadBalancerStmAdapter reverseProxyLoadBalancerStmAdapter) {
        this.reverseProxyLoadBalancerStmAdapter = reverseProxyLoadBalancerStmAdapter;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    private void checkAndSetIfSoapEndPointBad(LoadBalancerEndpointConfiguration config, StmRollBackException ex) throws AxisFault {
        Host configuredHost = config.getEndpointUrlHost();
        if (IpHelper.isNetworkConnectionException(ex)) {
            LOG.error(String.format("STM endpoint %s went bad marking host[%d] as bad. Exception was %s", configuredHost.getEndpoint(), configuredHost.getId(), Debug.getExtendedStackTrace(ex)));
            configuredHost.setSoapEndpointActive(Boolean.FALSE);
            hostService.update(configuredHost);
        }
        LOG.warn(String.format("STM endpoint %s on host[%d] throw an STM Fault but not marking as bad as it was not a network connection error: Exception was %s", configuredHost.getEndpoint(), configuredHost.getId(), Debug.getExtendedStackTrace(ex)));
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
    public void updateSslTermination(LoadBalancer loadBalancer, ZeusSslTermination sslTermination) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.updateSslTermination(config, loadBalancer, sslTermination);
        } catch (AxisFault af) {
            checkAndSetIfSoapEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeSslTermination(LoadBalancer lb) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            reverseProxyLoadBalancerStmAdapter.removeSslTermination(config, lb);
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
