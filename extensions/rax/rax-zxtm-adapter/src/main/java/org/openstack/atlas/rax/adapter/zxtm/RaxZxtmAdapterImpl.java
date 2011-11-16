package org.openstack.atlas.rax.adapter.zxtm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.adapter.zxtm.ZxtmAdapterImpl;
import org.openstack.atlas.service.domain.entity.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.util.Set;

@Primary
@Service
public class RaxZxtmAdapterImpl extends ZxtmAdapterImpl implements RaxZxtmAdapter {

    private static Log LOG = LogFactory.getLog(RaxZxtmAdapterImpl.class.getName());

    @Override
    public void addVirtualIps(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Set<VirtualIp> ipv4Vips, Set<VirtualIpv6> ipv6Vips) throws AdapterException {
        try {
            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setAccountId(accountId);
            loadBalancer.setId(lbId);

            for (VirtualIp ipv4Vip : ipv4Vips) {
                LoadBalancerJoinVip joinVip = new LoadBalancerJoinVip(null, loadBalancer, ipv4Vip);
                loadBalancer.getLoadBalancerJoinVipSet().add(joinVip);
            }

            for (VirtualIpv6 ipv6Vip : ipv6Vips) {
                LoadBalancerJoinVip6 joinVip6 = new LoadBalancerJoinVip6(null, loadBalancer, ipv6Vip);
                loadBalancer.getLoadBalancerJoinVip6Set().add(joinVip6);
            }

            addVirtualIps(config, loadBalancer);
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }
}
