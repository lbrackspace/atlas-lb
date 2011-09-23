package org.openstack.atlas.api.resource;

import org.apache.log4j.Logger;
import org.openstack.atlas.api.validation.validator.NodesValidator;
import org.openstack.atlas.core.api.v1.Nodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.*;

@Controller
@Scope("request")
public class NodesResource {
    private final Logger LOG = Logger.getLogger(NodesResource.class);
    protected Integer accountId;

    @Autowired
    protected NodesValidator validator;
    @Autowired
    private NodeResource nodeResource;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveNodes() {
        // TODO: Implement
        return Response.status(Response.Status.OK).entity("Return something useful!").build();
    }

    @DELETE
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Response deleteNodes(@QueryParam("id") List<Integer> ids) {
        // TODO: Implement
        return Response.status(Response.Status.ACCEPTED).entity("Return something useful!").build();
    }

    @POST
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response createNodes(Nodes _nodes) {
        // TODO: Implement
        return Response.status(Response.Status.ACCEPTED).entity("Return something useful!").build();
    }

    @Path("{id: [-+]?[1-9][0-9]*}")
    public NodeResource retrieveNodeResource(@PathParam("id") int id) {
        nodeResource.setId(id);
        nodeResource.setAccountId(accountId);
        return nodeResource;
    }
}
