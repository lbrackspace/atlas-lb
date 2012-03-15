package org.openstack.atlas.api.resources;

import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.resources.providers.RequestStateContainer;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

@Path("{id: [-+]?[0-9][0-9]*}")
public class RootResource extends CommonDependencyProvider {

    @Context
    HttpHeaders hh;
    private SecurityContext sc;
    @Context
    private UriInfo ui;
    @Context
    HttpHeaders requestHeaders;
    private RequestStateContainer origContainer;

    @PathParam("id")
    private Integer accountId;
    private LoadBalancersResource loadBalancersResource;
    private ThrowResource throwResource; // Yes for testing

    @Path("loadbalancers")
    public LoadBalancersResource retrieveLoadBalancersResource() {
        this.origContainer.setHttpHeaders(hh);
        this.origContainer.setSecurityContext(sc);
        this.origContainer.setUriInfo(ui);
        this.loadBalancersResource.setRequestHeaders(hh);
        this.loadBalancersResource.setAccountId(accountId);
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
        this.hh = requestHeaders;
    }

    public RequestStateContainer getOrigContainer() {
        return origContainer;
    }

    public void setOrigContainer(RequestStateContainer origContainer) {
        this.origContainer = origContainer;
    }
}
