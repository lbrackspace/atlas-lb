package org.openstack.atlas.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeMeta;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

public class NodeMetaResource extends CommonDependencyProvider {
    private final Log LOG = LogFactory.getLog(NodeMetaResource.class);
    private HttpHeaders requestHeaders;
    private Integer accountId;
    private Integer nodeId;
    private Integer loadbalancerId;
    private Integer id;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Response retrieveNodeMeta() {
        org.openstack.atlas.service.domain.entities.NodeMeta domainNodeMeta;
        NodeMeta nodeMeta;
        try {
            domainNodeMeta = nodeMetadataService.getNodeMeta(accountId, loadbalancerId, nodeId, id);
            nodeMeta = dozerMapper.map(domainNodeMeta, org.openstack.atlas.docs.loadbalancers.api.v1.NodeMeta.class);
            return Response.status(Response.Status.OK).entity(nodeMeta).build();
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

    public void setLoadbalancerId(Integer loadbalancerId) {
        this.loadbalancerId = loadbalancerId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
/*
package org.openstack.atlas.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.Meta;
import org.openstack.atlas.service.domain.entities.LoadBalancer;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;

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

    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response updateMeta(Meta meta) {
        ValidatorResult result = ValidatorRepository.getValidatorFor(Meta.class).validate(meta, HttpRequestType.PUT);

        if (!result.passedValidation()) {
            return getValidationFaultResponse(result);
        }

        try {
            meta.setId(id);
            org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer apiLb = new org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer();

            apiLb.getMetadata().add(meta);
            LoadBalancer domainLb = dozerMapper.map(apiLb, LoadBalancer.class);
            domainLb.setId(loadBalancerId);
            domainLb.setAccountId(accountId);
            metadataService.updateMeta(domainLb);
            return Response.status(Response.Status.OK).build();
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
}

*/