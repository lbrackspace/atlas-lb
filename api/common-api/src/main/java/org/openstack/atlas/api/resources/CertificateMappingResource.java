package org.openstack.atlas.api.resources;

import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import java.util.HashSet;
import java.util.Set;

import static javax.ws.rs.core.MediaType.*;

public class CertificateMappingResource extends CommonDependencyProvider {

    private Integer id;
    private Integer accountId;
    private Integer loadBalancerId;
    private HttpHeaders requestHeaders;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveCertificateMapping() {
        try {
            org.openstack.atlas.service.domain.entities.CertificateMapping dbCertMapping = certificateMappingService.getByIdAndLoadBalancerId(id, loadBalancerId);
            CertificateMapping certificateMapping = dozerMapper.map(dbCertMapping, CertificateMapping.class);
            return Response.status(Response.Status.OK).entity(certificateMapping).build();
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
            certificateMapping.setId(id);
            Set<org.openstack.atlas.service.domain.entities.CertificateMapping> certificateMappingSet = new HashSet<org.openstack.atlas.service.domain.entities.CertificateMapping>();
            org.openstack.atlas.service.domain.entities.CertificateMapping domainCertMapping = dozerMapper.map(certificateMapping, org.openstack.atlas.service.domain.entities.CertificateMapping.class);
            certificateMappingSet.add(domainCertMapping);

            LoadBalancer lb = new LoadBalancer();
            lb.setId(loadBalancerId);
            lb.setAccountId(accountId);
            lb.setUserName(getUserName(requestHeaders));
            lb.setCertificateMappings(certificateMappingSet);

            certificateMappingService.update(lb);

            MessageDataContainer dataContainer = new MessageDataContainer();
            dataContainer.setAccountId(accountId);
            dataContainer.setLoadBalancerId(loadBalancerId);
            dataContainer.setUserName(getUserName(requestHeaders));
            dataContainer.setCertificateMapping(domainCertMapping);

            asyncService.callAsyncLoadBalancingOperation(Operation.UPDATE_CERTIFICATE_MAPPING, dataContainer);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    public Response deleteCertificateMapping() {
        try {
            certificateMappingService.prepareForDelete(id, loadBalancerId);

            MessageDataContainer dataContainer = new MessageDataContainer();
            dataContainer.setAccountId(accountId);
            dataContainer.setLoadBalancerId(loadBalancerId);
            dataContainer.setUserName(getUserName(requestHeaders));
            dataContainer.setCertificateMappingId(id);

            asyncService.callAsyncLoadBalancingOperation(Operation.DELETE_CERTIFICATE_MAPPING, dataContainer);
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

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
}
