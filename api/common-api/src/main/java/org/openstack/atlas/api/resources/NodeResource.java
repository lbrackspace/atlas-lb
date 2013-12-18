package org.openstack.atlas.api.resources;

import org.apache.abdera.model.Feed;
import org.openstack.atlas.api.atom.FeedType;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeType;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.operations.Operation;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.*;

import static javax.ws.rs.core.MediaType.*;

public class NodeResource extends CommonDependencyProvider {

    private Integer id;
    private Integer accountId;
    private Integer loadBalancerId;
    private HttpHeaders requestHeaders;
    private NodeMetadataResource nodeMetadataResource;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveNode(@QueryParam("page") Integer page) {
        if (requestHeaders.getRequestHeader("Accept").get(0).equals(APPLICATION_ATOM_XML)) {
            return getFeedResponse(page);
        }

        org.openstack.atlas.service.domain.entities.Node dnode;
        org.openstack.atlas.docs.loadbalancers.api.v1.Node rnode;
        try {
            dnode = nodeService.getNodeByAccountIdLoadBalancerIdNodeId(accountId, loadBalancerId, id);
            rnode = dozerMapper.map(dnode, org.openstack.atlas.docs.loadbalancers.api.v1.Node.class);
            return Response.status(200).entity(rnode).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response updateNode(Node node) {
        ValidatorResult result = ValidatorRepository.getValidatorFor(Node.class).validate(node, HttpRequestType.PUT);

        if (!result.passedValidation()) {
            return getValidationFaultResponse(result);
        }

        try {
            node.setId(id);
            org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer apiLb = new org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer();

            //BANDAIDed V1-D-10515:
            if (node.getType() == null) {
              node.setType(NodeType.valueOf(
                      nodeService.getNodeByAccountIdLoadBalancerIdNodeId(accountId, loadBalancerId, id).getType().toString()));
            }

            apiLb.getNodes().add(node);
            LoadBalancer domainLb = dozerMapper.map(apiLb, LoadBalancer.class);
            domainLb.setId(loadBalancerId);
            domainLb.setAccountId(accountId);
            if (requestHeaders != null) {
                domainLb.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));
            }

            LoadBalancer dbLb = nodeService.updateNode(domainLb);
            asyncService.callAsyncLoadBalancingOperation(Operation.UPDATE_NODE, dbLb);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    public Response deleteNode() {
        try {
            LoadBalancer msgLb = new LoadBalancer();
            Set<org.openstack.atlas.service.domain.entities.Node> nodes = new HashSet<org.openstack.atlas.service.domain.entities.Node>();
            org.openstack.atlas.service.domain.entities.Node node = new org.openstack.atlas.service.domain.entities.Node();
            node.setId(id);
            nodes.add(node);
            msgLb.setNodes(nodes);
            msgLb.setId(loadBalancerId);
            msgLb.setAccountId(accountId);
            if (requestHeaders != null) {
                msgLb.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));
            }

            LoadBalancer loadBalancer = nodeService.deleteNode(msgLb);
            asyncService.callAsyncLoadBalancingOperation(Operation.DELETE_NODE, loadBalancer);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @Path("metadata")
    public NodeMetadataResource getNodeMetaDataResource() {
        nodeMetadataResource.setRequestHeaders(requestHeaders);
        nodeMetadataResource.setAccountId(accountId);
        nodeMetadataResource.setLoadbalancerId(loadBalancerId);
        nodeMetadataResource.setNodeId(id);
        return nodeMetadataResource;
    }

    private Response getFeedResponse(Integer page) {
        Map<String, Object> feedAttributes = new HashMap<String, Object>();
        feedAttributes.put("feedType", FeedType.NODE_FEED);
        feedAttributes.put("accountId", accountId);
        feedAttributes.put("loadBalancerId", loadBalancerId);
        feedAttributes.put("nodeId", id);
        feedAttributes.put("page", page);
        Feed feed = atomFeedAdapter.getFeed(feedAttributes);

        if (feed.getEntries().isEmpty()) {
            try {
                nodeService.getNodeByAccountIdLoadBalancerIdNodeId(accountId, loadBalancerId, id);
            } catch (Exception e) {
                return ResponseFactory.getErrorResponse(e, null, null);
            }
        }

        return Response.status(200).entity(feed).build();
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Integer getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void setNodeMetadataResource(NodeMetadataResource nodeMetadataResource) {
        this.nodeMetadataResource = nodeMetadataResource;
    }
}
