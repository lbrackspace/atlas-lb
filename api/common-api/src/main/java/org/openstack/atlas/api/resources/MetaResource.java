package org.openstack.atlas.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

public class MetaResource extends CommonDependencyProvider {
    private final Log LOG = LogFactory.getLog(MetaResource.class);
    private HttpHeaders requestHeaders;
    private Integer accountId;
    private Integer loadBalancerId;
    private Integer id;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Response retrieveMeta() {
        org.openstack.atlas.service.domain.entities.Meta domainMeta;
        org.openstack.atlas.docs.loadbalancers.api.v1.Meta apiMeta;
        try {
            domainMeta = metadataService.getMeta(accountId, loadBalancerId, id);
            apiMeta = dozerMapper.map(domainMeta, org.openstack.atlas.docs.loadbalancers.api.v1.Meta.class);
            return Response.status(Response.Status.OK).entity(apiMeta).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    public Response deleteMeta() {
        try {
            metadataService.deleteMeta(accountId, loadBalancerId, id);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
