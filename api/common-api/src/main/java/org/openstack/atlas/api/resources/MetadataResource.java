package org.openstack.atlas.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.helpers.LoadBalancerProperties;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.Metadata;
import org.openstack.atlas.docs.loadbalancers.api.v1.Nodes;
import org.openstack.atlas.service.domain.entities.Meta;
import org.openstack.atlas.service.domain.entities.Node;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static javax.ws.rs.core.MediaType.APPLICATION_ATOM_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

public class MetadataResource extends CommonDependencyProvider {
    private final Log LOG = LogFactory.getLog(MetadataResource.class);
    private HttpHeaders requestHeaders;
    private Integer accountId;
    private Integer loadBalancerId;

    @POST
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response createMetadata(Metadata metadata) {
        ValidatorResult result = ValidatorRepository.getValidatorFor(Metadata.class).validate(metadata, HttpRequestType.POST);

        if (!result.passedValidation()) {
            return getValidationFaultResponse(result);
        }

        try {
            loadBalancerService.get(loadBalancerId, accountId);

            List<org.openstack.atlas.service.domain.entities.Meta> domainMetaList = new ArrayList<Meta>();
            for (org.openstack.atlas.docs.loadbalancers.api.v1.Meta meta : metadata.getMetas()) {
                domainMetaList.add(dozerMapper.map(meta, org.openstack.atlas.service.domain.entities.Meta.class));
            }

            Set<org.openstack.atlas.service.domain.entities.Meta> dbMetadata = metadataService.createMetadata(accountId, loadBalancerId, domainMetaList);

            org.openstack.atlas.docs.loadbalancers.api.v1.Metadata returnMetadata = new org.openstack.atlas.docs.loadbalancers.api.v1.Metadata();
            for (org.openstack.atlas.service.domain.entities.Meta meta : dbMetadata) {
                returnMetadata.getMetas().add(dozerMapper.map(meta, org.openstack.atlas.docs.loadbalancers.api.v1.Meta.class));
            }

            return Response.status(Response.Status.ACCEPTED).entity(returnMetadata).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveMetadata(){
        Set<Meta> domainMetaSet;
        org.openstack.atlas.docs.loadbalancers.api.v1.Metadata returnMetadata = new org.openstack.atlas.docs.loadbalancers.api.v1.Metadata();
        try {
            domainMetaSet = metadataService.getMetadataByAccountIdLoadBalancerId(accountId, loadBalancerId);
            for (org.openstack.atlas.service.domain.entities.Meta domainMeta : domainMetaSet) {
                returnMetadata.getMetas().add(dozerMapper.map(domainMeta, org.openstack.atlas.docs.loadbalancers.api.v1.Meta.class));
            }
            return Response.status(200).entity(returnMetadata).build();
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
}
