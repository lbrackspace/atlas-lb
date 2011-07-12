package org.openstack.atlas.api.resources;

import org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.api.atom.FeedType;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.apache.abdera.model.Feed;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.*;

public class SessionPersistenceResource extends CommonDependencyProvider {

    private Integer accountId;
    private Integer loadBalancerId;
    private HttpHeaders requestHeaders;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveSessionPersistence(@QueryParam("page") Integer page) {
        if (requestHeaders.getRequestHeader("Accept").get(0).equals(APPLICATION_ATOM_XML)) {
            return getFeedResponse(page);
        }

        SessionPersistence rsp;
        try {
            org.openstack.atlas.service.domain.entities.SessionPersistence dsp = sessionPersistenceService.get(accountId, loadBalancerId);
            if (dsp == null || dsp.equals(org.openstack.atlas.service.domain.entities.SessionPersistence.NONE)) {
                rsp = new SessionPersistence();
            } else {
                rsp = dozerMapper.map(dsp, SessionPersistence.class);
            }
            return Response.status(200).entity(rsp).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response enableSessionPersistence(SessionPersistence persistence) {
        ValidatorResult result = ValidatorRepository.getValidatorFor(SessionPersistence.class).validate(persistence, HttpRequestType.POST);

        if (!result.passedValidation()) {
            return getValidationFaultResponse(result);
        }

        try {
            loadBalancerService.get(loadBalancerId, accountId);
            org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer apiLb = new org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer();
            apiLb.setSessionPersistence(persistence);
            LoadBalancer domainLb = dozerMapper.map(apiLb, LoadBalancer.class);
            domainLb.setId(loadBalancerId);
            domainLb.setAccountId(accountId);
            if (requestHeaders != null) domainLb.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));

            sessionPersistenceService.update(domainLb);
            asyncService.callAsyncLoadBalancingOperation(Operation.ENABLE_SESSION_PERSISTENCE, domainLb);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }

    }

    @DELETE
    public Response disableSessionPersistence() {
        try {
            loadBalancerService.get(loadBalancerId, accountId);
            LoadBalancer lb = new LoadBalancer();
            lb.setId(loadBalancerId);
            lb.setAccountId(accountId);

            if (requestHeaders != null) lb.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));
            sessionPersistenceService.delete(lb);
            asyncService.callAsyncLoadBalancingOperation(Operation.DISABLE_SESSION_PERSISTENCE, lb);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    private Response getFeedResponse(Integer page) {
        Map<String, Object> feedAttributes = new HashMap<String, Object>();
        feedAttributes.put("feedType", FeedType.SESSION_PERSISTENCE_FEED);
        feedAttributes.put("accountId", accountId);
        feedAttributes.put("loadBalancerId", loadBalancerId);
        feedAttributes.put("page", page);
        Feed feed = atomFeedAdapter.getFeed(feedAttributes);

        if (feed.getEntries().isEmpty()) {
            try {
                sessionPersistenceService.get(accountId, loadBalancerId);
            } catch (Exception e) {
                return ResponseFactory.getErrorResponse(e, null, null);
            }
        }

        return Response.status(200).entity(feed).build();
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
