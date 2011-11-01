package org.openstack.atlas.api.resource;

import org.apache.log4j.Logger;
import org.openstack.atlas.api.response.ResponseFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.*;

@Controller
@Scope("request")
public class VirtualIpsResource {
    private final Logger LOG = Logger.getLogger(VirtualIpsResource.class);
    private Integer accountId;
    private Integer loadBalancerId;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveVirtualIps() {
        try {
            // TODO: Implement
            return Response.status(Response.Status.OK).entity("Return something useful!").build();
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
