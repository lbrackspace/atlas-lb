package org.openstack.atlas.rax.adapter.zxtm;

import org.apache.axis.AxisFault;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.rax.domain.entity.RaxAccessList;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.entity.VirtualIpv6;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface RaxZxtmAdapter {
    void addVirtualIps(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Set<VirtualIp> ipv4Vips, Set<VirtualIpv6> ipv6Vips) throws AdapterException;

    void deleteVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer lb, List<Integer> vipIdsToDelete) throws AdapterException;

    void updateAccessList(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Collection<RaxAccessList> accessListItems) throws AdapterException;

    void deleteAccessList(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) throws AdapterException, AxisFault;

    void updateConnectionLogging(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, boolean isConnectionLogging, String protocol) throws AdapterException;

    void uploadDefaultErrorPage(LoadBalancerEndpointConfiguration config, String content) throws AdapterException;

    void setDefaultErrorPage(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws AdapterException;

    void setErrorPage(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId, String content) throws AdapterException;

    void deleteErrorPage(LoadBalancerEndpointConfiguration config, Integer loadBalancerId, Integer accountId) throws AdapterException;
}
