package org.openstack.atlas.api.resource;

import org.apache.log4j.Logger;
import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.SessionPersistenceValidator;
import org.openstack.atlas.core.api.v1.SessionPersistence;
import org.openstack.atlas.service.domain.service.SessionPersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.*;

@Controller
@Scope("request")
public class SessionPersistenceResource extends CommonDependencyProvider {
    private final Logger LOG = Logger.getLogger(SessionPersistenceResource.class);
    private Integer accountId;
    private Integer loadBalancerId;

    @Autowired
    protected SessionPersistenceValidator validator;
    @Autowired
    protected SessionPersistenceService service;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveSessionPersistence() {
        try {
            SessionPersistence sessionPersistence = dozerMapper.map(service.get(loadBalancerId), SessionPersistence.class);
            return Response.status(Response.Status.OK).entity(sessionPersistence).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response updateSessionPersistence(SessionPersistence _sessionPersistence) {
        ValidatorResult result = validator.validate(_sessionPersistence, HttpRequestType.PUT);

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
    public Response deleteSessionPersistence() {
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
