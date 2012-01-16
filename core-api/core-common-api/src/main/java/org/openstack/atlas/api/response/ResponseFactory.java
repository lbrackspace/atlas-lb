package org.openstack.atlas.api.response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.core.api.v1.exceptions.BadRequest;
import org.openstack.atlas.core.api.v1.exceptions.LbaasException;
import org.openstack.atlas.core.api.v1.exceptions.LoadBalancerException;
import org.openstack.atlas.core.api.v1.exceptions.ValidationErrors;
import org.openstack.atlas.api.validation.result.ValidatorResult;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class ResponseFactory {
    private static final Log LOG = LogFactory.getLog(ResponseFactory.class);
    protected final static String VALIDATION_FAILURE = "Validation Failure";

    public static Response getValidationFaultResponse(ValidatorResult result) {
        List<String> vmessages = result.getValidationErrorMessages();
        int status = 400;
        BadRequest badreq = buildBadRequestResponse(VALIDATION_FAILURE, vmessages);
        Response vresp = Response.status(status).entity(badreq).build();
        return vresp;
    }

    public static Response getValidationFaultResponse(List<String> messages) {
        int status = 400;
        BadRequest badreq = buildBadRequestResponse(VALIDATION_FAILURE, messages);
        Response vresp = Response.status(status).entity(badreq).build();
        return vresp;
    }

    public static Response getValidationFaultResponse(String errorStr){
        List<String> errorStrs = new ArrayList<String>();
        errorStrs.add(errorStr);
        return getValidationFaultResponse(errorStrs);
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

    public static Response getErrorResponse(Exception e) {
        return getErrorResponse(e, null, null);
    }

    public static Response getErrorResponse(Exception e, String message, String detail) {
        String errMsg;
        LbaasException lbaasException = ResponseMapper.getFault(e, message, detail);
        Integer code = ResponseMapper.getStatus(e);
        lbaasException.setCode(code);

        if (code == 500) {
            errMsg = String.format("Exception Caught: %s", getExtendedStackTrace(e));
            LOG.debug(errMsg);
        }

        return Response.status(code).entity(lbaasException).build();
    }

    private static String getExtendedStackTrace(Throwable ti) {
        Throwable t;
        StringBuffer sb;
        Exception currEx;
        String msg;

        sb = new StringBuffer();
        t = ti;
        while (t != null) {
            if (t instanceof Exception) {
                currEx = (Exception) t;
                msg = String.format("%s\n", getStackTrace(currEx));
                sb.append(msg);
                t = t.getCause();
            }
        }
        return sb.toString();
    }

    private static String getStackTrace(Exception ex) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("Exception: %s:%s\n", ex.getMessage(), ex.getClass().getName()));
        for (StackTraceElement se : ex.getStackTrace()) {
            sb.append(String.format("%s\n", se.toString()));
        }
        return sb.toString();
    }
}
