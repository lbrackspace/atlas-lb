package org.openstack.atlas.api.resources;

import org.openstack.atlas.docs.loadbalancers.api.v1.AccessList;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItem;
import org.openstack.atlas.service.domain.entities.AccountLimitType;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
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
import java.util.*;

import static org.openstack.atlas.service.domain.operations.Operation.APPEND_TO_ACCESS_LIST;
import static javax.ws.rs.core.MediaType.*;

public class AccessListResource extends CommonDependencyProvider {

    private NetworkItemResource networkItemResource;
    private Integer accountId;
    private Integer loadBalancerId;
    private HttpHeaders requestHeaders;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveAccessList(@QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit, @QueryParam("marker") Integer marker, @QueryParam("page") Integer page) {
        if (requestHeaders.getRequestHeader("Accept").get(0).equals(APPLICATION_ATOM_XML)) {
            return getFeedResponse(page);
        }

        List<org.openstack.atlas.service.domain.entities.AccessList> daccessList;
        org.openstack.atlas.docs.loadbalancers.api.v1.AccessList raccessList = new org.openstack.atlas.docs.loadbalancers.api.v1.AccessList();
        NetworkItem ni;
        try {

            daccessList = accessListService.getAccessListByAccountIdLoadBalancerId(accountId, loadBalancerId, offset, limit, marker);
            for (org.openstack.atlas.service.domain.entities.AccessList accessListItem : daccessList) {
                raccessList.getNetworkItems().add(dozerMapper.map(accessListItem, NetworkItem.class));
            }
            return Response.status(200).entity(raccessList).build();

        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @POST
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response createOrReplaceAccessList(AccessList accessList) {
        ValidatorResult result = ValidatorRepository.getValidatorFor(AccessList.class).validate(accessList, HttpRequestType.POST);
        if (!result.passedValidation()) {
            return getValidationFaultResponse(result);
        }

        try {        
            LoadBalancer rLb = dozerMapper.map(accessList, LoadBalancer.class);
            rLb.setId(loadBalancerId);
            rLb.setAccountId(accountId);
            if (requestHeaders != null) {
                rLb.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));
            }
            // Insert Database stuff Service layer stuff here

            rLb = accessListService.updateAccessList(rLb);

            // See if this throws unimplemented exception
            asyncService.callAsyncLoadBalancingOperation(APPEND_TO_ACCESS_LIST, rLb);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    public Response deleteAccessList(@QueryParam("id") List<Integer> networkItemIds) throws EntityNotFoundException {
        LoadBalancer returnLB = new LoadBalancer();
        LoadBalancer domainLB = new LoadBalancer();
        Integer size = accountLimitService.getLimit(accountId, AccountLimitType.BATCH_DELETE_LIMIT);

        if (networkItemIds.size() == 0) {
            try {
                domainLB.setId(loadBalancerId);
                domainLB.setAccountId(accountId);
                if (requestHeaders != null) {
                    domainLB.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));
                }
                loadBalancerService.get(loadBalancerId, accountId);
                returnLB.setId(loadBalancerId);
                returnLB.setAccountId(accountId);
                returnLB = accessListService.markForDeletionAccessList(returnLB);
                asyncService.callAsyncLoadBalancingOperation(Operation.DELETE_ACCESS_LIST, returnLB);
            } catch (Exception badRequestException) {
                badRequestException = new BadRequestException(String.format("Must supply one or more id's to process this request.", size));
                return ResponseFactory.getErrorResponse(badRequestException, null, null);
            }
            return Response.status(Response.Status.ACCEPTED).build();
        } else if (networkItemIds.size() <= size) {
            try {
                Set<org.openstack.atlas.service.domain.entities.AccessList> accessLists = new HashSet<org.openstack.atlas.service.domain.entities.AccessList>();
                org.openstack.atlas.service.domain.entities.AccessList aList = new org.openstack.atlas.service.domain.entities.AccessList();
                accessLists.add(aList);
                returnLB.setId(loadBalancerId);
                returnLB.setAccountId(accountId);
                returnLB.setAccessLists(accessLists);
                returnLB = accessListService.markForDeletionNetworkItems(returnLB, networkItemIds);
                asyncService.callAsyncLoadBalancingOperation(Operation.APPEND_TO_ACCESS_LIST, returnLB);
                return Response.status(Response.Status.ACCEPTED).build();
            } catch (Exception e) {
                return ResponseFactory.getErrorResponse(e, null, null);
            }
        } else {
            Exception badRequestException = new BadRequestException(String.format("Currently, the limit of accepted parameters is: %s :please supply a valid parameter list.", size));
            return ResponseFactory.getErrorResponse(badRequestException, null, null);
        }
    }

    @Path("{id: [-+]?[0-9][0-9]*}")
    public NetworkItemResource retrieveNetworkItemResource(@PathParam("id") int id) {
        networkItemResource.setAccountId(accountId);
        networkItemResource.setLoadBalancerId(loadBalancerId);
        networkItemResource.setId(id);
        return networkItemResource;
    }

    private Response getFeedResponse(Integer page) {
        Map<String, Object> feedAttributes = new HashMap<String, Object>();
        feedAttributes.put("feedType", FeedType.ACCESS_LIST_FEED);
        feedAttributes.put("accountId", accountId);
        feedAttributes.put("loadBalancerId", loadBalancerId);
        feedAttributes.put("page", page);
        Feed feed = atomFeedAdapter.getFeed(feedAttributes);

        if (feed.getEntries().isEmpty()) {
            try {
                accessListService.getAccessListByAccountIdLoadBalancerId(accountId, loadBalancerId);
            } catch (Exception e) {
                return ResponseFactory.getErrorResponse(e, null, null);
            }
        }

        return Response.status(200).entity(feed).build();
    }

    public void setNetworkItemResource(NetworkItemResource networkItemResource) {
        this.networkItemResource = networkItemResource;
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
