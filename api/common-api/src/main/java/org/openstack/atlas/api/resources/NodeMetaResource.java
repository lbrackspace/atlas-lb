package org.openstack.atlas.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.Meta;
import org.openstack.atlas.service.domain.entities.Node;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

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
        Meta Meta;
        try {
            domainNodeMeta = nodeMetadataService.getNodeMeta(nodeId, id);
            Meta = dozerMapper.map(domainNodeMeta, org.openstack.atlas.docs.loadbalancers.api.v1.Meta.class);
            return Response.status(Response.Status.OK).entity(Meta).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response updateMeta(Meta callNodeMeta) {
        ValidatorResult result = ValidatorRepository.getValidatorFor(Meta.class).validate(callNodeMeta, HttpRequestType.PUT);

        if (!result.passedValidation()) {
            return getValidationFaultResponse(result);
        }

        callNodeMeta.setId(id);

        try {
            org.openstack.atlas.service.domain.entities.NodeMeta domainNodeMeta = dozerMapper.map(callNodeMeta, org.openstack.atlas.service.domain.entities.NodeMeta.class);
            Meta Meta = dozerMapper.map(nodeMetadataService.updateNodeMeta(accountId, loadbalancerId, nodeId, domainNodeMeta), Meta.class);

            return Response.status(Response.Status.OK).entity(Meta).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    public Response deleteMeta() {
        List<Integer> ids = new ArrayList<Integer>();
        Node node;
        ids.add(id);

        try {
            node = nodeService.getNodeByAccountIdLoadBalancerIdNodeId(accountId, loadbalancerId, nodeId);
            nodeMetadataService.prepareForNodeMetadataDeletion(accountId, loadbalancerId, nodeId, ids);
            nodeMetadataService.deleteNodeMetadata(node, ids);

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