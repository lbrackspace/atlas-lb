package org.openstack.atlas.api.resource;

import org.openstack.atlas.api.extension.ExtensionService;
import org.openstack.atlas.api.response.ResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

@Controller
@Scope("request")
public class ExtensionsResource {

    @Autowired
    private ExtensionService extensionService;

    @GET
    @Produces({APPLICATION_XML})
    public Response retrieveExtensionsAsXml() {
        try {
            String extensions = extensionService.getExtensionsAsXml();
            return Response.status(Response.Status.OK).entity(extensions).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseFactory.getErrorResponse(e);
        }
    }

    @GET
    @Produces({APPLICATION_JSON})
    public Response retrieveExtensionsAsJson() {
        try {
            String extensions = extensionService.getExtensionsAsJson();
            return Response.status(Response.Status.OK).entity(extensions).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseFactory.getErrorResponse(e);
        }
    }


    @GET
    @Path("{alias}")
    @Produces({APPLICATION_XML})
    public Response getExtensionAsXml(@PathParam("alias") String alias) {
        try {
            String extension = extensionService.getExtensionAsXml(alias);
            return Response.status(Response.Status.OK).entity(extension).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseFactory.getErrorResponse(e);
        }
    }

    @GET
    @Path("{alias}")
    @Produces({APPLICATION_JSON})
    public Response getExtensionAsJson(@PathParam("alias") String alias) {
        try {
            String extension = extensionService.getExtensionAsJson(alias);
            return Response.status(Response.Status.OK).entity(extension).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseFactory.getErrorResponse(e);
        }
    }
}
