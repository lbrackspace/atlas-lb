package org.openstack.atlas.api.resource;

import org.apache.log4j.Logger;
import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.ResourceValidator;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.openstack.atlas.service.domain.operation.Operation;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.service.domain.service.LoadBalancerService;
import org.openstack.atlas.service.domain.service.VirtualIpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

@Controller("CORE-LoadBalancersResource")
@Scope("request")
public class LoadBalancersResource extends CommonDependencyProvider {
    private final Logger LOG = Logger.getLogger(LoadBalancersResource.class);
    private HttpHeaders requestHeaders;
    private Integer accountId;

    @Autowired
    private ResourceValidator<LoadBalancer> validator;
    @Autowired
    private LoadBalancerService loadbalancerService;
    @Autowired
    private VirtualIpService virtualIpService;

    @POST
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response createLoadBalancer(LoadBalancer loadBalancer) {
        ValidatorResult result = validator.validate(loadBalancer, HttpRequestType.POST);

        if (!result.passedValidation()) {
            return ResponseFactory.getValidationFaultResponse(result);
        }

        try {
            org.openstack.atlas.service.domain.entity.LoadBalancer mappedLb = dozerMapper.map(loadBalancer, org.openstack.atlas.service.domain.entity.LoadBalancer.class);
            mappedLb.setAccountId(accountId);
            virtualIpService.addAccountRecord(accountId);

            org.openstack.atlas.service.domain.entity.LoadBalancer newlyCreatedLb = loadbalancerService.create(mappedLb);
            MessageDataContainer msg = new MessageDataContainer();
            msg.setLoadBalancer(newlyCreatedLb);
            asyncService.callAsyncLoadBalancingOperation(Operation.CREATE_LOADBALANCER, msg);
            return Response.status(Response.Status.ACCEPTED).entity(dozerMapper.map(newlyCreatedLb, LoadBalancer.class)).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
}
