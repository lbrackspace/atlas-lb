package org.openstack.atlas.api.faults;

import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.LoadBalancerFault;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.ValidationErrors;

import java.util.List;

public final class HttpResponseBuilder {

    public static BadRequest buildBadRequestResponse(String message, List<String> validationErrors) {
        BadRequest badRequest = new BadRequest();
        ValidationErrors errors = new ValidationErrors();

        errors.getMessages().addAll(validationErrors);
        badRequest.setDetails("The object is not valid");
        badRequest.setValidationErrors(errors);
        badRequest.setMessage(message);
        badRequest.setCode(400);

        return badRequest;
    }

    public static LoadBalancerFault buildLoadBalancerFault(Integer code, String message, String details) {
        LoadBalancerFault fault = new LoadBalancerFault();

        fault.setCode(code);
        fault.setMessage(message);
        fault.setDetails(details);

        return fault;
    }
}
