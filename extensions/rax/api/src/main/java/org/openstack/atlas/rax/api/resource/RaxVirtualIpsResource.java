package org.openstack.atlas.rax.api.resource;

import org.openstack.atlas.api.resource.VirtualIpsResource;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.VirtualIpValidator;
import org.openstack.atlas.core.api.v1.IpVersion;
import org.openstack.atlas.core.api.v1.VipType;
import org.openstack.atlas.core.api.v1.VirtualIp;
import org.openstack.atlas.rax.api.validation.context.VirtualIpContext;
import org.openstack.atlas.rax.domain.operation.RaxOperation;
import org.openstack.atlas.rax.domain.service.RaxVirtualIpService;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.VirtualIpv6;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

@Primary
@Controller
@Scope("request")
public class RaxVirtualIpsResource extends VirtualIpsResource {

    @Autowired
    protected VirtualIpValidator validator;
    @Autowired
    protected RaxVirtualIpService virtualIpService;
    @Autowired
    protected RaxVirtualIpResource raxVirtualIpResource;

    @POST
    @Path("/ext/RAX-ATLAS-AV")
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response addIpv6VirtualIpToLoadBalancer(VirtualIp _virtualIp) {
        ValidatorResult result = validator.validate(_virtualIp, VirtualIpContext.POST_IPV6);

        if (!result.passedValidation()) {
            return ResponseFactory.getValidationFaultResponse(result);
        }

        try {
            VirtualIpv6 domainVirtualIpv6 = new VirtualIpv6();
            domainVirtualIpv6.setAccountId(accountId);

            LoadBalancer domainLb = new LoadBalancer();
            domainLb.setId(loadBalancerId);
            domainLb.setAccountId(accountId);

            VirtualIpv6 newlyAddedIpv6Vip = virtualIpService.addIpv6VirtualIpToLoadBalancer(domainVirtualIpv6, domainLb);

            MessageDataContainer dataContainer = new MessageDataContainer();
            dataContainer.setAccountId(accountId);
            dataContainer.setLoadBalancerId(loadBalancerId);
            dataContainer.setVirtualIpv6(newlyAddedIpv6Vip);
//            if (requestHeaders != null) dataContainer.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));

            asyncService.callAsyncLoadBalancingOperation(RaxOperation.RAX_ADD_VIRTUAL_IP, dataContainer);

            VirtualIp returnVip = new VirtualIp();
            returnVip.setId(newlyAddedIpv6Vip.getId());
            returnVip.setType(VipType.PUBLIC);
            returnVip.setIpVersion(IpVersion.IPV6);
            returnVip.setAddress(newlyAddedIpv6Vip.getDerivedIpString());

            return Response.status(Response.Status.ACCEPTED).entity(returnVip).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }
    
    @Path("{id: [-+]?[0-9][0-9]*}")
    public RaxVirtualIpResource retrieveRaxVirtualIpResource(@PathParam("id") int virtualIpId) {
        raxVirtualIpResource.setLoadBalancerId(loadBalancerId);
        raxVirtualIpResource.setId(virtualIpId);
        raxVirtualIpResource.setAccountId(accountId);
        return raxVirtualIpResource;
    }
}
