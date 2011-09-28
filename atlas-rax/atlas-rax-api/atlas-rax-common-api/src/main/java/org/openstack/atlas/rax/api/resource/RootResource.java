package org.openstack.atlas.rax.api.resource;

import org.openstack.atlas.api.resource.AlgorithmsResource;
import org.openstack.atlas.api.resource.ProtocolsResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

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
    @Autowired
    @Qualifier("RAX-LoadBalancersResource")
    private LoadBalancersResource loadBalancersResource;
    @Autowired
    protected AlgorithmsResource algorithmsResource;
    @Autowired
    protected ProtocolsResource protocolsResource;

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

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
}
