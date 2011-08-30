package org.openstack.atlas.adapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.service.domain.entity.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class NullAdapterImpl implements LoadBalancerAdapter {
    public static Log LOG = LogFactory.getLog(NullAdapterImpl.class.getName());
    private static String SOURCE_IP = "SOURCE_IP";
    private static String HTTP_COOKIE = "HTTP_COOKIE";
    private static LoadBalancerAlgorithm DEFAULT_ALGORITHM = LoadBalancerAlgorithm.ROUND_ROBIN;
    
    @Override
    public void createLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws AdapterException {
        LOG.info("createLoadBalancer"); // NOP
    }

    @Override
    public void deleteLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws AdapterException {
        LOG.info("deleteLoadBalancer");// NOP
    }

    @Override
    public void updateProtocol(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, LoadBalancerProtocol protocol) throws AdapterException {
        LOG.info("updateProtocol"); // NOP
    }

    @Override
    public void updatePort(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Integer port) throws AdapterException {
        LOG.info("updatePort"); // NOP
    }

    @Override
    public void setLoadBalancingAlgorithm(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, LoadBalancerAlgorithm algorithm) throws AdapterException {
        LOG.info("setLoadBalancingAlgorithm"); // NOP
    }

    @Override
    public void addVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws AdapterException {
        LOG.info("addVirtualIps"); // NOP
    }

    @Override
    public void deleteVirtualIp(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Integer vipId) throws AdapterException {
        LOG.info("deleteVirtualIp"); // NOP
    }

    @Override
    public void deleteVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, List<Integer> vipId) throws AdapterException {
        LOG.info("deleteVirtualIps"); // NOP
    }

    @Override
    public void changeHostForLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Host newHost) throws AdapterException {
        LOG.info("changeHostForLoadBalancer"); // NOP
    }

    @Override
    public void setNodes(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Collection<Node> nodes) throws AdapterException {
        LOG.info("setNodes"); // NOP
    }

    @Override
    public void removeNodes(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Collection<Node> nodes) throws AdapterException {
        LOG.info("removeNodes"); // NOP
    }

    @Override
    public void removeNode(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, String ipAddress, Integer port) throws AdapterException {
        LOG.info("removeNode"); // NOP
    }

    @Override
    public void setNodeWeights(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Collection<Node> nodes) throws AdapterException {
        LOG.info("setNodeWeights"); // NOP
    }

    @Override
    public void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, ConnectionThrottle throttle) throws AdapterException {
        LOG.info("updateConnectionThrottle"); // NOP
    }

    @Override
    public void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws AdapterException {
        LOG.info("deleteConnectionThrottle"); // NOP
    }

    @Override
    public void updateHealthMonitor(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, HealthMonitor healthMonitor) throws AdapterException {
        LOG.info("updateHealthMonitor"); // NOP
    }

    @Override
    public void removeHealthMonitor(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws AdapterException {
        LOG.info("removeHealthMonitor"); // NOP
    }

    @Override
    public void suspendLoadBalancer(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws AdapterException {
        LOG.info("suspendLoadBalancer"); // NOP
    }

    @Override
    public void removeSuspension(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws AdapterException {
        LOG.info("removeSuspension"); // NOP
    }

    @Override
    public boolean isEndPointWorking(LoadBalancerEndpointConfiguration config) throws AdapterException {
        LOG.info("isEndPointWorking");
        return true;  // NOP
    }

    @Override
    public Map<String, Long> getLoadBalancerBytesIn(LoadBalancerEndpointConfiguration config, List<String> names) throws AdapterException {
        LOG.info("getLoadBalancerBytesIn");
        return null;  // NOP
    }

    @Override
    public Map<String, Long> getLoadBalancerBytesOut(LoadBalancerEndpointConfiguration config, List<String> names) throws AdapterException {
        LOG.info("getLoadBalancerBytesOut");
        return null;  // NOP
    }
}
