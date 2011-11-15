package org.openstack.atlas.rax.api.integration;

import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exception.ConnectionException;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerServiceImpl;
import org.openstack.atlas.rax.adapter.zxtm.RaxZxtmAdapter;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.entity.VirtualIpv6;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Set;

@Primary
@Service
public class RaxProxyServiceImpl extends ReverseProxyLoadBalancerServiceImpl implements RaxProxyService {

    @Override
    public void addVirtualIps(Integer accountId, Integer lbId, Set<VirtualIp> ipv4Vips, Set<VirtualIpv6> ipv6Vips) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            ((RaxZxtmAdapter) loadBalancerAdapter).addVirtualIps(config, accountId, lbId, ipv4Vips, ipv6Vips);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }
}
