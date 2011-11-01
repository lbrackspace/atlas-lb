package org.openstack.atlas.api.faults;

import org.openstack.atlas.core.api.v1.exceptions.BadRequest;
import org.openstack.atlas.core.api.v1.exceptions.LoadBalancerException;
import org.openstack.atlas.core.api.v1.exceptions.ValidationErrors;

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

    public static LoadBalancerException buildLoadBalancerFault(Integer code, String message, String details) {
        LoadBalancerException fault = new LoadBalancerException();

        fault.setCode(code);
        fault.setMessage(message);
        fault.setDetails(details);

        return fault;
    }
}
