package org.openstack.atlas.rax.api.resource;

import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.service.domain.operation.Operation;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.core.Response;

@Controller("RAX-LoadBalancerResource")
@Scope("request")
public class LoadBalancerResource extends org.openstack.atlas.api.resource.LoadBalancerResource {

    @Override
    public Response update(LoadBalancer _loadBalancer) {
        ValidatorResult result = validator.validate(_loadBalancer, HttpRequestType.PUT);

        if (!result.passedValidation()) {
            return ResponseFactory.getValidationFaultResponse(result);
        }

        try {
            RaxLoadBalancer raxLoadBalancer  = dozerMapper.map(_loadBalancer, RaxLoadBalancer.class);
            raxLoadBalancer.setId(id);
            raxLoadBalancer.setAccountId(accountId);

            loadBalancerService.update(raxLoadBalancer);

            MessageDataContainer msg = new MessageDataContainer();
            msg.setLoadBalancer(raxLoadBalancer);

            asyncService.callAsyncLoadBalancingOperation(Operation.UPDATE_LOADBALANCER, msg);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }
}
