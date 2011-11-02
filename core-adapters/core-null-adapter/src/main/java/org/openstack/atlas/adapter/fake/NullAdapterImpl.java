package org.openstack.atlas.adapter.fake;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerAdapter;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.service.domain.entity.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Set;

@Primary
@Service
public class NullAdapterImpl implements LoadBalancerAdapter {
    public static Log LOG = LogFactory.getLog(NullAdapterImpl.class.getName());

    @Override
    public void createLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws AdapterException {
        LOG.info("createLoadBalancer"); // NOP
    }

    @Override
    public void updateLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws AdapterException {
        LOG.info("updateLoadBalancer");// NOP
    }

    @Override
    public void deleteLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb) throws AdapterException {
        LOG.info("deleteLoadBalancer");// NOP
    }

    @Override
    public void createNodes(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException {
        LOG.info("createNodes");// NOP
    }

    @Override
    public void deleteNodes(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException {
        LOG.info("deleteNodes");// NOP
    }

    @Override
    public void updateNode(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Node node) throws AdapterException {
        LOG.info("updateNodes");// NOP
    }

    @Override
    public void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, ConnectionThrottle connectionThrottle) throws AdapterException {
        LOG.info("updateConnectionThrottle");// NOP
    }

    @Override
    public void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) throws AdapterException {
        LOG.info("deleteConnectionThrottle");// NOP
    }

    @Override
    public void updateHealthMonitor(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, HealthMonitor monitor) throws AdapterException {
        LOG.info("updateHealthMonitor");// NOP
    }

    @Override
    public void deleteHealthMonitor(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) throws AdapterException {
        LOG.info("deleteHealthMonitor");// NOP
    }

    @Override
    public void setSessionPersistence(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, SessionPersistence sessionPersistence) throws AdapterException {
        LOG.info("setSessionPersistence");// NOP
    }

    @Override
    public void deleteSessionPersistence(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) throws AdapterException {
        LOG.info("deleteSessionPersistence");// NOP
    }

}
