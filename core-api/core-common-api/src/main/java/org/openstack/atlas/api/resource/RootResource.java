package org.openstack.atlas.api.resource;

import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

@Path("{accountId: [-+]?[0-9][0-9]*}")
public class RootResource {
    @PathParam("accountId")
    private Integer accountId;
    @Context
    private HttpHeaders requestHeaders;

    @Autowired
    private LoadBalancersResource loadBalancersResource;
    @Autowired
    protected AlgorithmsResource algorithmsResource;
    @Autowired
    protected ProtocolsResource protocolsResource;
    @Autowired
    private ExtensionsResource extensionsResource;

    @Path("loadbalancers")
    public LoadBalancersResource retrieveLoadBalancersResource() {
        loadBalancersResource.setRequestHeaders(requestHeaders);
        loadBalancersResource.setAccountId(accountId);
        return loadBalancersResource;
    }

    @Path("protocols")
    public ProtocolsResource retrieveProtocolsResource() {
        return protocolsResource;
    }

    @Path("algorithms")
    public AlgorithmsResource retrieveAlgorithmsResource() {
        return algorithmsResource;
    }

    @Path("extensions")
    public ExtensionsResource retrieveExtensionsResource() {
        return extensionsResource;
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
}
