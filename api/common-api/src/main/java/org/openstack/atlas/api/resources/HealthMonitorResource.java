package org.openstack.atlas.api.resources;

import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.api.atom.FeedType;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.apache.abdera.model.Feed;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;
import static javax.ws.rs.core.MediaType.*;

public class HealthMonitorResource extends CommonDependencyProvider {

    private Integer accountId;
    private Integer loadBalancerId;
    private final ResourceValidator<HealthMonitor> validator = ValidatorRepository.getValidatorFor(HealthMonitor.class);
    private HttpHeaders requestHeaders;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveHealthMonitor(@QueryParam("page") Integer page) {
        if (requestHeaders.getRequestHeader("Accept").get(0).equals(APPLICATION_ATOM_XML)) {
            return getFeedResponse(page);
        }

        try {
            org.openstack.atlas.service.domain.entities.HealthMonitor dbHealthMonitor = healthMonitorService.get(accountId, loadBalancerId);
            HealthMonitor restHealthMonitor = dozerMapper.map(dbHealthMonitor, org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor.class);
            return Response.status(Response.Status.OK).entity(restHealthMonitor).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response updateHealthMonitor(HealthMonitor healthMonitor) {
        ValidatorResult result = validator.validate(healthMonitor, PUT);

        if (!result.passedValidation()) {
            return getValidationFaultResponse(result);
        }

        try {
            loadBalancerService.get(loadBalancerId,accountId);

            org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer apiLb = new org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer();
            apiLb.setHealthMonitor(healthMonitor);
            LoadBalancer lb = dozerMapper.map(apiLb, LoadBalancer.class);
            lb.setId(loadBalancerId);
            lb.setAccountId(accountId);
            if (requestHeaders != null) {
                lb.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));
            }

            healthMonitorService.update(lb);
            asyncService.callAsyncLoadBalancingOperation(Operation.UPDATE_HEALTH_MONITOR, lb);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    public Response deleteHealthMonitor() {
        try {
            loadBalancerService.get(loadBalancerId,accountId);

            LoadBalancer lb = new LoadBalancer();
            lb.setId(loadBalancerId);
            lb.setAccountId(accountId);
            if (requestHeaders != null) {
                lb.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));
            }
            healthMonitorService.prepareForDeletion(lb);
            asyncService.callAsyncLoadBalancingOperation(Operation.DELETE_HEALTH_MONITOR, lb);
                return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    private Response getFeedResponse(Integer page) {
        Map<String, Object> feedAttributes = new HashMap<String, Object>();
        feedAttributes.put("feedType", FeedType.HEALTH_MONITOR_FEED);
        feedAttributes.put("accountId", accountId);
        feedAttributes.put("loadBalancerId", loadBalancerId);
        feedAttributes.put("page", page);
        Feed feed = atomFeedAdapter.getFeed(feedAttributes);

        if (feed.getEntries().isEmpty()) {
            try {
                healthMonitorService.get(accountId, loadBalancerId);
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
