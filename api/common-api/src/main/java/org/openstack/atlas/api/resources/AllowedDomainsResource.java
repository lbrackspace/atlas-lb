package org.openstack.atlas.api.resources;

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

        List<org.openstack.atlas.service.domain.entities.AllowedDomain> allowedDomains = null;
        AllowedDomains rDomains = new AllowedDomains();
        try {
            allowedDomains = allowedDomainsService.getAllowedDomains();
            for (org.openstack.atlas.service.domain.entities.AllowedDomain allowedDomain : allowedDomains) {
                rDomains.getAllowedDomains().add(dozerMapper.map(allowedDomain, AllowedDomain.class));
            }
            return Response.status(200).entity(rDomains).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }
}
