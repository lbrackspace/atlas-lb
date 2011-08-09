package org.openstack.atlas.api.response;

import org.openstack.atlas.core.api.v1.exceptions.BadRequest;
import org.openstack.atlas.core.api.v1.exceptions.LoadBalancerException;
import org.openstack.atlas.core.api.v1.exceptions.ValidationErrors;
import org.openstack.atlas.api.validation.result.ValidatorResult;

import javax.ws.rs.core.Response;
import java.util.List;

public class ResponseFactory {
    protected final static String VALIDATION_FAILURE = "Validation Failure";

    public static Response getValidationFaultResponse(ValidatorResult result) {
        List<String> vmessages = result.getValidationErrorMessages();
        int status = 400;
        BadRequest badreq = buildBadRequestResponse(VALIDATION_FAILURE, vmessages);
        Response vresp = Response.status(status).entity(badreq).build();
        return vresp;
    }

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
