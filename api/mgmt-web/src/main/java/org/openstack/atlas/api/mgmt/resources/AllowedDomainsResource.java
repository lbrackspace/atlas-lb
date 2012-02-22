package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.v1.AllowedDomain;
import org.openstack.atlas.docs.loadbalancers.api.v1.AllowedDomains;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;

import static javax.ws.rs.core.MediaType.*;

public class AllowedDomainsResource extends ManagementDependencyProvider {

    private HttpHeaders requestHeaders;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retireveAllowedDomains(@QueryParam("matches") String hostName) {

        AllowedDomains rads = new AllowedDomains();
        List<String> ads;
        try {
            if (hostName != null) { // Its life a filter after the fact
                ads = new ArrayList<String>(allowedDomainsService.matches(hostName));
            } else {
                ads = new ArrayList(allowedDomainsService.getAllowedDomains());
            }
            for (String name : ads) {
                AllowedDomain ad = new AllowedDomain();
                ad.setName(name);
                rads.getAllowedDomains().add(ad);
            }
            return Response.status(200).entity(rads).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Response deleteAllowedDomain(@QueryParam("name") String name) {
        try {
            boolean wasDeleted = allowedDomainsService.remove(name);
            if (wasDeleted) {
                return ResponseFactory.getSuccessResponse(String.format("Deleted domain%s", name), 200);
            } else {
                String msg = String.format("Can't delete domain %s its already gone", name);
                return ResponseFactory.getResponseWithStatus(410, msg);
            }

        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @PUT
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Response addAllowedDomain(AllowedDomain ad) {
        String name = ad.getName();
        try {
            boolean wasAdded = allowedDomainsService.add(name);
            if (wasAdded) {
                return ResponseFactory.getSuccessResponse(String.format("Added domain %s", name), 200);
            } else {
                String msg = String.format("Not adding domain %s as it already exists", name);
                return ResponseFactory.getResponseWithStatus(409, msg);
            }
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
}
