package org.openstack.atlas.rax.adapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerAdapter;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.service.domain.entity.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Primary
@Component
public class ZeusAdapterImpl implements LoadBalancerAdapter {
    public static Log LOG = LogFactory.getLog(ZeusAdapterImpl.class.getName());

    @Override
    public void createLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws AdapterException {
        LOG.info("Zeus createLoadBalancer"); // NOP
    }

    @Override
    public void deleteLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws AdapterException {
        LOG.info("Zeus deleteLoadBalancer");// NOP
    }

    @Override
    public void updateProtocol(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, LoadBalancerProtocol protocol) throws AdapterException {
        LOG.info("Zeus updateProtocol"); // NOP
    }

    @Override
    public void updatePort(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Integer port) throws AdapterException {
        LOG.info("Zeus updatePort"); // NOP
    }

    @Override
    public void setLoadBalancingAlgorithm(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, LoadBalancerAlgorithm algorithm) throws AdapterException {
        LOG.info("Zeus setLoadBalancingAlgorithm"); // NOP
    }

    @Override
    public void addVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer) throws AdapterException {
        LOG.info("Zeus addVirtualIps"); // NOP
    }

    @Override
    public void deleteVirtualIp(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Integer vipId) throws AdapterException {
        LOG.info("Zeus deleteVirtualIp"); // NOP
    }

    @Override
    public void deleteVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, List<Integer> vipId) throws AdapterException {
        LOG.info("Zeus deleteVirtualIps"); // NOP
    }

    @Override
    public void changeHostForLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer loadBalancer, Host newHost) throws AdapterException {
        LOG.info("Zeus changeHostForLoadBalancer"); // NOP
    }

    @Override
    public void setNodes(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Collection<Node> nodes) throws AdapterException {
        LOG.info("Zeus setNodes"); // NOP
    }

    @Override
    public void removeNodes(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Collection<Node> nodes) throws AdapterException {
        LOG.info("Zeus removeNodes"); // NOP
    }

    @Override
    public void removeNode(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, String ipAddress, Integer port) throws AdapterException {
        LOG.info("Zeus removeNode"); // NOP
    }

    @Override
    public void setNodeWeights(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, Collection<Node> nodes) throws AdapterException {
        LOG.info("Zeus setNodeWeights"); // NOP
    }

    @Override
    public void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, ConnectionThrottle throttle) throws AdapterException {
        LOG.info("Zeus updateConnectionThrottle"); // NOP
    }

    @Override
    public void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws AdapterException {
        LOG.info("Zeus deleteConnectionThrottle"); // NOP
    }

    @Override
    public void updateHealthMonitor(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, HealthMonitor healthMonitor) throws AdapterException {
        LOG.info("Zeus updateHealthMonitor"); // NOP
    }

    @Override
    public void removeHealthMonitor(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws AdapterException {
        LOG.info("Zeus removeHealthMonitor"); // NOP
    }

    @Override
    public void suspendLoadBalancer(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws AdapterException {
        LOG.info("Zeus suspendLoadBalancer"); // NOP
    }

    @Override
    public void removeSuspension(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws AdapterException {
        LOG.info("Zeus removeSuspension"); // NOP
    }

    @Override
    public boolean isEndPointWorking(LoadBalancerEndpointConfiguration config) throws AdapterException {
        LOG.info("Zeus isEndPointWorking");
        return true;  // NOP
    }

    @Override
    public Map<String, Long> getLoadBalancerBytesIn(LoadBalancerEndpointConfiguration config, List<String> names) throws AdapterException {
        LOG.info("Zeus getLoadBalancerBytesIn");
        return null;  // NOP
    }

    @Override
    public Map<String, Long> getLoadBalancerBytesOut(LoadBalancerEndpointConfiguration config, List<String> names) throws AdapterException {
        LOG.info("Zeus getLoadBalancerBytesOut");
        return null;  // NOP
    }
}
