package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.repository.ValidatorRepository;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.SslCipherProfile;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

import javax.ws.rs.Consumes;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class SslCipherProfileResource extends ManagementDependencyProvider {

    private int id;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getById(){
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        SslCipherProfile dataModelSslCipherProfile;
        try {
            org.openstack.atlas.service.domain.entities.SslCipherProfile domainSslCipherProfile = sslCipherProfileService.getById(id);
            dataModelSslCipherProfile = getDozerMapper().map(domainSslCipherProfile, org.openstack.atlas.docs.loadbalancers.api.management.v1.SslCipherProfile.class);
            return Response.status(200).entity(dataModelSslCipherProfile).build();
        } catch(EntityNotFoundException ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }

    }

    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateCipherProfile(SslCipherProfile sslCipherProfile){
        if (!isUserInRole("ops")) {
            return ResponseFactory.accessDenied();
        }

        ValidatorResult result = ValidatorRepository.getValidatorFor(SslCipherProfile.class).validate(
                sslCipherProfile, HttpRequestType.PUT);

        if (!result.passedValidation()) {
            return Response.status(400).entity(HttpResponseBuilder.buildBadRequestResponse(
                    "Validation fault", result.getValidationErrorMessages())).build();
        }

        try {
            org.openstack.atlas.service.domain.entities.SslCipherProfile domainSslCipherProfile =
                    getDozerMapper().map(sslCipherProfile,
                            org.openstack.atlas.service.domain.entities.SslCipherProfile.class);
            sslCipherProfileService.update(id, domainSslCipherProfile);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch(BadRequestException ex){
            return ResponseFactory.getErrorResponse(ex, null, null);
        }catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    public Response deleteSslCipherProfile () {
        if (!isUserInRole("ops")) {
            return ResponseFactory.accessDenied();
        }
        try {
            org.openstack.atlas.service.domain.entities.SslCipherProfile sslCipherProfile = new org.openstack.atlas.service.domain.entities.SslCipherProfile();
            sslCipherProfile.setId(id);
            sslCipherProfileService.deleteSslCipherProfile(sslCipherProfile);
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

}
