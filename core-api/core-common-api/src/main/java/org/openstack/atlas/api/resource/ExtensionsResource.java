package org.openstack.atlas.api.resource;

import org.openstack.atlas.api.response.ResponseFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

@Controller
@Scope("request")
public class ExtensionsResource {

    @GET
    public Response retrieveExtensions() {
        try {
            // TODO: Grab all available extensions here.
            return Response.status(Response.Status.OK).entity("Extensions currently in development...").build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }
}
