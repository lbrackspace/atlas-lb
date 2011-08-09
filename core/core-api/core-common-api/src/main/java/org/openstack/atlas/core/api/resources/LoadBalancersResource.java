package org.openstack.atlas.core.api.resources;

import org.apache.log4j.Logger;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

@Controller
@Scope(value = "request")
public class LoadBalancersResource {
    private final Logger LOG = Logger.getLogger(LoadBalancersResource.class);
    private HttpHeaders requestHeaders;
    private Integer accountId;

    @POST
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response createLoadBalancer(LoadBalancer loadBalancer) {
        LOG.info("We created a loadbalancer!");
        LOG.info(loadBalancer);
        System.out.print("We created an lb");
        System.out.print(loadBalancer);
/*        ValidatorResult result = ValidatorRepository.getValidatorFor(LoadBalancer.class).validate(loadBalancer, HttpRequestType.POST);

        if (!result.passedValidation()) {
            return getValidationFaultResponse(result);
        }

        try {
            org.openstack.atlas.service.domain.entities.LoadBalancer domainLb = dozerMapper.map(loadBalancer, org.openstack.atlas.service.domain.entities.LoadBalancer.class);
            domainLb.setAccountId(accountId);
            if (requestHeaders != null) {
                domainLb.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));
            }

            virtualIpService.addAccountRecord(accountId);
            org.openstack.atlas.service.domain.entities.LoadBalancer returnLb = loadBalancerService.create(domainLb);
            asyncService.callAsyncLoadBalancingOperation(CREATE_LOADBALANCER, returnLb);
            return Response.status(Response.Status.ACCEPTED).entity(dozerMapper.map(returnLb, LoadBalancer.class)).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }*/
        return Response.status(200).entity(loadBalancer).build();
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
}
