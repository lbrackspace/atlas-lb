package org.openstack.atlas.rax.api.resource;

import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.v1.extensions.rax.AccessList;
import org.openstack.atlas.api.v1.extensions.rax.NetworkItem;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.rax.api.validation.validator.AccessListValidator;
import org.openstack.atlas.rax.domain.entity.RaxAccessList;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.rax.domain.operation.RaxOperation;
import org.openstack.atlas.rax.domain.repository.RaxAccessListRepository;
import org.openstack.atlas.rax.domain.service.RaxAccessListService;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
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
    @Autowired
    protected RaxAccessListRepository accessListRepository;
    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;

    protected Integer accountId;
    protected Integer loadBalancerId;

    @Autowired
    private RaxNetworkItemResource networkItemResource;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Response list(@QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit, @QueryParam("marker") Integer marker, @QueryParam("page") Integer page) {
        AccessList _accessList = new AccessList();
        try {
            List<RaxAccessList> accessList = accessListRepository.getAccessListByAccountIdLoadBalancerId(accountId, loadBalancerId, offset, limit, marker);
            for (RaxAccessList accessListItem : accessList) {
                _accessList.getNetworkItems().add(dozerMapper.map(accessListItem, NetworkItem.class));
            }
            return Response.status(200).entity(_accessList).build();

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
            RaxLoadBalancer loadBalancer = new RaxLoadBalancer();
            loadBalancer.setAccountId(accountId);
            loadBalancer.setId(loadBalancerId);

            for (NetworkItem _networkItem : _accessList.getNetworkItems()) {
                RaxAccessList raxAccessList = dozerMapper.map(_networkItem, RaxAccessList.class);
                loadBalancer.getAccessLists().add(raxAccessList);
            }


            loadBalancer = (RaxLoadBalancer) accessListService.updateAccessList(loadBalancer);

            MessageDataContainer data = new MessageDataContainer();
            data.setLoadBalancer(loadBalancer);

            asyncService.callAsyncLoadBalancingOperation(RaxOperation.UPDATE_ACCESS_LIST, data);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    public Response deleteAccessList(@QueryParam("id") List<Integer> networkItemIds) {
        try {

            RaxLoadBalancer loadBalancer = new RaxLoadBalancer();
            loadBalancer.setAccountId(accountId);
            loadBalancer.setId(loadBalancerId);

            MessageDataContainer data = new MessageDataContainer();

            if (networkItemIds.size() == 0) {
                loadBalancer = (RaxLoadBalancer) accessListService.markAccessListForDeletion(accountId, loadBalancerId);
                data.setLoadBalancer(loadBalancer);
                asyncService.callAsyncLoadBalancingOperation(RaxOperation.DELETE_ACCESS_LIST, data);
            } else {
                loadBalancer = (RaxLoadBalancer) accessListService.markNetworkItemsForDeletion(accountId, loadBalancerId, networkItemIds);
                data.setLoadBalancer(loadBalancer);
                asyncService.callAsyncLoadBalancingOperation(RaxOperation.UPDATE_ACCESS_LIST, data);
            }

            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
        /*LoadBalancer returnLB = new LoadBalancer();
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
                asyncService.callAsyncLoadBalancingOperation(RaxOperation.DELETE_ACCESS_LIST, returnLB);
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
                asyncService.callAsyncLoadBalancingOperation(RaxOperation.APPEND_TO_ACCESS_LIST, returnLB);
                return Response.status(Response.Status.ACCEPTED).build();
            } catch (Exception e) {
                return ResponseFactory.getErrorResponse(e, null, null);
            }
        } else {
            Exception badRequestException = new BadRequestException(String.format("Currently, the limit of accepted parameters is: %s :please supply a valid parameter list.", size));
            return ResponseFactory.getErrorResponse(badRequestException, null, null);
        }*/
    }

    @Path("{id: [-+]?[0-9][0-9]*}")
    public RaxNetworkItemResource retrieveNetworkItemResource(@PathParam("id") int id) {
        networkItemResource.setAccountId(accountId);
        networkItemResource.setLoadBalancerId(loadBalancerId);
        networkItemResource.setId(id);
        return networkItemResource;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

}
