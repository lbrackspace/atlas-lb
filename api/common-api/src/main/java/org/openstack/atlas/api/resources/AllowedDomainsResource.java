package org.openstack.atlas.api.resources;

import java.util.ArrayList;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.v1.AllowedDomain;
import org.openstack.atlas.docs.loadbalancers.api.v1.AllowedDomains;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.*;

public class AllowedDomainsResource extends CommonDependencyProvider {

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retireveAllowedDomains() {
        AllowedDomains rads = new AllowedDomains();
        List<String> ads;
        try {
            ads = new ArrayList<String>(allowedDomainsService.getAllowedDomains());
            for(String name : ads){
                AllowedDomain ad = new AllowedDomain();
                ad.setName(name);
                rads.getAllowedDomains().add(ad);
            }
            return Response.status(200).entity(rads).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }
}
