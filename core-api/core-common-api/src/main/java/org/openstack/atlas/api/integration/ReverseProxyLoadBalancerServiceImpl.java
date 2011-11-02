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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
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
    public void createLoadBalancer(Integer accountId, LoadBalancer lb) throws AdapterException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            loadBalancerAdapter.createLoadBalancer(config, lb);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void updateLoadBalancer(Integer accountId, LoadBalancer lb) throws AdapterException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lb.getId());
        try {
            loadBalancerAdapter.updateLoadBalancer(config, lb);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

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
    public void createNodes(Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            loadBalancerAdapter.createNodes(config, accountId, lbId, nodes);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void deleteNodes(Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            loadBalancerAdapter.deleteNodes(config, accountId, lbId, nodes);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void updateNode(Integer accountId, Integer lbId, Node node) throws AdapterException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            loadBalancerAdapter.updateNode(config, accountId, lbId, node);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void deleteNode(Integer accountId, Integer lbId, Node node) throws AdapterException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            Set<Node> nodes = new HashSet<Node>();
            nodes.add(node);
            loadBalancerAdapter.deleteNodes(config, accountId, lbId, nodes);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void updateConnectionThrottle(Integer accountId, Integer lbId, ConnectionThrottle connectionThrottle) throws AdapterException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            loadBalancerAdapter.updateConnectionThrottle(config, accountId, lbId, connectionThrottle);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void deleteConnectionThrottle(Integer accountId, Integer lbId) throws AdapterException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            loadBalancerAdapter.deleteConnectionThrottle(config, accountId, lbId);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void updateHealthMonitor(Integer accountId, Integer lbId, HealthMonitor monitor) throws AdapterException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            loadBalancerAdapter.updateHealthMonitor(config, accountId, lbId, monitor);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void deleteHealthMonitor(Integer accountId, Integer lbId) throws AdapterException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            loadBalancerAdapter.deleteHealthMonitor(config, accountId, lbId);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void setSessionPersistence(Integer lbId, Integer accountId, SessionPersistence sessionPersistence) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            loadBalancerAdapter.setSessionPersistence(config, accountId, lbId, sessionPersistence);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void deleteSessionPersistence(Integer accountId, Integer lbId) throws AdapterException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            loadBalancerAdapter.deleteSessionPersistence(config, accountId, lbId);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }


    private LoadBalancerEndpointConfiguration getConfigbyLoadBalancerId(Integer lbId) throws EntityNotFoundException, DecryptException, MalformedURLException {
        org.openstack.atlas.service.domain.entity.LoadBalancer loadBalancer = loadBalancerRepository.getById(lbId);
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
