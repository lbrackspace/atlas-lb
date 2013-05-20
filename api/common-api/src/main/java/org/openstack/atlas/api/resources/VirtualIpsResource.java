package org.openstack.atlas.api.resources;

import org.openstack.atlas.docs.loadbalancers.api.v1.IpVersion;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.service.domain.entities.AccountLimitType;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.entities.VirtualIpv6;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.api.atom.FeedType;
import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.validation.context.VirtualIpContext;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.apache.abdera.model.Feed;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

import static org.openstack.atlas.api.filters.helpers.StringUtilities.getExtendedStackTrace;
import static javax.ws.rs.core.MediaType.*;

public class VirtualIpsResource extends CommonDependencyProvider {

    private VirtualIpResource virtualIpResource;
    private Integer accountId;
    private Integer loadBalancerId;
    private HttpHeaders requestHeaders;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveAllVirtualIps(@QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit, @QueryParam("marker") Integer marker, @QueryParam("page") Integer page) {
        if (requestHeaders.getRequestHeader("Accept").get(0).equals(APPLICATION_ATOM_XML)) {
            return getFeedResponse(page);
        }

        org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIps rvips = new org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIps();

        try {
            loadBalancerService.get(loadBalancerId, accountId);

            try {
                Set<VirtualIp> ipv4Vips = virtualIpService.get(accountId, loadBalancerId, offset, limit, marker);
                for (org.openstack.atlas.service.domain.entities.VirtualIp ipv4Vip : ipv4Vips) {
                    org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp apiIpv4Vip = dozerMapper.map(ipv4Vip, org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp.class);
                    apiIpv4Vip.setIpVersion(IpVersion.IPV4);
                    rvips.getVirtualIps().add(apiIpv4Vip);
                }
            } catch (EntityNotFoundException enfe) {
                // Ignore since we still need to check for IPv6 vips.
            }

            Set<VirtualIpv6> ipv6Vips = virtualIpService.getVirtualIpv6ByLoadBalancerId(loadBalancerId);
            for (org.openstack.atlas.service.domain.entities.VirtualIpv6 ipv6Vip : ipv6Vips) {
                org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp apiIpv6Vip;
                apiIpv6Vip = new org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp();
                apiIpv6Vip.setId(ipv6Vip.getId());
                apiIpv6Vip.setType(VipType.PUBLIC);
                apiIpv6Vip.setIpVersion(IpVersion.IPV6);
                apiIpv6Vip.setAddress(virtualIpService.getVirtualIpv6String(ipv6Vip));
                rvips.getVirtualIps().add(apiIpv6Vip);
            }
            
            return Response.status(200).entity(rvips).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addIpv6VirtualIpToLoadBalancer(org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp vip) {
        ValidatorResult result = ValidatorRepository.getValidatorFor(org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp.class).validate(vip, VirtualIpContext.POST_IPV6);

        if (!result.passedValidation()) {
            return Response.status(400).entity(HttpResponseBuilder.buildBadRequestResponse("Validation fault", result.getValidationErrorMessages())).build();
        }

        try {
            VirtualIpv6 domainVirtualIpv6 = new VirtualIpv6();
            domainVirtualIpv6.setId(vip.getId());
            domainVirtualIpv6.setAccountId(accountId);

            LoadBalancer domainLb = new LoadBalancer();
            domainLb.setId(loadBalancerId);
            domainLb.setAccountId(accountId);

            VirtualIpv6 newlyAddedIpv6Vip = virtualIpService.addIpv6VirtualIpToLoadBalancer(domainVirtualIpv6, domainLb);

            MessageDataContainer dataContainer = new MessageDataContainer();
            dataContainer.setAccountId(accountId);
            dataContainer.setLoadBalancerId(loadBalancerId);
            dataContainer.getNewVipIds().add(newlyAddedIpv6Vip.getId());
            if (requestHeaders != null) dataContainer.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));

            asyncService.callAsyncLoadBalancingOperation(Operation.ADD_VIRTUAL_IP, dataContainer);

            org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp returnVip = new org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp();
            returnVip.setId(newlyAddedIpv6Vip.getId());
            returnVip.setType(VipType.PUBLIC);
            returnVip.setIpVersion(IpVersion.IPV6);
            returnVip.setAddress(newlyAddedIpv6Vip.getDerivedIpString());

            return Response.status(Response.Status.ACCEPTED).entity(returnVip).build();
        } catch (Exception e) {
            String msg = getExtendedStackTrace(e);
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response deleteVirtualIps(@QueryParam("id") Set<Integer> virtualIpIds) {
        try {
            if (virtualIpIds.isEmpty()) {
                BadRequestException badRequestException = new BadRequestException("Must supply one or more id's to process this request.");
                return ResponseFactory.getErrorResponse(badRequestException, null, null);
            }

            Integer limit = accountLimitService.getLimit(accountId, AccountLimitType.BATCH_DELETE_LIMIT);
            if (virtualIpIds.size() > limit) {
                BadRequestException badRequestException = new BadRequestException(String.format("Currently, the limit of accepted parameters is: %s : please supply a valid parameter list.", limit));
                return ResponseFactory.getErrorResponse(badRequestException, null, null);
            }

            List<Integer> vipIds = new ArrayList<Integer>(virtualIpIds);
            virtualIpService.prepareForVirtualIpsDeletion(accountId, loadBalancerId, vipIds);

            MessageDataContainer messageDataContainer = new MessageDataContainer();
            messageDataContainer.setIds(vipIds);
            messageDataContainer.setAccountId(accountId);
            messageDataContainer.setLoadBalancerId(loadBalancerId);
            asyncService.callAsyncLoadBalancingOperation(Operation.DELETE_VIRTUAL_IPS, messageDataContainer);
            return Response.status(202).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }


    @Path("{id: [-+]?[0-9][0-9]*}")
    public VirtualIpResource retrieveVirtualIpResource(@PathParam("id") int virtualIpId) {
        virtualIpResource.setRequestHeaders(requestHeaders);
        virtualIpResource.setLoadBalancerId(loadBalancerId);
        virtualIpResource.setId(virtualIpId);
        virtualIpResource.setAccountId(accountId);
        return virtualIpResource;
    }

    private Response getFeedResponse(Integer page) {
        Map<String, Object> feedAttributes = new HashMap<String, Object>();
        feedAttributes.put("feedType", FeedType.VIRTUAL_IPS_FEED);
        feedAttributes.put("accountId", accountId);
        feedAttributes.put("loadBalancerId", loadBalancerId);
        feedAttributes.put("page", page);
        Feed feed = atomFeedAdapter.getFeed(feedAttributes);

        if (feed.getEntries().isEmpty()) {
            try {
                lbRepository.getVipsByLoadBalancerId(loadBalancerId);
            } catch (Exception e) {
                return ResponseFactory.getErrorResponse(e, null, null);
            }
        }

        return Response.status(200).entity(feed).build();
    }

    public void setVirtualIpResource(VirtualIpResource virtualIpResource) {
        this.virtualIpResource = virtualIpResource;
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
