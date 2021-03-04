package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.repository.ValidatorRepository;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.SslCipherProfile;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class SslCipherProfilesResource extends ManagementDependencyProvider {
    
    private SslCipherProfileResource sslCipherProfileResource;

    @Path("{id: [1-9][0-9]*}")
    public SslCipherProfileResource getHostResource(@PathParam("id") int id) {
        sslCipherProfileResource.setId(id);
        return sslCipherProfileResource;
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createCipherProfile(SslCipherProfile sslCipherProfile){
        if (!isUserInRole("ops")) {
            return ResponseFactory.accessDenied();
        }

        ValidatorResult result = ValidatorRepository.getValidatorFor(SslCipherProfile.class).validate(sslCipherProfile, HttpRequestType.POST);

        if (!result.passedValidation()) {
            return Response.status(400).entity(HttpResponseBuilder.buildBadRequestResponse("Validation fault", result.getValidationErrorMessages())).build();
        }

        try {
            org.openstack.atlas.service.domain.entities.SslCipherProfile domainSslCipherProfile =
                    getDozerMapper().map(sslCipherProfile, org.openstack.atlas.service.domain.entities.SslCipherProfile.class);
            sslCipherProfileService.create(domainSslCipherProfile);
            return Response.status(Response.Status.ACCEPTED).entity(dozerMapper.map(domainSslCipherProfile,
                    SslCipherProfile.class)).build();
        } catch(BadRequestException ex){
            return ResponseFactory.getErrorResponse(ex, null, null);
        }catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public void setSslCipherProfileResource(SslCipherProfileResource sslCipherProfileResource) {
        this.sslCipherProfileResource = sslCipherProfileResource;
    }
}
