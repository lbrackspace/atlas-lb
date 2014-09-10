package org.openstack.atlas.api.resources;

import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping;
import org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMappings;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

public class CertificateMappingsResource extends CommonDependencyProvider {

    private Integer accountId;
    private Integer loadBalancerId;

    private CertificateMappingResource certificateMappingResource;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Response retrieveCertificateMappings() {
        try {
            return Response.status(Response.Status.OK).entity(new CertificateMappings()).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @POST
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response createCertificateMapping(CertificateMapping certificateMapping) {
        ValidatorResult result = ValidatorRepository.getValidatorFor(CertificateMapping.class).validate(certificateMapping, HttpRequestType.POST);

        if (!result.passedValidation()) {
            return getValidationFaultResponse(result);
        }

        try {
            return Response.status(Response.Status.ACCEPTED).entity(new CertificateMapping()).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @Path("{id: [-+]?[1-9][0-9]*}")
    public CertificateMappingResource retrieveCertificateMappingResource(@PathParam("id") int id) {
        certificateMappingResource.setId(id);
        certificateMappingResource.setAccountId(accountId);
        certificateMappingResource.setLoadBalancerId(loadBalancerId);
        return certificateMappingResource;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public void setCertificateMappingResource(CertificateMappingResource certificateMappingResource) {
        this.certificateMappingResource = certificateMappingResource;
    }
}
