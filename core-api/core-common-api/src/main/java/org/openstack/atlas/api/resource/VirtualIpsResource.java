package org.openstack.atlas.api.resource;

import org.apache.log4j.Logger;
import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.core.api.v1.IpVersion;
import org.openstack.atlas.core.api.v1.VipType;
import org.openstack.atlas.core.api.v1.VirtualIps;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.entity.VirtualIpv6;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpv6Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import java.util.List;
import java.util.Set;

import static javax.ws.rs.core.MediaType.*;

@Controller
@Scope("request")
public class VirtualIpsResource extends CommonDependencyProvider {
    private final Logger LOG = Logger.getLogger(VirtualIpsResource.class);
    protected Integer accountId;
    protected Integer loadBalancerId;

    @Autowired
    protected VirtualIpRepository virtualIpRepository;
    @Autowired
    protected VirtualIpv6Repository virtualIpv6Repository;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveVirtualIps() {
        try {
            final List<VirtualIp> ipv4Vips = virtualIpRepository.getVipsByLoadBalancerId(loadBalancerId);
            final Set<VirtualIpv6> ipv6Vips = virtualIpv6Repository.getVipsByLoadBalancerId(loadBalancerId);
            VirtualIps virtualIps = new VirtualIps();

            for (VirtualIp ipv4Vip : ipv4Vips) {
                org.openstack.atlas.core.api.v1.VirtualIp virtualIp = new org.openstack.atlas.core.api.v1.VirtualIp();
                virtualIp.setId(ipv4Vip.getId());
                virtualIp.setAddress(ipv4Vip.getAddress());
                virtualIp.setType(VipType.valueOf(ipv4Vip.getVipType().name()));
                virtualIp.setIpVersion(IpVersion.IPV4);
                virtualIps.getVirtualIps().add(virtualIp);
            }

            for (VirtualIpv6 ipv6Vip : ipv6Vips) {
                org.openstack.atlas.core.api.v1.VirtualIp virtualIp = new org.openstack.atlas.core.api.v1.VirtualIp();
                virtualIp.setId(ipv6Vip.getId());
                virtualIp.setAddress(ipv6Vip.getDerivedIpString());
                virtualIp.setType(VipType.PUBLIC);
                virtualIp.setIpVersion(IpVersion.IPV6);
                virtualIps.getVirtualIps().add(virtualIp);
            }

            return Response.status(Response.Status.OK).entity(virtualIps).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }
}
