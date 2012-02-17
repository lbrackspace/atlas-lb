package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AllowedDomain;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AllowedDomains;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.*;

public class AllowedDomainsResource extends ManagementDependencyProvider {

    private AllowedDomainResource allowedDomainResource;
    private HttpHeaders requestHeaders;

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

    @POST
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response createAllowedDomains(AllowedDomains allowedDomains) {
//        ValidatorResult result = ValidatorRepository.getValidatorFor(AllowedDomain.class).validate(allowedDomains, HttpRequestType.POST);
//
//        if (!result.passedValidation()) {
//            return getValidationFaultResponse(result);
//        }

        try {

            List<org.openstack.atlas.service.domain.entities.AllowedDomain> dbDomains = new ArrayList<org.openstack.atlas.service.domain.entities.AllowedDomain>();
            for (AllowedDomain allowedDomain : allowedDomains.getAllowedDomains()) {
                dbDomains.add(dozerMapper.map(allowedDomain, org.openstack.atlas.service.domain.entities.AllowedDomain.class));
            }

            allowedDomainsService.createAllowedDomain(dbDomains);

            return Response.status(Response.Status.ACCEPTED).entity(allowedDomains).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);

        }
    }

    @Path("{id: [-+]?[1-9][0-9]*}")
    public AllowedDomainResource retrieveNodeResource(@PathParam("id") int id) {
        allowedDomainResource.setRequestHeaders(requestHeaders);
        allowedDomainResource.setId(id);
        return allowedDomainResource;
    }

    public void setAllowedDomainResource(AllowedDomainResource allowedDomainResource) {
        this.allowedDomainResource = allowedDomainResource;
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
}
