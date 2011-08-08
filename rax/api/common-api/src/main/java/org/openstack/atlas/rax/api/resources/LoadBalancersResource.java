package org.openstack.atlas.rax.api.resources;

import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;

import javax.ws.rs.core.Response;
import java.util.logging.Logger;

public class LoadBalancersResource extends org.openstack.atlas.api.resources.LoadBalancersResource {
    Logger logger = Logger.getLogger("LoadBalancersResource");

    @Override
    public Response createLoadBalancer(LoadBalancer loadBalancer) {
        logger.info("Entering rax createLoadBalancer: ");
        return Response.status(Response.Status.OK).entity(loadBalancer).build();

    }
}
