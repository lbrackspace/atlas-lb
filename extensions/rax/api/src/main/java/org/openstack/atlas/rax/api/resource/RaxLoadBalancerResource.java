package org.openstack.atlas.rax.api.resource;

import org.openstack.atlas.api.resource.LoadBalancerResource;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.service.domain.operation.CoreOperation;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Primary
@Controller
@Scope("request")
public class RaxLoadBalancerResource extends LoadBalancerResource {

    @Autowired
    protected RaxAccessListResource raxAccessListResource;

    @Autowired
    protected RaxConnectionLoggingResource connectionLoggingResource;

    @Autowired
    protected RaxErrorPageResource raxErrorPageResource;

    @Override
    public Response get() {
        try {
            org.openstack.atlas.service.domain.entity.LoadBalancer loadBalancer = loadBalancerRepository.getByIdAndAccountId(id, accountId);
            LoadBalancer _loadBalancer = dozerMapper.map(loadBalancer, LoadBalancer.class);
            return Response.status(Response.Status.OK).entity(_loadBalancer).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }


    @Override
    public Response update(LoadBalancer loadBalancer) {
        ValidatorResult result = validator.validate(loadBalancer, HttpRequestType.PUT);

        if (!result.passedValidation()) {
            return ResponseFactory.getValidationFaultResponse(result);
        }

        try {
            RaxLoadBalancer raxLoadBalancer = dozerMapper.map(loadBalancer, RaxLoadBalancer.class);
            raxLoadBalancer.setId(id);
            raxLoadBalancer.setAccountId(accountId);

            loadBalancerService.update(raxLoadBalancer);

            MessageDataContainer msg = new MessageDataContainer();
            msg.setLoadBalancer(raxLoadBalancer);

            asyncService.callAsyncLoadBalancingOperation(CoreOperation.UPDATE_LOADBALANCER, msg);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @Path("accesslist")
    public RaxAccessListResource retrieveAccessListResource() {
        //accessListResource.setRequestHeaders(requestHeaders);
        raxAccessListResource.setAccountId(accountId);
        raxAccessListResource.setLoadBalancerId(id);
        return raxAccessListResource;
    }


    @Path("connectionlogging")
    public RaxConnectionLoggingResource retrieveConnectionLoggingResource() {
        connectionLoggingResource.setAccountId(accountId);
        connectionLoggingResource.setLoadBalancerId(id);
        return connectionLoggingResource;
    }

    @Path("errorpage")
    public RaxErrorPageResource retrieveErrorpageResource() {
        raxErrorPageResource.setAccountId(accountId);
        raxErrorPageResource.setLoadBalancerId(id);
        return raxErrorPageResource;
    }
}
