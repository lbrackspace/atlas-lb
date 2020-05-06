package org.openstack.atlas.api.integration;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.adapter.helpers.IpHelper;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerStmAdapter;
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
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;

import java.net.MalformedURLException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.*;

import static java.util.Calendar.getInstance;

public class ReverseProxyLoadBalancerServiceVTMImpl implements ReverseProxyLoadBalancerVTMService {

    final Log LOG = LogFactory.getLog(ReverseProxyLoadBalancerServiceVTMImpl.class);
    // We support multiple back end REST adapters here, decide based on endpoint versions
    private ReverseProxyLoadBalancerStmAdapter reverseProxyLoadBalancerStmAdapter;
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

    private int getVersion(URI endpoint) {
        if (endpoint.getPath().contains("/7.0/")) return 7;
        // default to version 3
        return 3;
    }

    @Override
    public void createLoadBalancer(LoadBalancer lb) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.createLoadBalancer(config, lb);
            } else {
                reverseProxyLoadBalancerStmAdapter.createLoadBalancer(config, lb);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateLoadBalancer(LoadBalancer lb, LoadBalancer queLb) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.updateLoadBalancer(config, lb, queLb);
            } else {
                reverseProxyLoadBalancerStmAdapter.updateLoadBalancer(config, lb, queLb);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteLoadBalancer(LoadBalancer lb) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.deleteLoadBalancer(config, lb);
            } else {
                reverseProxyLoadBalancerStmAdapter.deleteLoadBalancer(config, lb);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void setRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.setRateLimit(config, loadBalancer, rateLimit);
            } else {
                reverseProxyLoadBalancerStmAdapter.setRateLimit(config, loadBalancer, rateLimit);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteRateLimit(LoadBalancer loadBalancer) throws InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.deleteRateLimit(config, loadBalancer);
            } else {
                reverseProxyLoadBalancerStmAdapter.deleteRateLimit(config, loadBalancer);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) throws RemoteException, InsufficientRequestException, RollBackException, EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.updateRateLimit(config, loadBalancer, rateLimit);
            } else {
                reverseProxyLoadBalancerStmAdapter.updateRateLimit(config, loadBalancer, rateLimit);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void changeHostForLoadBalancers(List<LoadBalancer> lbs, Host newHost) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException, RemoteException {
        // All LBs should be guaranteed to be on the same host, so get the old config based on the first one
        LoadBalancerEndpointConfiguration configOld = getConfigbyLoadBalancerId(lbs.get(0).getId());
        LoadBalancerEndpointConfiguration configNew = getConfig(newHost);
        int retryCount;
        try{
            retryCount = Integer.parseInt(configuration.getString(PublicApiServiceConfigurationKeys.rest_api_retries));
        } catch (Exception e) { // NumberFormatException or ConfigurationInitializationException
            retryCount = 5;
        }

        try {
            if (getVersion(configOld.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.changeHostForLoadBalancers(configOld, configNew, lbs, retryCount);
            } else {
                reverseProxyLoadBalancerStmAdapter.changeHostForLoadBalancers(configOld, configNew, lbs, retryCount);
            }
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
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.updateVirtualIps(config, loadBalancer);
            } else {
                reverseProxyLoadBalancerStmAdapter.updateVirtualIps(config, loadBalancer);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void setNodes(LoadBalancer lb) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.setNodes(config, lb);
            } else {
                reverseProxyLoadBalancerStmAdapter.setNodes(config, lb);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeNode(LoadBalancer lb, Node node) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.removeNode(config, lb, node);
            } else {
                reverseProxyLoadBalancerStmAdapter.removeNode(config, lb, node);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeNodes(LoadBalancer lb, List<Node> nodesToRemove) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.removeNodes(config, lb, nodesToRemove);
            } else {
                reverseProxyLoadBalancerStmAdapter.removeNodes(config, lb, nodesToRemove);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateAccessList(LoadBalancer loadBalancer) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.updateAccessList(config, loadBalancer);
            } else {
                reverseProxyLoadBalancerStmAdapter.updateAccessList(config, loadBalancer);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;

        }
    }

    @Override
    public void deleteAccessList(LoadBalancer lb, List<Integer> accessListToDelete) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.deleteAccessList(config, lb, accessListToDelete);
            } else {
                reverseProxyLoadBalancerStmAdapter.deleteAccessList(config, lb, accessListToDelete);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateConnectionThrottle(LoadBalancer loadBalancer) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.updateConnectionThrottle(config, loadBalancer);
            } else {
                reverseProxyLoadBalancerStmAdapter.updateConnectionThrottle(config, loadBalancer);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteConnectionThrottle(LoadBalancer loadBalancer) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.deleteConnectionThrottle(config, loadBalancer);
            } else {
                reverseProxyLoadBalancerStmAdapter.deleteConnectionThrottle(config, loadBalancer);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateHealthMonitor(LoadBalancer loadBalancer) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.updateHealthMonitor(config, loadBalancer);
            } else {
                reverseProxyLoadBalancerStmAdapter.updateHealthMonitor(config, loadBalancer);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeHealthMonitor(LoadBalancer loadBalancer) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.deleteHealthMonitor(config, loadBalancer);
            } else {
                reverseProxyLoadBalancerStmAdapter.deleteHealthMonitor(config, loadBalancer);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateSessionPersistence(LoadBalancer loadBalancer, LoadBalancer quelb) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.updateSessionPersistence(config, loadBalancer, quelb);
            } else {
                reverseProxyLoadBalancerStmAdapter.updateSessionPersistence(config, loadBalancer, quelb);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeSessionPersistence(LoadBalancer loadBalancer, LoadBalancer quelb) throws StmRollBackException, EntityNotFoundException, DecryptException, MalformedURLException, InsufficientRequestException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.deleteSessionPersistence(config, loadBalancer, quelb);
            } else {
                reverseProxyLoadBalancerStmAdapter.deleteSessionPersistence(config, loadBalancer, quelb);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void suspendLoadBalancer(LoadBalancer lb) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.addSuspension(config, lb);
            } else {
                reverseProxyLoadBalancerStmAdapter.addSuspension(config, lb);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeSuspension(LoadBalancer lb) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.removeSuspension(config, lb);
            } else {
                reverseProxyLoadBalancerStmAdapter.removeSuspension(config, lb);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteVirtualIps(LoadBalancer lb, List<Integer> ids, UserPages up) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.deleteVirtualIps(config, lb, ids);
            } else {
                reverseProxyLoadBalancerStmAdapter.deleteVirtualIps(config, lb, ids);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }

    }

    @Override
    public boolean isEndPointWorking(Host host) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        boolean out;
        LoadBalancerEndpointConfiguration hostConfig = getConfigHost(host);
        if (getVersion(hostConfig.getRestEndpoint()) == 7) {
            out = reverseProxyLoadBalancerVTMAdapter.isEndPointWorking(hostConfig);
        } else {
            out = reverseProxyLoadBalancerStmAdapter.isEndPointWorking(hostConfig);
        }
        return out;
    }

    // Host subnet mappings
    @Override
    public Hostssubnet getSubnetMappings(Host host) throws RollBackException, MalformedURLException, DecryptException, InsufficientRequestException {
        LoadBalancerEndpointConfiguration hostConfig = getConfig(host);
        String hostName = host.getTrafficManagerName();
        Hostssubnet hostssubnet;
        try {
            if (getVersion(hostConfig.getRestEndpoint()) == 7) {
                hostssubnet = reverseProxyLoadBalancerVTMAdapter.getSubnetMappings(getConfig(host), hostName);
            } else {
                hostssubnet = reverseProxyLoadBalancerStmAdapter.getSubnetMappings(getConfig(host), hostName);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(hostConfig, af);
            throw af;
        }
        return hostssubnet;
    }

    @Override
    public void setSubnetMappings(Host host, Hostssubnet hostssubnet) throws RollBackException, MalformedURLException, DecryptException, InsufficientRequestException {
        LoadBalancerEndpointConfiguration hostConfig = getConfig(host);
        try {
            if (getVersion(hostConfig.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.setSubnetMappings(hostConfig, hostssubnet);
            } else {
                reverseProxyLoadBalancerStmAdapter.setSubnetMappings(hostConfig, hostssubnet);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(hostConfig, af);
            throw af;
        }
    }

    @Override
    public void deleteSubnetMappings(Host host, Hostssubnet hostssubnet) throws InsufficientRequestException, RollBackException, MalformedURLException, EntityNotFoundException, DecryptException {
        LoadBalancerEndpointConfiguration hostConfig = getConfig(host);
        try {
            if (getVersion(hostConfig.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.deleteSubnetMappings(hostConfig, hostssubnet);
            } else {
                reverseProxyLoadBalancerStmAdapter.deleteSubnetMappings(hostConfig, hostssubnet);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(hostConfig, af);
            throw af;
        }
    }

    @Override
    public void deleteErrorFile(LoadBalancer loadBalancer, UserPages up) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException, StmRollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.deleteErrorFile(config, loadBalancer);
            } else {
                reverseProxyLoadBalancerStmAdapter.deleteErrorFile(config, loadBalancer);
            }
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
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.setErrorFile(config, loadBalancer, content);
            } else {
                reverseProxyLoadBalancerStmAdapter.setErrorFile(config, loadBalancer, content);
            }
        } catch (StmRollBackException ex) {
            checkAndSetIfRestEndPointBad(config, ex);
            throw ex;
        }
    }

    @Override
    public void uploadDefaultErrorFile(Integer clusterId, String content) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RemoteException, RollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyClusterId(clusterId);
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.uploadDefaultErrorFile(config, content);
            } else {
                reverseProxyLoadBalancerStmAdapter.uploadDefaultErrorFile(config, content);
            }
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

    public void setReverseProxyLoadBalancerStmAdapter(ReverseProxyLoadBalancerStmAdapter reverseProxyLoadBalancerStmAdapter) {
        this.reverseProxyLoadBalancerStmAdapter = reverseProxyLoadBalancerStmAdapter;
    }

    public void setReverseProxyLoadBalancerVTMAdapter(ReverseProxyLoadBalancerVTMAdapter reverseProxyLoadBalancerVTMAdapter) {
        this.reverseProxyLoadBalancerVTMAdapter = reverseProxyLoadBalancerVTMAdapter;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    private void checkAndSetIfRestEndPointBad(LoadBalancerEndpointConfiguration config, StmRollBackException ex) {
        Host configuredHost = config.getEndpointUrlHost();
        if (IpHelper.isNetworkConnectionException(ex)) {
            LOG.error(String.format("REST endpoint %s went bad marking host[%d] as bad. Exception was %s", configuredHost.getRestEndpoint(), configuredHost.getId(), Debug.getExtendedStackTrace(ex)));
            configuredHost.setRestEndpointActive(Boolean.FALSE);
            hostService.update(configuredHost);
        } else {
            LOG.warn(String.format("REST endpoint %s on host[%d] throw an STM Fault but not marking as bad as it was not a network connection error: Exception was %s", configuredHost.getRestEndpoint(), configuredHost.getId(), Debug.getExtendedStackTrace(ex)));
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
    public void updateCertificateMappings(LoadBalancer loadBalancer) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.updateCertificateMappings(config, loadBalancer);
            } else {
                reverseProxyLoadBalancerStmAdapter.updateCertificateMappings(config, loadBalancer);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateCertificateMapping(LoadBalancer loadBalancer, CertificateMapping certificateMapping) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.updateCertificateMapping(config, loadBalancer, certificateMapping);
            } else {
                reverseProxyLoadBalancerStmAdapter.updateCertificateMapping(config, loadBalancer, certificateMapping);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void deleteCertificateMapping(LoadBalancer loadBalancer, CertificateMapping certificateMapping) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.deleteCertificateMapping(config, loadBalancer, certificateMapping);
            } else {
                reverseProxyLoadBalancerStmAdapter.deleteCertificateMapping(config, loadBalancer, certificateMapping);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void updateSslTermination(LoadBalancer loadBalancer, ZeusSslTermination sslTermination) throws MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancer.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.updateSslTermination(config, loadBalancer, sslTermination);
            } else {
                reverseProxyLoadBalancerStmAdapter.updateSslTermination(config, loadBalancer, sslTermination);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public void removeSslTermination(LoadBalancer lb) throws RemoteException, MalformedURLException, EntityNotFoundException, DecryptException, InsufficientRequestException, RollBackException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.removeSslTermination(config, lb);
            } else {
                reverseProxyLoadBalancerStmAdapter.removeSslTermination(config, lb);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public Stats getVirtualServerStats(LoadBalancer loadBalancer) throws EntityNotFoundException, MalformedURLException, DecryptException, InsufficientRequestException, StingrayRestClientObjectNotFoundException, StingrayRestClientException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        Integer accountId = loadBalancer.getAccountId();
        Integer loadbalancerId = loadBalancer.getId();
        LoadBalancerEndpointConfiguration config = getConfigHost(loadBalancerService.get(loadbalancerId).getHost());
        String key = CacheKeyGen.generateKeyName(accountId, loadbalancerId);
        Stats stats;

        long cal = getInstance().getTimeInMillis();
        stats = (Stats) atlasCache.get(key);
        if (stats == null) {
            if (getVersion(config.getRestEndpoint()) == 7) {
                stats = reverseProxyLoadBalancerVTMAdapter.getVirtualServerStats(config, loadBalancer);
            } else {
                stats = reverseProxyLoadBalancerStmAdapter.getVirtualServerStats(config, loadBalancer);
            }
            LOG.info("Date:" + DateHelpers.getDate(Calendar.getInstance().getTime()) + " AccountID: " + accountId + " GetLoadBalancerStats, Missed from cache, retrieved from api... Time taken: " + DateHelpers.getTotalTimeTaken(cal) + " ms");
            atlasCache.set(key, stats);
        } else {
            LOG.info("Date:" + DateHelpers.getDate(Calendar.getInstance().getTime()) + " AccountID: " + accountId + " GetLoadBalancerStats, retrieved from cache... Time taken: " + DateHelpers.getTotalTimeTaken(cal) + " ms");
            return stats;
        }
        return stats;
    }

    @Override
    public String getSslCiphers(Integer accountId, Integer loadbalancerId) throws EntityNotFoundException, RemoteException, MalformedURLException, DecryptException, RollBackException, InsufficientRequestException, StingrayRestClientException, StingrayRestClientObjectNotFoundException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadbalancerId);
        String ciphers = null;
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.getSslCiphersByVhost(config, accountId, loadbalancerId);
            } else {
                ciphers = reverseProxyLoadBalancerStmAdapter.getSslCiphersByVhost(config, accountId, loadbalancerId);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
        return ciphers;
    }

    @Override
    public void setSslCiphers(Integer accountId, Integer loadbalancerId, String ciphers) throws EntityNotFoundException, RemoteException, MalformedURLException, DecryptException, RollBackException, InsufficientRequestException, StingrayRestClientException, StingrayRestClientObjectNotFoundException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadbalancerId);
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                reverseProxyLoadBalancerVTMAdapter.setSslCiphersByVhost(config, accountId, loadbalancerId, ciphers);
            } else {
                reverseProxyLoadBalancerStmAdapter.setSslCiphersByVhost(config, accountId, loadbalancerId, ciphers);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
    }

    @Override
    public String getSsl3Ciphers() throws RemoteException, EntityNotFoundException, DecryptException, RollBackException, InsufficientRequestException, StingrayRestClientException, StingrayRestClientObjectNotFoundException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        String globalCiphers = null;
        LoadBalancerEndpointConfiguration config = getConfigFirstAvaliableRest();
        try {
            if (getVersion(config.getRestEndpoint()) == 7) {
                globalCiphers = reverseProxyLoadBalancerVTMAdapter.getSsl3Ciphers(config);
            } else {
                globalCiphers = reverseProxyLoadBalancerStmAdapter.getSsl3Ciphers(config);
            }
        } catch (RollBackException af) {
            checkAndSetIfRestEndPointBad(config, af);
            throw af;
        }
        return globalCiphers;
    }

    // Host stats
    @Override
    public int getTotalCurrentConnectionsForHost(Host host) throws RemoteException, VTMRestClientObjectNotFoundException, VTMRestClientException, MalformedURLException, DecryptException {
        // Call only available on version >= 7
        LoadBalancerEndpointConfiguration config = getConfigHost(host);

        return reverseProxyLoadBalancerVTMAdapter.getTotalCurrentConnectionsForHost(config);
    }

    @Override
    public Long getHostBytesIn(Host host) throws RemoteException, VTMRestClientObjectNotFoundException, VTMRestClientException, MalformedURLException, DecryptException {
        // Call only available on version >= 7
        LoadBalancerEndpointConfiguration config = getConfigHost(host);

        return reverseProxyLoadBalancerVTMAdapter.getHostBytesIn(config);
    }

    @Override
    public Long getHostBytesOut(Host host) throws RemoteException, VTMRestClientObjectNotFoundException, VTMRestClientException, MalformedURLException, DecryptException {
        // Call only available on version >= 7
        LoadBalancerEndpointConfiguration config = getConfigHost(host);

        return reverseProxyLoadBalancerVTMAdapter.getHostBytesOut(config);
    }

    public void setAtlasCache(AtlasCache atlasCache) {
        this.atlasCache = atlasCache;
    }

    public AtlasCache getAtlasCache() {
        return atlasCache;
    }
}
