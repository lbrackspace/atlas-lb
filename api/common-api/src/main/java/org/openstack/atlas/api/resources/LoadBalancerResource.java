package org.openstack.atlas.api.resources;

import org.apache.abdera.model.Feed;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.atom.FeedType;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.api.helpers.ConfigurationHelper;
import org.openstack.atlas.api.helpers.LoadBalancerProperties;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.operations.Operation;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static javax.ws.rs.core.MediaType.*;

public class LoadBalancerResource extends CommonDependencyProvider {

    private final Log LOG = LogFactory.getLog(LoadBalancerResource.class);
    private AccessListResource accessListResource;
    private ConnectionLoggingResource connectionLoggingResource;
    private ContentCachingResource contentCachingResource;
    private HealthMonitorResource healthMonitorResource;
    private LoadbalancerMetadataResource loadbalancerMetadataResource;
    private NodesResource nodesResource;
    private SessionPersistenceResource sessionPersistenceResource;
    private ConnectionThrottleResource connectionThrottleResource;
    private VirtualIpsResource virtualIpsResource;
    private UsageResource usageResource;
    private ErrorpageResource errorpageResource;
    private SslTerminationResource sslTerminationResource;
    private int id;
    private Integer accountId;
    private HttpHeaders requestHeaders;
    private NodeMetadataResource nodeMetadataResource;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveLoadBalancerDetail(@QueryParam("page") Integer page) {
        if (requestHeaders.getRequestHeader("Accept").get(0).equals(APPLICATION_ATOM_XML)) {
            return getFeedResponse(page);
        }

        try {
            org.openstack.atlas.service.domain.entities.LoadBalancer domainLb = loadBalancerService.get(id, accountId);
            Set<Node> nodesList = new HashSet(domainLb.getNodes());
            Set<Node> nodesSet = new HashSet(LoadBalancerProperties.setWeightsforNodes(nodesList));
            domainLb.setNodes(nodesSet);
            LoadBalancer dataModelLb;

            if (domainLb.getStatus().equals(LoadBalancerStatus.DELETED)) {
                dataModelLb = dozerMapper.map(domainLb, LoadBalancer.class, "DELETED_LB");
            } else {
                dataModelLb = dozerMapper.map(domainLb, LoadBalancer.class);
            }
            return Response.status(Response.Status.OK).entity(dataModelLb).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    public Response deleteLoadBalancer() {
        try {
            org.openstack.atlas.service.domain.entities.LoadBalancer domainLb = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            domainLb.setId(id);
            domainLb.setAccountId(accountId);
            if (requestHeaders != null) {
                if (requestHeaders.getRequestHeader("X-PP-User") != null && requestHeaders.getRequestHeader("X-PP-User").size() > 0) {
                    domainLb.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));
                }
            }

            loadBalancerService.prepareForDelete(domainLb);
            org.openstack.atlas.service.domain.entities.LoadBalancer dbLb = loadBalancerService.get(domainLb.getId(), domainLb.getAccountId());
            asyncService.callAsyncLoadBalancingOperation(Operation.DELETE_LOADBALANCER, dbLb);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response updateLoadBalancer(LoadBalancer loadBalancer) {
        ValidatorResult result = ValidatorRepository.getValidatorFor(LoadBalancer.class).validate(loadBalancer, HttpRequestType.PUT);
        org.openstack.atlas.service.domain.entities.LoadBalancer domainLb;
        if (!result.passedValidation()) {
            return getValidationFaultResponse(result);
        }

        try {
            loadBalancerService.get(id, accountId);
            domainLb = dozerMapper.map(loadBalancer, org.openstack.atlas.service.domain.entities.LoadBalancer.class);
            domainLb.setId(id);
            domainLb.setAccountId(accountId);
            if (requestHeaders != null) {
                domainLb.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));
            }

            loadBalancerService.prepareForUpdate(domainLb);
            org.openstack.atlas.service.domain.entities.LoadBalancer dbLb = loadBalancerService.get(domainLb.getId(), domainLb.getAccountId());
            asyncService.callAsyncLoadBalancingOperation(Operation.UPDATE_LOADBALANCER, dbLb);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @GET
    @Path("stats")
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveLoadBalancerStats() {
        if (!ConfigurationHelper.isAllowed(restApiConfiguration, PublicApiServiceConfigurationKeys.stats))
            return Response.status(Response.Status.BAD_REQUEST).build();

        try {
            org.openstack.atlas.service.domain.entities.LoadBalancer loadBalancer = loadBalancerService.get(id, accountId);
            if (loadBalancer.getStatus() != LoadBalancerStatus.ACTIVE) {
                throw new ImmutableEntityException("The load balancer is not available to display statistics.");
            }
            org.openstack.atlas.docs.loadbalancers.api.v1.Stats stats = dozerMapper.map(reverseProxyLoadBalancerService
                    .getLoadBalancerStats(id, accountId), org.openstack.atlas.docs.loadbalancers.api.v1.Stats.class);

            return Response.status(Response.Status.OK).entity(stats).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @Path("accesslist")
    public AccessListResource retrieveAccessListResource() {
        accessListResource.setRequestHeaders(requestHeaders);
        accessListResource.setAccountId(accountId);
        accessListResource.setLoadBalancerId(id);
        return accessListResource;
    }

    @Path("connectionlogging")
    public ConnectionLoggingResource retrieveConnectionLoggingResource() {
        connectionLoggingResource.setAccountId(accountId);
        connectionLoggingResource.setLoadBalancerId(id);
        return connectionLoggingResource;
    }

    @Path("contentcaching")
    public ContentCachingResource retrieveContentCachingResource() {
        contentCachingResource.setAccountId(accountId);
        contentCachingResource.setLoadBalancerId(id);
        return contentCachingResource;
    }

    @Path("connectionthrottle")
    public ConnectionThrottleResource retrieveConnectionThrottleResource() {
        connectionThrottleResource.setRequestHeaders(requestHeaders);
        connectionThrottleResource.setAccountId(accountId);
        connectionThrottleResource.setLoadBalancerId(id);
        return connectionThrottleResource;
    }

    @Path("errorpage")
    public ErrorpageResource retrieveErrorpageResource() {
        errorpageResource.setAccountId(accountId);
        errorpageResource.setLoadBalancerId(id);
        return errorpageResource;
    }

    @Path("healthmonitor")
    public HealthMonitorResource retrieveHealthMonitorResource() {
        healthMonitorResource.setRequestHeaders(requestHeaders);
        healthMonitorResource.setAccountId(accountId);
        healthMonitorResource.setLoadBalancerId(id);
        return healthMonitorResource;
    }

    @Path("metadata")
    public LoadbalancerMetadataResource retrieveMetadataResource() {
        loadbalancerMetadataResource.setRequestHeaders(requestHeaders);
        loadbalancerMetadataResource.setAccountId(accountId);
        loadbalancerMetadataResource.setLoadBalancerId(id);
        return loadbalancerMetadataResource;
    }

    @Path("nodes")
    public NodesResource retrieveNodesResource() {
        nodesResource.setRequestHeaders(requestHeaders);
        nodesResource.setAccountId(accountId);
        nodesResource.setLoadBalancerId(id);
        return nodesResource;
    }

    @Path("sessionpersistence")
    public SessionPersistenceResource retrieveSessionPersistenceResource() {
        sessionPersistenceResource.setRequestHeaders(requestHeaders);
        sessionPersistenceResource.setAccountId(accountId);
        sessionPersistenceResource.setLoadBalancerId(id);
        return sessionPersistenceResource;
    }

    @Path("ssltermination")
    public SslTerminationResource retrieveSslResource() {
        sslTerminationResource.setAccountId(accountId);
        sslTerminationResource.setLoadBalancerId(id);
        return sslTerminationResource;
    }

    @Path("usage")
    public UsageResource retrieveUsageResource() {
        usageResource.setAccountId(accountId);
        usageResource.setLoadBalancerId(id);
        return usageResource;
    }

    @Path("virtualips")
    public VirtualIpsResource retrieveVirtualIpsResource() {
        virtualIpsResource.setRequestHeaders(requestHeaders);
        virtualIpsResource.setAccountId(accountId);
        virtualIpsResource.setLoadBalancerId(id);
        return virtualIpsResource;
    }

    private Response getFeedResponse(Integer page) {
        Map<String, Object> feedAttributes = new HashMap<String, Object>();
        feedAttributes.put("feedType", FeedType.LOADBALANCER_FEED);
        feedAttributes.put("accountId", accountId);
        feedAttributes.put("loadBalancerId", id);
        feedAttributes.put("page", page);
        Feed feed = atomFeedAdapter.getFeed(feedAttributes);

        if (feed.getEntries().isEmpty()) {
            try {
                loadBalancerService.get(id, accountId);
            } catch (Exception e) {
                return ResponseFactory.getErrorResponse(e, null, null);
            }
        }

        return Response.status(200).entity(feed).build();
    }

    public void setAccessListResource(AccessListResource accessListResource) {
        this.accessListResource = accessListResource;
    }

    public void setHealthMonitorResource(HealthMonitorResource healthMonitorResource) {
        this.healthMonitorResource = healthMonitorResource;
    }

    public void setLoadbalancerMetadataResource(LoadbalancerMetadataResource loadbalancerMetadataResource) {
        this.loadbalancerMetadataResource = loadbalancerMetadataResource;
    }

    public void setNodeMetadataResource(NodeMetadataResource nodeMetadataResource) {
        this.nodeMetadataResource = nodeMetadataResource;
    }

    public void setNodesResource(NodesResource nodesResource) {
        this.nodesResource = nodesResource;
    }

    public void setSessionPersistenceResource(SessionPersistenceResource sessionPersistenceResource) {
        this.sessionPersistenceResource = sessionPersistenceResource;
    }

    public void setConnectionThrottleResource(ConnectionThrottleResource connectionThrottleResource) {
        this.connectionThrottleResource = connectionThrottleResource;
    }

    public void setContentCachingResource(ContentCachingResource contentCachingResource) {
        this.contentCachingResource = contentCachingResource;
    }

    public void setVirtualIpsResource(VirtualIpsResource virtualIpsResource) {
        this.virtualIpsResource = virtualIpsResource;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public UsageResource getUsageResource() {
        return usageResource;
    }

    public void setUsageResource(UsageResource usageResource) {
        this.usageResource = usageResource;
    }

    public void setConnectionLoggingResource(ConnectionLoggingResource connectionLoggingResource) {
        this.connectionLoggingResource = connectionLoggingResource;
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void setErrorpageResource(ErrorpageResource errorpageResource) {
        this.errorpageResource = errorpageResource;
    }

    public SslTerminationResource getSslTerminationResource() {
        return sslTerminationResource;
    }

    public void setSslTerminationResource(SslTerminationResource sslTerminationResource) {
        this.sslTerminationResource = sslTerminationResource;
    }
}
