package org.openstack.atlas.api.resources;

import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.*;

public class CertificateMappingResource extends CommonDependencyProvider {

    private Integer id;
    private Integer accountId;
    private Integer loadBalancerId;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveCertificateMapping() {
        try {
            return Response.status(Response.Status.OK).entity(new CertificateMapping()).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response updateCertificateMapping(CertificateMapping certificateMapping) {
        ValidatorResult result = ValidatorRepository.getValidatorFor(CertificateMapping.class).validate(certificateMapping, HttpRequestType.PUT);

        if (!result.passedValidation()) {
            return getValidationFaultResponse(result);
        }

        try {
            return Response.status(Response.Status.ACCEPTED).entity(new CertificateMapping()).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    public Response deleteCertificateMapping() {
        try {
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }
}
