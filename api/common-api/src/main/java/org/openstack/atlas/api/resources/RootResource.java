package org.openstack.atlas.api.resources;

import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

@Path("{id: [-+]?[0-9][0-9]*}")
public class RootResource extends CommonDependencyProvider {

    @Context
    HttpHeaders requestHeaders;

    @PathParam("id")
    private Integer accountId;
    private LoadBalancersResource loadBalancersResource;
    private ThrowResource throwResource; // Yes for testing

    @Path("loadbalancers")
    public LoadBalancersResource retrieveLoadBalancersResource() {
        loadBalancersResource.setRequestHeaders(requestHeaders);
        loadBalancersResource.setAccountId(accountId);
        return loadBalancersResource;
    }

    @Path("throw")
    public ThrowResource retrieveThrowResource() {
        return throwResource;
    }

    public void setLoadBalancersResource(LoadBalancersResource loadBalancersResource) {
        this.loadBalancersResource = loadBalancersResource;
    }

    public void setThrowResource(ThrowResource throwResource) {
        this.throwResource = throwResource;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
}
