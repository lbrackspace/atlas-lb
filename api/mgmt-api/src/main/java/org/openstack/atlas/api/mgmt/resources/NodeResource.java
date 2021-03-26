package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.integration.AsyncService;
import org.openstack.atlas.api.mgmt.repository.ValidatorRepository;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.SslCipherProfile;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.repository.NodeRepository;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class NodeResource extends ManagementDependencyProvider {

    private int id;


    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateNode(org.openstack.atlas.docs.loadbalancers.api.management.v1.Node node) throws EntityNotFoundException {
        if (!isUserInRole("ops")) {
            return ResponseFactory.accessDenied();
        }

        ValidatorResult result = ValidatorRepository.getValidatorFor(org.openstack.atlas.docs.loadbalancers.api.management.v1.Node.class).validate(
                node, HttpRequestType.PUT);

        if (!result.passedValidation()) {
            return Response.status(400).entity(HttpResponseBuilder.buildBadRequestResponse(
                    "Validation fault", result.getValidationErrorMessages())).build();
        }

        Node dbnode = nodeService.getNodeById(id);

        try {
            org.openstack.atlas.service.domain.entities.Node domainNode =
                    getDozerMapper().map(node, Node.class);

            domainNode.setId(id);

            LoadBalancer msgLb = loadBalancerRepository.getById(dbnode.getLoadbalancer().getId());


            LoadBalancer loadBalancer = nodeService.updateNode(msgLb, domainNode);
            EsbRequest req = new EsbRequest();
            req.setLoadBalancer(loadBalancer);
            getManagementAsyncService().callAsyncLoadBalancingOperation(Operation.UPDATE_NODE, req);

            return Response.status(Response.Status.ACCEPTED).build();
        }catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    public Response deleteNode() {
        if (!isUserInRole("ops")) {
            return ResponseFactory.accessDenied();
        }

        LoadBalancer lb = new LoadBalancer();

        try {
            Node node; // Throw up an exception if this doesn't exist.
            node = nodeService.getNodeById(id);
            LoadBalancer msgLb = loadBalancerRepository.getById(node.getLoadbalancer().getId());
            LoadBalancer loadBalancer = nodeService.deleteNode(msgLb, node);
            lb.getNodes().add(node);
            loadBalancer.setNodes(lb.getNodes());
            EsbRequest req = new EsbRequest();
            req.setLoadBalancer(loadBalancer);
            getManagementAsyncService().callAsyncLoadBalancingOperation(Operation.DELETE_NODE_MGMT, req);
            return Response.status(Response.Status.ACCEPTED).build();
        }catch (Exception cne){
            return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, cne.getMessage());
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }


}
