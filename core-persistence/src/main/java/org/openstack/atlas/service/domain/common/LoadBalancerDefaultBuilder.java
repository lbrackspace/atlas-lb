package org.openstack.atlas.service.domain.common;

import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.datamodel.CoreNodeStatus;
import org.openstack.atlas.service.domain.entity.*;

public class LoadBalancerDefaultBuilder {

    private LoadBalancerDefaultBuilder() {
    }

    public static LoadBalancer addDefaultValues(final LoadBalancer loadBalancer) {
        loadBalancer.setStatus(CoreLoadBalancerStatus.QUEUED);
        NodesHelper.setNodesToStatus(loadBalancer, CoreNodeStatus.ONLINE);

        // Add an IPv6 virtual ip as default
        if (loadBalancer.getLoadBalancerJoinVipSet().isEmpty() && loadBalancer.getLoadBalancerJoinVip6Set().isEmpty()) {
            VirtualIpv6 virtualIpv6 = new VirtualIpv6();
            LoadBalancerJoinVip6 loadBalancerJoinVip = new LoadBalancerJoinVip6(loadBalancer.getPort(), loadBalancer, virtualIpv6);
            loadBalancer.getLoadBalancerJoinVip6Set().add(loadBalancerJoinVip);
        }

        return loadBalancer;
    }
}
