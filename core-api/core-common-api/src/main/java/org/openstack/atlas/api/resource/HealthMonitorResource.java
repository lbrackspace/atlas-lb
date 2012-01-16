package org.openstack.atlas.api.resource;

import org.apache.log4j.Logger;
import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.HealthMonitorValidator;
import org.openstack.atlas.core.api.v1.HealthMonitor;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.operation.CoreOperation;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.service.domain.repository.HealthMonitorRepository;
import org.openstack.atlas.service.domain.service.HealthMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.*;

@Controller
@Scope("request")
public class HealthMonitorResource extends CommonDependencyProvider {
    private final Logger LOG = Logger.getLogger(HealthMonitorResource.class);
    protected Integer accountId;
    protected Integer loadBalancerId;

    @Autowired
    protected HealthMonitorValidator validator;
    @Autowired
    protected HealthMonitorService service;
    @Autowired
    protected HealthMonitorRepository repository;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveHealthMonitor() {
        try {
            HealthMonitor healthMonitor = dozerMapper.map(repository.getByLoadBalancerId(loadBalancerId), HealthMonitor.class);
            return Response.status(Response.Status.OK).entity(healthMonitor).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response updateHealthMonitor(HealthMonitor _healthMonitor) {
        ValidatorResult result = validator.validate(_healthMonitor, HttpRequestType.PUT);

        if (!result.passedValidation()) {
            return ResponseFactory.getValidationFaultResponse(result);
        }

        try {
            MessageDataContainer data = new MessageDataContainer();
            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setAccountId(accountId);
            loadBalancer.setId(loadBalancerId);
            data.setLoadBalancer(loadBalancer);

            org.openstack.atlas.service.domain.entity.HealthMonitor healthMonitor = dozerMapper.map(_healthMonitor, org.openstack.atlas.service.domain.entity.HealthMonitor.class);
            healthMonitor = service.update(loadBalancerId, healthMonitor);
            asyncService.callAsyncLoadBalancingOperation(CoreOperation.UPDATE_HEALTH_MONITOR, data);
            _healthMonitor = dozerMapper.map(healthMonitor, HealthMonitor.class);
            return Response.status(Response.Status.ACCEPTED).entity(_healthMonitor).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    @DELETE
    public Response deleteHealthMonitor() {
        try {
            MessageDataContainer data = new MessageDataContainer();
            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setAccountId(accountId);
            loadBalancer.setId(loadBalancerId);
            data.setLoadBalancer(loadBalancer);

            service.preDelete(loadBalancerId);
            asyncService.callAsyncLoadBalancingOperation(CoreOperation.DELETE_HEALTH_MONITOR, data);
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
