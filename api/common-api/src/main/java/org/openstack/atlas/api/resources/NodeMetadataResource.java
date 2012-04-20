package org.openstack.atlas.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeMeta;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeMetadata;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static javax.ws.rs.core.MediaType.*;

public class NodeMetadataResource extends CommonDependencyProvider {
    private final Log LOG = LogFactory.getLog(NodeMetadataResource.class);
    private NodeMetaResource nodeMetaResource;
    private HttpHeaders requestHeaders;
    private Integer loadbalancerId;
    private Integer accountId;
    private Integer nodeId;

    @POST
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response createMetadata(List<NodeMeta> nodeMetadata) throws EntityNotFoundException, BadRequestException, ImmutableEntityException, UnprocessableEntityException {
        List<org.openstack.atlas.service.domain.entities.NodeMeta> domainNodeMetaSet = new ArrayList<org.openstack.atlas.service.domain.entities.NodeMeta>();
        for (NodeMeta meta : nodeMetadata) {
            ValidatorResult result = ValidatorRepository.getValidatorFor(NodeMeta.class).validate(meta, HttpRequestType.POST);
            if (!result.passedValidation()) {
                return getValidationFaultResponse(result);
            } else {
                domainNodeMetaSet.add(dozerMapper.map(meta, org.openstack.atlas.service.domain.entities.NodeMeta.class));
            }
        }

        NodeMetadata retNodeMetadata = new NodeMetadata();
        for (org.openstack.atlas.service.domain.entities.NodeMeta meta : nodeMetadataService.createNodeMetadata(accountId, loadbalancerId, nodeId, domainNodeMetaSet)) {
            retNodeMetadata.getNodeMetas().add(dozerMapper.map(meta, NodeMeta.class));
        }
        return Response.status(Response.Status.OK).entity(retNodeMetadata).build();
    }

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveMetadata() {
        List<org.openstack.atlas.service.domain.entities.NodeMeta> domainNodeMetaSet;
        NodeMetadata returnMetadata = new NodeMetadata();

        try {
            domainNodeMetaSet = nodeMetadataService.getNodeMetadataByAccountIdNodeId(accountId, nodeId);
            for (org.openstack.atlas.service.domain.entities.NodeMeta domainMeta : domainNodeMetaSet) {
                returnMetadata.getNodeMetas().add(dozerMapper.map(domainMeta, NodeMeta.class));
            }
            return Response.status(Response.Status.OK).entity(returnMetadata).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Response deleteMetadata(@QueryParam("id") List<Integer> metaIds) {
        List<String> validationErrors;
        Collections.sort(metaIds);
        Node node = new Node();
        node.setId(nodeId);

        try {
            if (metaIds.isEmpty()) {
                BadRequestException badRequestException = new BadRequestException("Must supply one or more id's to process this request.");
                return ResponseFactory.getErrorResponse(badRequestException, null, null);
            }

            validationErrors = metadataService.prepareForMetadataDeletion(accountId, nodeId, metaIds);
            if (!validationErrors.isEmpty()) {
                return getValidationFaultResponse(validationErrors);
            }

            nodeMetadataService.deleteNodeMetadata(node, metaIds);
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
        return Response.status(200).build();
    }

    @Path("{id: [-+]?[1-9][0-9]*}")
    public NodeMetaResource retrieveNodeResource(@PathParam("id") int id) {
        nodeMetaResource.setRequestHeaders(requestHeaders);
        nodeMetaResource.setId(id);
        nodeMetaResource.setNodeId(nodeId);
        return nodeMetaResource;
    }

    public void setNodeMetaResource(NodeMetaResource nodeMetaResource) {
        this.nodeMetaResource = nodeMetaResource;
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public void setLoadbalancerId(Integer loadbalancerId) {
        this.loadbalancerId = loadbalancerId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
}