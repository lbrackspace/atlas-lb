package org.openstack.atlas.rax.api.resource;

import org.apache.log4j.Logger;
import org.openstack.atlas.api.resource.HealthMonitorResource;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.core.api.v1.HealthMonitor;
import org.openstack.atlas.rax.domain.entity.RaxHealthMonitor;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.operation.CoreOperation;
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
public class RaxHealthMonitorResource extends HealthMonitorResource {
    private final Logger LOG = Logger.getLogger(RaxHealthMonitorResource.class);

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

            RaxHealthMonitor raxHealthMonitor = dozerMapper.map(_healthMonitor, RaxHealthMonitor.class);
            org.openstack.atlas.service.domain.entity.HealthMonitor healthMonitor = service.update(loadBalancerId, raxHealthMonitor);
            asyncService.callAsyncLoadBalancingOperation(CoreOperation.UPDATE_HEALTH_MONITOR, data);
            _healthMonitor = dozerMapper.map(healthMonitor, HealthMonitor.class);
            return Response.status(Response.Status.ACCEPTED).entity(_healthMonitor).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }
}
