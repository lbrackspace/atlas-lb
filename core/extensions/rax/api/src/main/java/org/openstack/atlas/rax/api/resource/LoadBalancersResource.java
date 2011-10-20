package org.openstack.atlas.rax.api.resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.rax.domain.pojo.RaxMessageDataContainer;
import org.openstack.atlas.service.domain.operation.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Controller("RAX-LoadBalancersResource")
@Scope("request")
public class LoadBalancersResource extends org.openstack.atlas.api.resource.LoadBalancersResource {
    public static Log LOG = LogFactory.getLog(LoadBalancersResource.class.getName());

    @Autowired
    @Qualifier("RAX-LoadBalancerResource")
    protected LoadBalancerResource loadBalancerResource;

    @Override
    public Response create(LoadBalancer _loadBalancer) {
        LOG.debug("loadbalancer: " + _loadBalancer);

        ValidatorResult result = validator.validate(_loadBalancer, HttpRequestType.POST);
        if (!result.passedValidation()) {
            return ResponseFactory.getValidationFaultResponse(result);
        }
        try {
            RaxLoadBalancer raxLoadBalancer = dozerMapper.map(_loadBalancer, RaxLoadBalancer.class);
            raxLoadBalancer.setAccountId(accountId);

            //This call should be moved somewhere else
            virtualIpService.addAccountRecord(accountId);

            org.openstack.atlas.service.domain.entity.LoadBalancer newlyCreatedLb = loadbalancerService.create(raxLoadBalancer);
            RaxMessageDataContainer msg = new RaxMessageDataContainer();
            msg.setLoadBalancer(newlyCreatedLb);
            asyncService.callAsyncLoadBalancingOperation(Operation.CREATE_LOADBALANCER, msg);
            return Response.status(Response.Status.ACCEPTED).entity(dozerMapper.map(newlyCreatedLb, LoadBalancer.class)).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @Path("{id: [-+]?[0-9][0-9]*}")
    public org.openstack.atlas.api.resource.LoadBalancerResource retrieveLoadBalancerResource(@PathParam("id") int id) {
        loadBalancerResource.setId(id);
        loadBalancerResource.setAccountId(accountId);
        return loadBalancerResource;
    }
}
