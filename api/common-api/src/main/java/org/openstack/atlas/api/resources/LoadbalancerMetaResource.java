package org.openstack.atlas.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.Meta;
import org.openstack.atlas.service.domain.entities.LoadBalancer;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

public class LoadbalancerMetaResource extends CommonDependencyProvider {
    private final Log LOG = LogFactory.getLog(LoadbalancerMetaResource.class);
    private HttpHeaders requestHeaders;
    private Integer accountId;
    private Integer loadBalancerId;
    private Integer id;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Response retrieveMeta() {
        org.openstack.atlas.service.domain.entities.LoadbalancerMeta domainLoadbalancerMeta;
        Meta apiMeta;
        try {
            domainLoadbalancerMeta = loadbalancerMetadataService.getLoadbalancerMeta(accountId, loadBalancerId, id);
            apiMeta = dozerMapper.map(domainLoadbalancerMeta, org.openstack.atlas.docs.loadbalancers.api.v1.Meta.class);
            return Response.status(Response.Status.OK).entity(apiMeta).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response updateMeta(Meta meta) {
        ValidatorResult result = ValidatorRepository.getValidatorFor(Meta.class).validate(meta, HttpRequestType.PUT);

        if (!result.passedValidation()) {
            return getValidationFaultResponse(result);
        }

        try {
            meta.setId(id);
            org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer apiLb = new org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer();

            apiLb.getMetadata().add(meta);
            LoadBalancer domainLb = dozerMapper.map(apiLb, LoadBalancer.class);
            domainLb.setId(loadBalancerId);
            domainLb.setAccountId(accountId);
            loadbalancerMetadataService.updateLoadbalancerMeta(domainLb);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    public Response deleteMeta() {
        try {
            loadbalancerMetadataService.deleteLoadbalancerMeta(accountId, loadBalancerId, id);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
