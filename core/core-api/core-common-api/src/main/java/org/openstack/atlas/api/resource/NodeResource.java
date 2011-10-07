package org.openstack.atlas.api.resource;

import org.apache.log4j.Logger;
import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.NodeValidator;
import org.openstack.atlas.core.api.v1.Node;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.operation.Operation;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.service.domain.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.*;

@Controller
@Scope("request")
public class NodeResource extends CommonDependencyProvider {
    private final Logger LOG = Logger.getLogger(NodeResource.class);
    private int id;
    private Integer accountId;
    private Integer loadBalancerId;
    private HttpHeaders requestHeaders;

    @Autowired
    protected NodeValidator validator;

    @Autowired
    protected NodeService nodeService;

    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response updateNode(Node _node) {
        ValidatorResult result = validator.validate(_node, HttpRequestType.PUT);

        if (!result.passedValidation()) {
            return ResponseFactory.getValidationFaultResponse(result);
        }

        try {
            _node.setId(id);
            org.openstack.atlas.core.api.v1.LoadBalancer apiLb = new org.openstack.atlas.core.api.v1.LoadBalancer();

            apiLb.getNodes().add(_node);
            LoadBalancer domainLb = dozerMapper.map(apiLb, LoadBalancer.class);
            domainLb.setId(loadBalancerId);
            domainLb.setAccountId(accountId);
            if (requestHeaders != null) domainLb.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));

            LoadBalancer dbLb = nodeService.updateNode(domainLb);

            MessageDataContainer dataContainer = new MessageDataContainer();
            dataContainer.setLoadBalancer(dbLb);

            asyncService.callAsyncLoadBalancingOperation(Operation.UPDATE_NODE, dataContainer);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveNode() {
        try {
            // TODO: Implement
            return Response.status(Response.Status.OK).entity("Return something useful!").build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    @DELETE
    public Response deleteNode() {
        try {
            // TODO: Implement
            return Response.status(Response.Status.ACCEPTED).entity("Return something useful!").build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
}
