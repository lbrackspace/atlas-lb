package org.openstack.atlas.api.resource;

import org.apache.log4j.Logger;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.ConnectionThrottleValidator;
import org.openstack.atlas.core.api.v1.ConnectionThrottle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.*;

@Controller
@Scope("request")
public class ConnectionThrottleResource {
    private final Logger LOG = Logger.getLogger(ConnectionThrottleResource.class);
    private Integer accountId;
    private Integer loadBalancerId;

    @Autowired
    protected ConnectionThrottleValidator validator;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveConnectionThrottle() {
        try {
            // TODO: Implement
            return Response.status(Response.Status.OK).entity("Return something useful!").build();
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
            // TODO: Implement
            return Response.status(Response.Status.ACCEPTED).entity("Return something useful!").build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    @DELETE
    public Response deleteConnectionThrottle() {
        try {
            // TODO: Implement
            return Response.status(Response.Status.ACCEPTED).entity("Return something useful!").build();
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
