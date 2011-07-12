package org.openstack.atlas.api.resources;

import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionThrottle;
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

public class ConnectionThrottleResource extends CommonDependencyProvider {

    private Integer accountId;
    private Integer loadBalancerId;
    private HttpHeaders requestHeaders;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveConnectionThrottle(@QueryParam("page") Integer page) {
        if (requestHeaders.getRequestHeader("Accept").get(0).equals(APPLICATION_ATOM_XML)) {
            return getFeedResponse(page);
        }

        org.openstack.atlas.service.domain.entities.ConnectionLimit dcl;
        org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionThrottle rcl;
        try {
            dcl = connectionThrottleService.get(accountId, loadBalancerId);
            if (dcl == null) {
                rcl = new org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionThrottle();
            } else {
                rcl = dozerMapper.map(dcl, org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionThrottle.class);
            }
            return Response.status(200).entity(rcl).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response updateConnectionThrottle(ConnectionThrottle throttle) {
        ValidatorResult result = ValidatorRepository.getValidatorFor(ConnectionThrottle.class).validate(throttle, HttpRequestType.PUT);
        if (!result.passedValidation()) {
            return getValidationFaultResponse(result);
        }

        try {
            loadBalancerService.get(loadBalancerId, accountId);

            org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer apiLb = new org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer();
            apiLb.setConnectionThrottle(throttle);
            LoadBalancer domainLb = dozerMapper.map(apiLb, LoadBalancer.class);
            domainLb.setId(loadBalancerId);
            domainLb.setAccountId(accountId);
            if (requestHeaders != null) domainLb.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));

            connectionThrottleService.update(domainLb);
            asyncService.callAsyncLoadBalancingOperation(Operation.UPDATE_CONNECTION_THROTTLE, domainLb);
            return Response.status(202).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }

    }

    @DELETE
    public Response disableConnectionThrottle() {
        try {
            loadBalancerService.get(loadBalancerId, accountId);
            LoadBalancer lb = new LoadBalancer();
            lb.setId(loadBalancerId);
            lb.setAccountId(accountId);
            if (requestHeaders != null) lb.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));

            connectionThrottleService.prepareForDeletion(lb);
            asyncService.callAsyncLoadBalancingOperation(Operation.DELETE_CONNECTION_THROTTLE, lb);
            return Response.status(202).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    private Response getFeedResponse(Integer page) {
        Map<String, Object> feedAttributes = new HashMap<String, Object>();
        feedAttributes.put("feedType", FeedType.CONNECTION_THROTTLE_FEED);
        feedAttributes.put("accountId", accountId);
        feedAttributes.put("loadBalancerId", loadBalancerId);
        feedAttributes.put("page", page);
        Feed feed = atomFeedAdapter.getFeed(feedAttributes);

        if (feed.getEntries().isEmpty()) {
            try {
                connectionThrottleService.get(accountId, loadBalancerId);
            } catch (Exception e) {
                return ResponseFactory.getErrorResponse(e, null, null);
            }
        }

        return Response.status(200).entity(feed).build();
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Integer getAccountId() {
        return accountId;
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
