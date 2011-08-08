package org.openstack.atlas.rax.api.resources;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

@Path("{id: [-+]?[0-9][0-9]*}")
public class RootResource {

    @Context
    HttpHeaders requestHeaders;

    @PathParam("id")
    private Integer accountId;
    private LoadBalancersResource loadBalancersResource;

    @Path("loadbalancers")
    public LoadBalancersResource retrieveLoadBalancersResource() {
        loadBalancersResource.setRequestHeaders(requestHeaders);
        loadBalancersResource.setAccountId(accountId);
        return loadBalancersResource;
    }

    public void setLoadBalancersResource(LoadBalancersResource loadBalancersResource) {
        this.loadBalancersResource = loadBalancersResource;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
}
