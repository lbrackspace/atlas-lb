package org.openstack.atlas.rax.api.resource;

import org.apache.log4j.Logger;
import org.openstack.atlas.api.resource.ConnectionThrottleResource;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.core.api.v1.ConnectionThrottle;
import org.openstack.atlas.rax.domain.entity.RaxConnectionThrottle;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.operation.Operation;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

@Primary
@Controller
@Scope("request")
public class RaxConnectionThrottleResource extends ConnectionThrottleResource {
    private final Logger LOG = Logger.getLogger(RaxConnectionThrottleResource.class);

    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response updateConnectionThrottle(ConnectionThrottle _connectionThrottle) {
        ValidatorResult result = validator.validate(_connectionThrottle, HttpRequestType.PUT);

        if (!result.passedValidation()) {
            return ResponseFactory.getValidationFaultResponse(result);
        }

        try {
            RaxConnectionThrottle raxConnectionThrottle = dozerMapper.map(_connectionThrottle, RaxConnectionThrottle.class);

            org.openstack.atlas.service.domain.entity.ConnectionThrottle connectionThrottle = connectionThrottleService.update(loadBalancerId, raxConnectionThrottle);

            MessageDataContainer data = new MessageDataContainer();
            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setAccountId(accountId);
            loadBalancer.setId(loadBalancerId);
            loadBalancer.setConnectionThrottle(connectionThrottle);
            data.setLoadBalancer(loadBalancer);

            asyncService.callAsyncLoadBalancingOperation(Operation.SET_CONNECTION_THROTTLE, data);
            _connectionThrottle = dozerMapper.map(connectionThrottle, ConnectionThrottle.class);
            return Response.status(Response.Status.ACCEPTED).entity(_connectionThrottle).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }
}
