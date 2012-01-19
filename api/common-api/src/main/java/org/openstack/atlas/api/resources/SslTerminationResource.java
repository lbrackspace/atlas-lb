package org.openstack.atlas.api.resources;

import org.apache.abdera.model.Feed;
import org.openstack.atlas.api.atom.FeedType;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.*;

import static javax.ws.rs.core.MediaType.*;

public class SslTerminationResource extends CommonDependencyProvider {

    private int id;
    private Integer accountId;
    private Integer loadBalancerId;
    private HttpHeaders requestHeaders;


    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Response retrieveSSL() {
        try {
//            org.openstack.atlas.service.domain.entities.SslTermination ssl = sslTerminationService.getSslTermination(loadBalancerId, accountId);
//               SslTermination dataSsl = dozerMapper.map(ssl, SslTermination.class);

            SslTermination sslTermination = new SslTermination();
            sslTermination.setCertificate("aGiantCert");
            sslTermination.setPrivatekey("APrivateKeyBLIGGITYBLAH");
            sslTermination.setIntermediateCertificate("anIntermediateCert");
            sslTermination.setEnabled(true);
            sslTermination.setSecurePort(443);
            sslTermination.setSecureTrafficOnly(false);
            return Response.status(Response.Status.OK).entity(sslTermination).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response createSsl(SslTermination ssl) {
        ValidatorResult result = ValidatorRepository.getValidatorFor(SslTermination.class).validate(ssl, HttpRequestType.PUT);
        if (!result.passedValidation()) {
            return getValidationFaultResponse(result);
        }

        try {
            org.openstack.atlas.service.domain.entities.SslTermination domainSslTermination = dozerMapper.map(ssl,
                    org.openstack.atlas.service.domain.entities.SslTermination.class);

            SslTermination apiSsl = dozerMapper.map(sslTerminationService.updateSslTermination(loadBalancerId, accountId,
                    domainSslTermination), SslTermination.class);

            MessageDataContainer dataContainer = new MessageDataContainer();
            dataContainer.setAccountId(accountId);
            dataContainer.setLoadBalancerId(loadBalancerId);
            dataContainer.setUserName(getUserName(requestHeaders));
            dataContainer.setQueSslTermination(domainSslTermination);

            asyncService.callAsyncLoadBalancingOperation(Operation.UPDATE_SSL_TERMINATION, dataContainer);
            return Response.status(Response.Status.ACCEPTED).entity(apiSsl).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    public Response removeSsl() {
        try {
            MessageDataContainer dataContainer = new MessageDataContainer();
            dataContainer.setAccountId(accountId);
            dataContainer.setLoadBalancerId(loadBalancerId);
            dataContainer.setUserName(getUserName(requestHeaders));

            asyncService.callAsyncLoadBalancingOperation(Operation.DELETE_SSL_TERMINATION, dataContainer);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }



    private Response getFeedResponse(Integer page) {
        Map<String, Object> feedAttributes = new HashMap<String, Object>();
        feedAttributes.put("feedType", FeedType.NODE_FEED);
        feedAttributes.put("accountId", accountId);
        feedAttributes.put("loadBalancerId", loadBalancerId);
        feedAttributes.put("nodeId", id);
        feedAttributes.put("page", page);
        Feed feed = atomFeedAdapter.getFeed(feedAttributes);

        if (feed.getEntries().isEmpty()) {
            try {
                nodeService.getNodeByAccountIdLoadBalancerIdNodeId(accountId, loadBalancerId, id);
            } catch (Exception e) {
                return ResponseFactory.getErrorResponse(e, null, null);
            }
        }

        return Response.status(200).entity(feed).build();
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Integer getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
}
