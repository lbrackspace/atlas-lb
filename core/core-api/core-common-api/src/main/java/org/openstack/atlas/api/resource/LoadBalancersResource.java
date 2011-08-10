package org.openstack.atlas.api.resource;

import org.apache.log4j.Logger;
import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.ResourceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

@Controller
@Scope("request")
public class LoadBalancersResource extends CommonDependencyProvider {
    private final Logger LOG = Logger.getLogger(LoadBalancersResource.class);
    private HttpHeaders requestHeaders;
    private Integer accountId;
    @Autowired
    private ResourceValidator<LoadBalancer> validator;

    @POST
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response createLoadBalancer(LoadBalancer loadBalancer) {
        ValidatorResult result = validator.validate(loadBalancer, HttpRequestType.POST);

        if (!result.passedValidation()) {
            return ResponseFactory.getValidationFaultResponse(result);
        }

        try {
            org.openstack.atlas.service.domain.entity.LoadBalancer domainLb = dozerMapper.map(loadBalancer, org.openstack.atlas.service.domain.entity.LoadBalancer.class);
            LoadBalancer apiLb = dozerMapper.map(domainLb, LoadBalancer.class);
            return Response.status(200).entity(apiLb).build();
/*            domainLb.setAccountId(accountId);
            if (requestHeaders != null) {
                domainLb.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));
            }

            virtualIpService.addAccountRecord(accountId);
            org.openstack.atlas.service.domain.entities.LoadBalancer returnLb = loadBalancerService.create(domainLb);
            asyncService.callAsyncLoadBalancingOperation(CREATE_LOADBALANCER, returnLb);
            return Response.status(Response.Status.ACCEPTED).entity(dozerMapper.map(returnLb, LoadBalancer.class)).build();*/
        } catch (Exception e) {
            e.printStackTrace();
            /*return ResponseFactory.getErrorResponse(e, null, null);*/
        }
        return Response.status(200).entity("FAIL").build();
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
}
