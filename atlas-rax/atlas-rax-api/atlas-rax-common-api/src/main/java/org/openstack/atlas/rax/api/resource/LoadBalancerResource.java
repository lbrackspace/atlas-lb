package org.openstack.atlas.rax.api.resource;

import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.core.api.v1.LoadBalancer;
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
            org.openstack.atlas.service.domain.entity.LoadBalancer loadBalancer  = dozerMapper.map(_loadBalancer, org.openstack.atlas.service.domain.entity.LoadBalancer.class);
            loadBalancer.setId(id);
            loadBalancer.setAccountId(accountId);

            loadBalancerService.update(loadBalancer);

            MessageDataContainer msg = new MessageDataContainer();
            msg.setLoadBalancer(loadBalancer);

            asyncService.callAsyncLoadBalancingOperation(Operation.UPDATE_LOADBALANCER, msg);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }
}
