package org.openstack.atlas.api.helpers;

import java.util.ArrayList;
import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import java.util.List;
import org.openstack.atlas.docs.loadbalancers.api.v1.Operationsuccess;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.GeneralFault;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.LbaasFault;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.openstack.atlas.api.filters.helpers.StringUtilities.getExtendedStackTrace;

public class ResponseFactory {

    private static final String VFAIL = "Validation Failure";
    private static final Log LOG = LogFactory.getLog(ResponseFactory.class);

    public static Response getSuccessfulResponse(String message) {
        return getSuccessResponse(message, 200);
    }


    public static Response getResponseWithStatus(Response.Status status, String message) {
        LbaasFault lbaasFault = new GeneralFault();
        lbaasFault.setCode(status.getStatusCode());
        lbaasFault.setMessage(message);
        return Response.status(status).entity(lbaasFault).build();
    }

    public static Response getResponseWithStatus(int  status, String message) {
        LbaasFault lbaasFault = new GeneralFault();
        lbaasFault.setCode(status);
        lbaasFault.setMessage(message);
        return Response.status(status).entity(lbaasFault).build();
    }

    public static Response getResponseWithStatus(int status, String message, String details) {
        LbaasFault lbaasFault = new GeneralFault();
        lbaasFault.setCode(status);
        lbaasFault.setMessage(message);
        lbaasFault.setDetails(details);
        return Response.status(status).entity(lbaasFault).build();
    }


    public static Response getResponseWithStatus(Response.Status status, String message, String details) {
        LbaasFault lbaasFault = new GeneralFault();
        lbaasFault.setCode(status.getStatusCode());
        lbaasFault.setMessage(message);
        lbaasFault.setDetails(details);
        return Response.status(status).entity(lbaasFault).build();
    }

    public static String getInternalServerErrorMessage() {
        return "Oopsie! Something happened and we are fanatically trying to resolve it.";
    }

    public static Response getSuccessResponse(String msg, int status) {
        Operationsuccess opResp = new Operationsuccess();
        opResp.setMessage(msg);
        opResp.setStatus(status);
        Response resp = Response.status(status).entity(opResp).build();
        return resp;
    }

    public static Response getErrorResponse(Exception e, String message, String detail) {
        String errMsg;
        LbaasFault lbaasFault = ResponseMapper.getFault(e, message, detail);
        Integer code = ResponseMapper.getStatus(e);
        lbaasFault.setCode(code);

        if (code == 500) {
            errMsg = String.format("Exception Caught: %s", getExtendedStackTrace(e));
            LOG.debug(errMsg);
        }

        return Response.status(code).entity(lbaasFault).build();
    }

    public static Response getErrorResponse(OperationResponse operationResponse) {
        if (!operationResponse.isExecutedOkay()) {
            LbaasFault lbaasFault = ResponseMapper.getFault(operationResponse.getErrorReason(), operationResponse.getMessage(), null);
            Integer code = ResponseMapper.getStatus(operationResponse);
            lbaasFault.setCode(code);
            return Response.status(code).entity(lbaasFault).build();
        } else {
            throw new RuntimeException("Error no Error found yet getErrorResponse called");
        }
    }

    public static Response accessDenied() {
        Response resp = ResponseFactory.getResponseWithStatus(Status.FORBIDDEN, "Access Denied");
        return resp;
    }

    public static Response getValidationFaultResponse(String errorStr) {
        List<String> errorStrs = new ArrayList<String>();
        errorStrs.add(errorStr);
        return getValidationFaultResponse(errorStrs);
    }

    public static Response getValidationFaultResponse(List<String> errorStrs) {
        BadRequest badreq;
        int status = 400;
        badreq = HttpResponseBuilder.buildBadRequestResponse(VFAIL, errorStrs);
        Response resp = Response.status(status).entity(badreq).build();
        return resp;
    }
}
