package org.openstack.atlas.service.domain.common;

import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.datamodel.CoreNodeStatus;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.entity.VirtualIpType;

public class LoadBalancerDefaultBuilder {

    private LoadBalancerDefaultBuilder() {
    }

    public static LoadBalancer addDefaultValues(final LoadBalancer loadBalancer) {
        loadBalancer.setStatus(CoreLoadBalancerStatus.QUEUED);
        NodesHelper.setNodesToStatus(loadBalancer, CoreNodeStatus.ONLINE);

        // Add an IPv4 virtual ip as default
        if (loadBalancer.getLoadBalancerJoinVipSet().isEmpty() && loadBalancer.getLoadBalancerJoinVip6Set().isEmpty()) {
            VirtualIp virtualIp = new VirtualIp();
            virtualIp.setVipType(VirtualIpType.PUBLIC);
            LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip(loadBalancer.getPort(), loadBalancer, virtualIp);
            loadBalancer.getLoadBalancerJoinVipSet().add(loadBalancerJoinVip);
        }

        return loadBalancer;
    }
}
