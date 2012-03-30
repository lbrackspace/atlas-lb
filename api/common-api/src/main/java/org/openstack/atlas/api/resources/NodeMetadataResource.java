package org.openstack.atlas.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.Metadata;
import org.openstack.atlas.service.domain.entities.Meta;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Set;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_ATOM_XML;

public class NodeMetadataResource extends CommonDependencyProvider {
    private final Log LOG = LogFactory.getLog(NodeMetadataResource.class);
    private MetaResource metaResource;
    private HttpHeaders requestHeaders;
    private Integer accountId;

    @POST
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response createMetadata(Metadata metadata) {
        ValidatorResult result = ValidatorRepository.getValidatorFor(Metadata.class).validate(metadata, HttpRequestType.POST);
         if (!result.passedValidation()) {
             return getValidationFaultResponse(result);
         }

        //Todo: make an appropriate return
        return getValidationFaultResponse("Valid response stub.");
    }

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveMetadata() {
        Set<Meta> domainMetaSet;
        org.openstack.atlas.docs.loadbalancers.api.v1.Metadata returnMetadata = new org.openstack.atlas.docs.loadbalancers.api.v1.Metadata();
        try {
            //Todo: Make this the call for the node meta data, not the load balancer id calls
            domainMetaSet = metadataService.getMetadataByAccountIdLoadBalancerId(accountId, 175);
            for (org.openstack.atlas.service.domain.entities.Meta domainMeta : domainMetaSet) {
                returnMetadata.getMetas().add(dozerMapper.map(domainMeta, org.openstack.atlas.docs.loadbalancers.api.v1.Meta.class));
            }
            return Response.status(Response.Status.OK).entity(returnMetadata).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @Path("{id: [-+]?[1-9][0-9]*}")
    public MetaResource retrieveNodeResource(@PathParam("id") int id) {
        metaResource.setRequestHeaders(requestHeaders);
        metaResource.setId(id);
        metaResource.setAccountId(accountId);
        return metaResource;
    }

    public void setMetaResource(MetaResource metaResource) {
        this.metaResource = metaResource;
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
}

/*

    @DELETE
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Response deleteMetadata(@QueryParam("id") List<Integer> metaIds) {
        List<String> validationErrors;
        Collections.sort(metaIds);
        LoadBalancer dlb = new LoadBalancer();
        dlb.setId(loadBalancerId);
        dlb.setAccountId(accountId);

        try {
            if (metaIds.isEmpty()) {
                BadRequestException badRequestException = new BadRequestException("Must supply one or more id's to process this request.");
                return ResponseFactory.getErrorResponse(badRequestException, null, null);
            }

            validationErrors = metadataService.prepareForMetadataDeletion(accountId, loadBalancerId, metaIds);
            if (!validationErrors.isEmpty()) {
                return getValidationFaultResponse(validationErrors);
            }

            metadataService.deleteMetadata(dlb, metaIds);
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
        return Response.status(200).build();
    }
}
*/