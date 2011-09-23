package org.openstack.atlas.api.resource;

import org.apache.log4j.Logger;
import org.openstack.atlas.core.api.v1.Node;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.*;

@Controller
@Scope("request")
public class NodeResource {
    private final Logger LOG = Logger.getLogger(NodeResource.class);
    private Integer id;
    protected Integer accountId;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveNode() {
        // TODO: Implement
        return Response.status(Response.Status.OK).entity("Return something useful!").build();
    }

    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response updateNode(Node _node) {
        // TODO: Implement
        return Response.status(Response.Status.ACCEPTED).entity("Return something useful!").build();
    }

    @DELETE
    public Response deleteNode() {
        // TODO: Implement
        return Response.status(Response.Status.ACCEPTED).entity("Return something useful!").build();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
}
