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

public class SslCipherProfileResource extends CommonDependencyProvider {

    private Integer id;
    private Integer accountId;
    private Integer loadBalancerId;
    private HttpHeaders requestHeaders;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveSSLCipherProfile() {
        try {
            org.openstack.atlas.service.domain.entities.SslCipherProfile dbCipherProfile = sslCipherProfileService.getCipherProfileByLoadBalancerId(loadBalancerId);
            //TODO CertificateMapping certificateMapping = dozerMapper.map(dbCertMapping, CertificateMapping.class);
            //return Response.status(Response.Status.OK).entity(certificateMapping).build();
            return Response.status(Response.Status.OK).entity("retrieveSSLCipherProfile successful").build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response updateSSLCipherProfile(CertificateMapping certificateMapping) {
        /* Added the skeleton for future use for management api to update a cipher profile. */
        //TODO change the parameter to vo of SslCipherProfile

        try {
            //asyncService.callAsyncLoadBalancingOperation(Operation.UPDATE_CERTIFICATE_MAPPING, dataContainer);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @POST
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response createSSLCipherProfile(CertificateMapping certificateMapping) {
        /* Added the skeleton for future use for management api to create a cipher profile. */
        //TODO change the parameter to vo of SslCipherProfile

        try {
            //asyncService.callAsyncLoadBalancingOperation(Operation.UPDATE_CERTIFICATE_MAPPING, dataContainer);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    public Response deleteSSLCipherProfile(Integer profileId) {
        try {
            //TODO sslCipherProfileService.removeCipherProfile();
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
