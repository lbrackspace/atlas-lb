package org.openstack.atlas.rax.api.resource;

import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.v1.extensions.rax.AccessList;
import org.openstack.atlas.api.v1.extensions.rax.NetworkItem;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.rax.api.validation.validator.AccessListValidator;
import org.openstack.atlas.rax.domain.entity.RaxAccessList;
import org.openstack.atlas.rax.domain.service.RaxAccessListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

@Primary
@Controller
@Scope("request")
public class RaxAccessListResource extends CommonDependencyProvider {

    @Autowired
    protected AccessListValidator validator;
    @Autowired
    protected RaxAccessListService accessListService;
    protected Integer accountId;
    protected Integer loadBalancerId;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Response retrieveAccessList(@QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit, @QueryParam("marker") Integer marker, @QueryParam("page") Integer page) {
        List<RaxAccessList> daccessList;
        AccessList raccessList = new AccessList();
        try {
            daccessList = accessListService.getAccessListByAccountIdLoadBalancerId(accountId, loadBalancerId, offset, limit, marker);
            for (RaxAccessList accessListItem : daccessList) {
                raccessList.getNetworkItems().add(dozerMapper.map(accessListItem, NetworkItem.class));
            }
            return Response.status(200).entity(raccessList).build();

        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @POST
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response createOrReplaceAccessList(AccessList _accessList) {

        ValidatorResult result = validator.validate(_accessList, HttpRequestType.POST);
        if (!result.passedValidation()) {
            return ResponseFactory.getValidationFaultResponse(result);
        }

        try {
            AccessList accessList = dozerMapper.map(_accessList, AccessList.class);

            // See if this throws unimplemented exception
//            asyncService.callAsyncLoadBalancingOperation(APPEND_TO_ACCESS_LIST, rLb);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

//    @DELETE
//    public Response deleteAccessList(@QueryParam("id") List<Integer> networkItemIds) throws EntityNotFoundException {
//        LoadBalancer returnLB = new LoadBalancer();
//        LoadBalancer domainLB = new LoadBalancer();
//        Integer size = accountLimitService.getLimit(accountId, AccountLimitType.BATCH_DELETE_LIMIT);

//        if (networkItemIds.size() == 0) {
//            try {
//                domainLB.setId(loadBalancerId);
//                domainLB.setAccountId(accountId);
//                if (requestHeaders != null) {
//                    domainLB.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));
//                }
//                loadBalancerService.get(loadBalancerId, accountId);
//                returnLB.setId(loadBalancerId);
//                returnLB.setAccountId(accountId);
//                returnLB = accessListService.markForDeletionAccessList(returnLB);
//                asyncService.callAsyncLoadBalancingOperation(Operation.DELETE_ACCESS_LIST, returnLB);
//            } catch (Exception badRequestException) {
//                badRequestException = new BadRequestException(String.format("Must supply one or more id's to process this request.", size));
//                return ResponseFactory.getErrorResponse(badRequestException, null, null);
//            }
//            return Response.status(Response.Status.ACCEPTED).build();
//        } else if (networkItemIds.size() <= size) {
//            try {
//                Set<org.openstack.atlas.service.domain.entities.RaxAccessList> accessLists = new HashSet<org.openstack.atlas.service.domain.entities.RaxAccessList>();
//                org.openstack.atlas.service.domain.entities.RaxAccessList aList = new org.openstack.atlas.service.domain.entities.RaxAccessList();
//                accessLists.add(aList);
//                returnLB.setId(loadBalancerId);
//                returnLB.setAccountId(accountId);
//                returnLB.setAccessLists(accessLists);
//                returnLB = accessListService.markForDeletionNetworkItems(returnLB, networkItemIds);
//                asyncService.callAsyncLoadBalancingOperation(Operation.APPEND_TO_ACCESS_LIST, returnLB);
//                return Response.status(Response.Status.ACCEPTED).build();
//            } catch (Exception e) {
//                return ResponseFactory.getErrorResponse(e, null, null);
//            }
//        } else {
//            Exception badRequestException = new BadRequestException(String.format("Currently, the limit of accepted parameters is: %s :please supply a valid parameter list.", size));
//            return ResponseFactory.getErrorResponse(badRequestException, null, null);
//        }
//    }

//    @Path("{id: [-+]?[0-9][0-9]*}")
//    public NetworkItemResource retrieveNetworkItemResource(@PathParam("id") int id) {
//        networkItemResource.setAccountId(accountId);
//        networkItemResource.setLoadBalancerId(loadBalancerId);
//        networkItemResource.setId(id);
//        return networkItemResource;
//    }

//    private Response getFeedResponse(Integer page) {
//        Map<String, Object> feedAttributes = new HashMap<String, Object>();
//        feedAttributes.put("feedType", FeedType.ACCESS_LIST_FEED);
//        feedAttributes.put("accountId", accountId);
//        feedAttributes.put("loadBalancerId", loadBalancerId);
//        feedAttributes.put("page", page);
//        Feed feed = atomFeedAdapter.getFeed(feedAttributes);

//        if (feed.getEntries().isEmpty()) {
//            try {
//                accessListService.getAccessListByAccountIdLoadBalancerId(accountId, loadBalancerId);
//            } catch (Exception e) {
//                return ResponseFactory.getErrorResponse(e, null, null);
//            }
//        }

//        return Response.status(200).entity(feed).build();
//    }

//    public void setNetworkItemResource(NetworkItemResource networkItemResource) {
//        this.networkItemResource = networkItemResource;
//    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

//    public void setRequestHeaders(HttpHeaders requestHeaders) {
//        this.requestHeaders = requestHeaders;
//    }
}
