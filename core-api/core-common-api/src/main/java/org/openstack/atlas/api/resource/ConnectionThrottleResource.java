package org.openstack.atlas.api.resource;

import org.apache.log4j.Logger;
import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.ConnectionThrottleValidator;
import org.openstack.atlas.core.api.v1.ConnectionThrottle;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.operation.CoreOperation;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.service.domain.repository.ConnectionThrottleRepository;
import org.openstack.atlas.service.domain.service.ConnectionThrottleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.*;

@Controller
@Scope("request")
public class ConnectionThrottleResource extends CommonDependencyProvider {
    private final Logger LOG = Logger.getLogger(ConnectionThrottleResource.class);
    protected Integer accountId;
    protected Integer loadBalancerId;

    @Autowired
    protected ConnectionThrottleValidator validator;
    @Autowired
    protected ConnectionThrottleService connectionThrottleService;
    @Autowired
    protected ConnectionThrottleRepository repository;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveConnectionThrottle() {
        try {
            ConnectionThrottle connectionThrottle = dozerMapper.map(repository.getByLoadBalancerId(loadBalancerId), ConnectionThrottle.class);
            return Response.status(Response.Status.OK).entity(connectionThrottle).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response updateConnectionThrottle(ConnectionThrottle _connectionThrottle) {
        ValidatorResult result = validator.validate(_connectionThrottle, HttpRequestType.PUT);

        if (!result.passedValidation()) {
            return ResponseFactory.getValidationFaultResponse(result);
        }

        try {

            org.openstack.atlas.service.domain.entity.ConnectionThrottle connectionThrottle = dozerMapper.map(_connectionThrottle, org.openstack.atlas.service.domain.entity.ConnectionThrottle.class);
            connectionThrottle = connectionThrottleService.update(loadBalancerId, connectionThrottle);

            MessageDataContainer data = new MessageDataContainer();
            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setAccountId(accountId);
            loadBalancer.setId(loadBalancerId);
            loadBalancer.setConnectionThrottle(connectionThrottle);
            data.setLoadBalancer(loadBalancer);

            asyncService.callAsyncLoadBalancingOperation(CoreOperation.UPDATE_CONNECTION_THROTTLE, data);
            _connectionThrottle = dozerMapper.map(connectionThrottle, ConnectionThrottle.class);
            return Response.status(Response.Status.ACCEPTED).entity(_connectionThrottle).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    @DELETE
    public Response deleteConnectionThrottle() {
        try {
            MessageDataContainer data = new MessageDataContainer();
            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setAccountId(accountId);
            loadBalancer.setId(loadBalancerId);
            data.setLoadBalancer(loadBalancer);

            connectionThrottleService.preDelete(loadBalancerId);
            asyncService.callAsyncLoadBalancingOperation(CoreOperation.DELETE_CONNECTION_THROTTLE, data);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }
}
