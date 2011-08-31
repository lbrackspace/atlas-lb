package org.openstack.atlas.api.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerAdapter;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.adapter.exception.ConnectionException;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.common.config.Configuration;
import org.openstack.atlas.common.crypto.CryptoUtil;
import org.openstack.atlas.common.crypto.exception.DecryptException;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.impl.LoadBalancerRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class ReverseProxyLoadBalancerServiceImpl implements ReverseProxyLoadBalancerService {
    private final Log LOG = LogFactory.getLog(ReverseProxyLoadBalancerServiceImpl.class);

    @Autowired
    private Configuration configuration;
    @Autowired
    private LoadBalancerAdapter loadBalancerAdapter;
    @Autowired
    private LoadBalancerRepository loadBalancerRepository;
    @Autowired
    private HostRepository hostRepository;

    @Override
    public void createLoadBalancer(LoadBalancer lb) throws AdapterException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            loadBalancerAdapter.createLoadBalancer(config, lb);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void deleteLoadBalancer(LoadBalancer lb) throws AdapterException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            loadBalancerAdapter.deleteLoadBalancer(config, lb);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void updateAlgorithm(LoadBalancer lb) throws AdapterException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            loadBalancerAdapter.setLoadBalancingAlgorithm(config, lb.getId(), lb.getAccountId(), lb.getAlgorithm());
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void changeHostForLoadBalancer(LoadBalancer lb, Host newHost) throws AdapterException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            loadBalancerAdapter.changeHostForLoadBalancer(config, lb, newHost);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void updatePort(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            loadBalancerAdapter.updatePort(config, lb.getId(), lb.getAccountId(),
                    lb.getPort());
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void updateProtocol(LoadBalancer lb) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            loadBalancerAdapter.updateProtocol(config, lb.getId(), lb.getAccountId(),
                    lb.getProtocol());
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void addVirtualIps(Integer lbId, Integer accountId, LoadBalancer loadBalancer) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            loadBalancerAdapter.addVirtualIps(config, loadBalancer);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void setNodes(Integer lbId, Integer accountId, Set<Node> nodes) throws AdapterException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            loadBalancerAdapter.setNodes(config, lbId, accountId, nodes);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void removeNode(Integer lbId, Integer accountId, Node node) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            loadBalancerAdapter.removeNode(config, lbId, accountId, node.getAddress(), node.getPort());
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }


    @Override
    public void removeNodes(Integer lbId, Integer accountId, Collection<Node> nodes) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            loadBalancerAdapter.removeNodes(config, lbId, accountId, nodes);
        } catch (Exception exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }


    @Override
    public void setNodeWeights(Integer lbId, Integer accountId, Set<Node> nodes) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            loadBalancerAdapter.setNodeWeights(config, lbId, accountId, nodes);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }

    }

    @Override
    public void updateConnectionThrottle(Integer lbId, Integer accountId, ConnectionThrottle connectionThrottle) throws AdapterException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            loadBalancerAdapter.updateConnectionThrottle(config, lbId, accountId,
                    connectionThrottle);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void deleteConnectionThrottle(Integer lbId, Integer accountId) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            loadBalancerAdapter.deleteConnectionThrottle(config, lbId, accountId);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void updateHealthMonitor(Integer lbId, Integer accountId, HealthMonitor monitor) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            loadBalancerAdapter.updateHealthMonitor(config, lbId, accountId, monitor);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void removeHealthMonitor(Integer lbId, Integer accountId) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            loadBalancerAdapter.removeHealthMonitor(config, lbId, accountId);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void suspendLoadBalancer(Integer lbId, Integer accountId) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            loadBalancerAdapter.suspendLoadBalancer(config, lbId, accountId);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void removeSuspension(Integer id, Integer accountId) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(id);
        try {
            loadBalancerAdapter.removeSuspension(config, id, accountId);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void deleteVirtualIp(LoadBalancer lb, Integer id) {
        try {
            LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
            try {
                loadBalancerAdapter.deleteVirtualIp(config, lb, id);
            } catch (ConnectionException exc) {
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
                loadBalancerAdapter.deleteVirtualIps(config, lb, ids);
            } catch (ConnectionException exc) {
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
        out = loadBalancerAdapter.isEndPointWorking(hostConfig);
        return out;
    }

    @Override
    public LoadBalancerEndpointConfiguration getConfig(Host host) throws DecryptException, MalformedURLException {
        Cluster cluster = host.getCluster();
        Host endpointHost = hostRepository.getEndPointHost(cluster.getId());
        List<String> failoverHosts = hostRepository.getFailoverHostNames(cluster.getId());
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);
        return new LoadBalancerEndpointConfiguration(endpointHost, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHosts, logFileLocation);
    }

    @Override
    public LoadBalancerEndpointConfiguration getConfigHost(Host host) throws DecryptException, MalformedURLException {
        Cluster cluster = host.getCluster();
        List<String> failoverHosts = hostRepository.getFailoverHostNames(cluster.getId());
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);
        return new LoadBalancerEndpointConfiguration(host, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHosts, logFileLocation);
    }

    private LoadBalancerEndpointConfiguration getConfigbyLoadBalancerId(Integer lbId) throws EntityNotFoundException, DecryptException, MalformedURLException {
        LoadBalancer loadBalancer = loadBalancerRepository.getById(lbId);
        Host host = loadBalancer.getHost();
        Cluster cluster = host.getCluster();
        Host endpointHost = hostRepository.getEndPointHost(cluster.getId());
        List<String> failoverHosts = hostRepository.getFailoverHostNames(cluster.getId());
        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);
        return new LoadBalancerEndpointConfiguration(endpointHost, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHosts, logFileLocation);
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

    private void checkAndSetIfEndPointBad(LoadBalancerEndpointConfiguration config, Exception exc) throws AdapterException, Exception {
        Host badHost = config.getHost();
        if (isConnectionExcept(exc)) {
            LOG.error(String.format("Endpoint %s went bad marking host[%d] as bad.", badHost.getEndpoint(), badHost.getId()));
            badHost.setEndpointActive(Boolean.FALSE);
            hostRepository.update(badHost);
        }
    }
}
