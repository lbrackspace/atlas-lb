package org.openstack.atlas.rax.api.resources;

import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;

import javax.ws.rs.core.Response;

public class LoadBalancersResource extends org.openstack.atlas.api.resources.LoadBalancersResource {
    @Override
    public Response createLoadBalancer(LoadBalancer loadBalancer) {
        return null;
    }
}
