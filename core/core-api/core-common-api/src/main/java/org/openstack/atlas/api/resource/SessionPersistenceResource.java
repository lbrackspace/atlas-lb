package org.openstack.atlas.api.resource;

import org.apache.log4j.Logger;
import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.SessionPersistenceValidator;
import org.openstack.atlas.core.api.v1.SessionPersistence;
import org.openstack.atlas.service.domain.operation.Operation;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
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
            SessionPersistence sessionPersistence = dozerMapper.map(service.get(accountId, loadBalancerId), SessionPersistence.class);
            if(sessionPersistence == null) return Response.status(Response.Status.NOT_FOUND).build();
            else return Response.status(Response.Status.OK).entity(sessionPersistence).build();
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

            org.openstack.atlas.service.domain.entity.LoadBalancer loadBalancer = new org.openstack.atlas.service.domain.entity.LoadBalancer();
            loadBalancer.setId(loadBalancerId);
            loadBalancer.setAccountId(accountId);

            service.delete(loadBalancer);

            MessageDataContainer data = new MessageDataContainer();
            data.setLoadBalancer(loadBalancer);

            asyncService.callAsyncLoadBalancingOperation(Operation.DISABLE_SESSION_PERSISTENCE, data);
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
