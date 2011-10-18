package org.openstack.atlas.adapter.zxtm;

import com.zxtm.service.client.VirtualServerRule;
import com.zxtm.service.client.VirtualServerRuleRunFlag;
import org.apache.axis.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerAdapter;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.adapter.zxtm.service.ZxtmServiceStubs;
import org.openstack.atlas.datamodel.CoreAlgorithmType;
import org.openstack.atlas.service.domain.entity.*;
import org.springframework.stereotype.Service;

import java.util.Set;

//@Service
public class ZxtmAdapterImpl implements LoadBalancerAdapter {

    public static Log LOG = LogFactory.getLog(ZxtmAdapterImpl.class.getName());
    public static final String DEFAULT_ALGORITHM = CoreAlgorithmType.ROUND_ROBIN;
    public static final String SOURCE_IP = "SOURCE_IP";
    public static final String HTTP_COOKIE = "HTTP_COOKIE";
    public static final String RATE_LIMIT_HTTP = "rate_limit_http";
    public static final String RATE_LIMIT_NON_HTTP = "rate_limit_nonhttp";
    public static final String XFF = "add_x_forwarded_for_header";
    public static final VirtualServerRule ruleRateLimitHttp = new VirtualServerRule(RATE_LIMIT_HTTP, true, VirtualServerRuleRunFlag.run_every);
    public static final VirtualServerRule ruleRateLimitNonHttp = new VirtualServerRule(RATE_LIMIT_NON_HTTP, true, VirtualServerRuleRunFlag.run_every);
    public static final VirtualServerRule ruleXForwardedFor = new VirtualServerRule(XFF, true, VirtualServerRuleRunFlag.run_every);

    ZxtmServiceStubs getServiceStubs(LoadBalancerEndpointConfiguration config) throws AxisFault {
        return ZxtmServiceStubs.getServiceStubs(config.getEndpointUrl(), config.getUsername(), config.getPassword());
    }

    @Override
    public void createLoadBalancer(LoadBalancerEndpointConfiguration config, Integer accountId, LoadBalancer lb) throws AdapterException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateLoadBalancer(LoadBalancerEndpointConfiguration config, Integer accountId, LoadBalancer lb) throws AdapterException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteLoadBalancer(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) throws AdapterException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void createNodes(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteNodes(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Set<Integer> nodeIds) throws AdapterException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateNode(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Node node) throws AdapterException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteNode(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Integer nodeId) throws AdapterException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateConnectionLogging(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Boolean enabled) throws AdapterException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, ConnectionThrottle connectionThrottle) throws AdapterException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) throws AdapterException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateHealthMonitor(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, HealthMonitor monitor) throws AdapterException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteHealthMonitor(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) throws AdapterException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setSessionPersistence(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, SessionPersistence sessionPersistence) throws AdapterException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteSessionPersistence(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) throws AdapterException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
