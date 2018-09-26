package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.api.resources.providers.RequestStateContainer;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class RootResource extends ManagementDependencyProvider {

    @Context
    private HttpHeaders hh;
    @Context
    private SecurityContext sc;
    @Context
    private UriInfo ui;
    @Context
    HttpHeaders requestHeaders;
    private RequestStateContainer origContainer;
    private ManagementResource mgmtResource;

    private org.openstack.atlas.api.resources.RootResource publicApiResource;

    @Path("management")
    public ManagementResource retrieveManagementResource() {
        this.origContainer.setHttpHeaders(hh);
        this.origContainer.setSecurityContext(sc);
        this.origContainer.setUriInfo(ui);
        return mgmtResource;
    }

    @Path("/{id: [1-9][0-9]*}")
    public org.openstack.atlas.api.resources.RootResource retrievePublicApiResource(@PathParam("id") int id) {
        publicApiResource.setRequestHeaders(requestHeaders);
        publicApiResource.setAccountId(id);
        this.origContainer.setHttpHeaders(hh);
        this.origContainer.setSecurityContext(sc);
        this.origContainer.setUriInfo(ui);

// TODO : Create a roles filter
/*        if(!isUserInRole("ops,cp")) {
            Response resp = ResponseFactory.accessDenied(); // Always ends up being a 404
            return resp;
        }*/

        return publicApiResource;
    }

    public void setMgmtResource(ManagementResource mgmtResource) {
        this.mgmtResource = mgmtResource;
    }

    public void setPublicApiResource(org.openstack.atlas.api.resources.RootResource publicApiResource) {
        this.publicApiResource = publicApiResource;
    }

    public RequestStateContainer getOrigContainer() {
        return origContainer;
    }

    public void setOrigContainer(RequestStateContainer origContainer) {
        this.origContainer = origContainer;
    }

}
