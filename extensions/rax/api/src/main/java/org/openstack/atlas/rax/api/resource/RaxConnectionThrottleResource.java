package org.openstack.atlas.rax.api.resource;

import org.apache.log4j.Logger;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.core.api.v1.ConnectionThrottle;
import org.openstack.atlas.rax.api.mapper.dozer.converter.ExtensionObjectMapper;
import org.openstack.atlas.rax.domain.entity.RaxConnectionThrottle;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.operation.Operation;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.*;

@Primary
@Controller
@Scope("request")
public class RaxConnectionThrottleResource extends org.openstack.atlas.api.resource.ConnectionThrottleResource {
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

            //refactor to use dozer xml or api?
            String minConnections = ExtensionObjectMapper.getOtherAttribute(_connectionThrottle.getOtherAttributes(), "minConnections");
            String maxConnections = ExtensionObjectMapper.getOtherAttribute(_connectionThrottle.getOtherAttributes(), "maxConnections");
            raxConnectionThrottle.setMinConnections(Integer.parseInt(minConnections));
            raxConnectionThrottle.setMaxConnections(Integer.parseInt(maxConnections));

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
