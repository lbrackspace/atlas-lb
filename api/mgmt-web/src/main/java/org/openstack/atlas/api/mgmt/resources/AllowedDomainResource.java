package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.service.domain.entities.AllowedDomain;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.*;

public class AllowedDomainResource extends ManagementDependencyProvider {

    private int id;
    private HttpHeaders requestHeaders;


    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retireveAllowedDomain() {

        AllowedDomain allowedDomain = null;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.AllowedDomain rDomain;
        try {
            allowedDomain = allowedDomainsService.getAllowedDomainById(id);
            rDomain = dozerMapper.map(allowedDomain, org.openstack.atlas.docs.loadbalancers.api.management.v1.AllowedDomain.class);
            return Response.status(200).entity(rDomain).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    public Response deleteAllowedDomain() {
        try {
            AllowedDomain domainToDelete = allowedDomainsService.getAllowedDomainById(id);
            allowedDomainsService.deleteAllowedDomain(domainToDelete);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
}
