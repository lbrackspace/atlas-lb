package org.openstack.atlas.rax.api.resource;

import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.v1.extensions.rax.ConnectionLogging;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.rax.api.validation.validator.RaxConnectionLoggingValidator;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.rax.domain.service.RaxConnectionLoggingService;
import org.openstack.atlas.service.domain.operation.Operation;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.LoadBalancerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

@Primary
@Controller
@Scope("request")
public class RaxConnectionLoggingResource extends CommonDependencyProvider {
    @Autowired
    protected RaxConnectionLoggingValidator validator;

    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;

    @Autowired
    protected RaxConnectionLoggingService raxConnectionLoggingService;

    protected Integer accountId;
    protected Integer loadBalancerId;

    @GET
    public Response get() {
        try {
            RaxLoadBalancer loadBalancer = (RaxLoadBalancer) loadBalancerRepository.getByIdAndAccountId(loadBalancerId, accountId);
            ConnectionLogging connectionLogging = new ConnectionLogging();
            connectionLogging.setEnabled(loadBalancer.getConnectionLogging());
            return Response.status(200).entity(connectionLogging).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response update(ConnectionLogging _connectionLogging) {
        ValidatorResult result = validator.validate(_connectionLogging, HttpRequestType.PUT);

        if (!result.passedValidation()) {
            return ResponseFactory.getValidationFaultResponse(result);
        }
        try {

            MessageDataContainer data = new MessageDataContainer();
            RaxLoadBalancer loadBalancer = new RaxLoadBalancer();
            loadBalancer.setAccountId(accountId);
            loadBalancer.setId(loadBalancerId);
            loadBalancer.setConnectionLogging(_connectionLogging.isEnabled());
            data.setLoadBalancer(loadBalancer);

            raxConnectionLoggingService.update(loadBalancer);
            asyncService.callAsyncLoadBalancingOperation(Operation.UPDATE_CONNECTION_LOGGING, data);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    public Integer getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
}
