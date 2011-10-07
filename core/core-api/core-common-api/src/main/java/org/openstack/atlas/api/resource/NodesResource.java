package org.openstack.atlas.api.resource;

import org.apache.log4j.Logger;
import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.NodesValidator;
import org.openstack.atlas.core.api.v1.Nodes;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.Node;
import org.openstack.atlas.service.domain.operation.Operation;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.LoadBalancerService;
import org.openstack.atlas.service.domain.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

import static javax.ws.rs.core.MediaType.*;

@Controller
@Scope("request")
public class NodesResource extends CommonDependencyProvider {
    private final Logger LOG = Logger.getLogger(NodesResource.class);
    protected Integer accountId;
    protected Integer loadBalancerId;
    private HttpHeaders requestHeaders;


    @Autowired
    protected NodesValidator validator;
    @Autowired
    private NodeResource nodeResource;
    @Autowired
    protected NodeService nodeService;
    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;
    @Autowired
    protected LoadBalancerService loadBalancerService;

    @POST
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response createNodes(Nodes _nodes) {
        ValidatorResult result = validator.validate(_nodes, HttpRequestType.POST);

        if (!result.passedValidation()) {
            return ResponseFactory.getValidationFaultResponse(result);
        }

        try {
            loadBalancerRepository.getByIdAndAccountId(loadBalancerId, accountId);

            org.openstack.atlas.core.api.v1.LoadBalancer apiLb = new org.openstack.atlas.core.api.v1.LoadBalancer();
            apiLb.getNodes().addAll(_nodes.getNodes());
            LoadBalancer domainLb = dozerMapper.map(apiLb, LoadBalancer.class);
            domainLb.setId(loadBalancerId);
            domainLb.setAccountId(accountId);

            domainLb.setUserName(getUserName(requestHeaders));

            Nodes returnNodes = new Nodes();
            Set<Node> dbnodes = nodeService.createNodes(domainLb);
            for (Node node : dbnodes) {
                returnNodes.getNodes().add(dozerMapper.map(node, org.openstack.atlas.core.api.v1.Node.class));
            }

            MessageDataContainer dataContainer = new MessageDataContainer();
            dataContainer.setLoadBalancer(domainLb);

            asyncService.callAsyncLoadBalancingOperation(Operation.CREATE_NODES, dataContainer);
            return Response.status(Response.Status.ACCEPTED).entity(returnNodes).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveNodes() {
        try {
            // TODO: Implement
            return Response.status(Response.Status.OK).entity("Return something useful!").build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    @DELETE
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Response deleteNodes(@QueryParam("id") List<Integer> ids) {
        try {
            // TODO: Implement
            return Response.status(Response.Status.ACCEPTED).entity("Return something useful!").build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    @Path("{id: [-+]?[1-9][0-9]*}")
    public NodeResource retrieveNodeResource(@PathParam("id") int id) {
        nodeResource.setId(id);
        nodeResource.setAccountId(accountId);
        nodeResource.setLbId(loadBalancerId);
        return nodeResource;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }
}
