package org.openstack.atlas.api.resources;

import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping;
import org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMappings;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

public class CertificateMappingsResource extends CommonDependencyProvider {

    private Integer accountId;
    private Integer loadBalancerId;
    private HttpHeaders requestHeaders;

    private CertificateMappingResource certificateMappingResource;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Response retrieveCertificateMappings() {
        try {
            CertificateMappings certificateMappings = new CertificateMappings();
            List<org.openstack.atlas.service.domain.entities.CertificateMapping> dbCertMappings = certificateMappingService.getAllForLoadBalancerId(loadBalancerId);

            for (org.openstack.atlas.service.domain.entities.CertificateMapping dbCertMapping : dbCertMappings) {
                certificateMappings.getCertificateMappings().add(dozerMapper.map(dbCertMapping, CertificateMapping.class, "HIDE_KEY_AND_CERTS"));
            }

            return Response.status(Response.Status.OK).entity(certificateMappings).build();
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
            Set<org.openstack.atlas.service.domain.entities.CertificateMapping> certificateMappingSet = new HashSet<org.openstack.atlas.service.domain.entities.CertificateMapping>();
            certificateMappingSet.add(dozerMapper.map(certificateMapping, org.openstack.atlas.service.domain.entities.CertificateMapping.class));

            LoadBalancer lb = new LoadBalancer();
            lb.setId(loadBalancerId);
            lb.setAccountId(accountId);
            lb.setUserName(getUserName(requestHeaders));
            lb.setCertificateMappings(certificateMappingSet);

            org.openstack.atlas.service.domain.entities.CertificateMapping dbCertMapping = certificateMappingService.create(lb);
            CertificateMapping returnMapping = dozerMapper.map(dbCertMapping, CertificateMapping.class);

            MessageDataContainer dataContainer = new MessageDataContainer();
            dataContainer.setAccountId(accountId);
            dataContainer.setLoadBalancerId(loadBalancerId);
            dataContainer.setUserName(getUserName(requestHeaders));
            dataContainer.setCertificateMapping(dbCertMapping);

            asyncService.callAsyncLoadBalancingOperation(Operation.UPDATE_CERTIFICATE_MAPPING, dataContainer);
            return Response.status(Response.Status.ACCEPTED).entity(returnMapping).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @Path("{id: [-+]?[1-9][0-9]*}")
    public CertificateMappingResource retrieveCertificateMappingResource(@PathParam("id") int id) {
        certificateMappingResource.setId(id);
        certificateMappingResource.setAccountId(accountId);
        certificateMappingResource.setLoadBalancerId(loadBalancerId);
        certificateMappingResource.setRequestHeaders(requestHeaders);
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

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
}
