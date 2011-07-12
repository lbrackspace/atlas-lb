package org.openstack.atlas.api.resources;

import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.service.domain.operations.OperationResponse.ErrorReason;
import org.openstack.atlas.api.faults.HttpResponseBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.*;


public class ThrowResource extends CommonDependencyProvider{
    @GET
    @Path("badRequest")
    public Response getBadRequest() {
        List<String> validationMessages = new ArrayList<String>();
        validationMessages.add("Example Message1");
        validationMessages.add("Example Message2");
        BadRequest badRequest = HttpResponseBuilder.buildBadRequestResponse("Validation fault", validationMessages);
        return Response.status(400).entity(badRequest).build();
    }

    @GET
    @Path("loadBalancerFault")
    public Response getLoadBalancerFault() {
        OperationResponse opResp = new OperationResponse();
        opResp.setErrorReason(ErrorReason.UNKNOWN);
        return ResponseFactory.getErrorResponse(opResp);

    }

    @GET
    @Path("ItemNotFound")
    public Response getItemNotFound() {
        OperationResponse opResp = new OperationResponse();
        opResp.setErrorReason(ErrorReason.ENTITY_NOT_FOUND);
        return ResponseFactory.getErrorResponse(opResp);
    }

    @GET
    @Path("OverLimit")
    public Response getOverLimit() {
        OperationResponse opResp = new OperationResponse();
        opResp.setErrorReason(ErrorReason.OVER_LIMIT);
        return ResponseFactory.getErrorResponse(opResp);
    }

    @GET
    @Path("Unauthorized")
    public Response getUnauthorized() {
        OperationResponse opResp = new OperationResponse();
        opResp.setErrorReason(ErrorReason.UNAUTHORIZED);
        return ResponseFactory.getErrorResponse(opResp);
    }

    @GET
    @Path("OutOfVirtualIps")
    public Response getOutOfVirtualIps() {
        OperationResponse opResp = new OperationResponse();
        opResp.setErrorReason(ErrorReason.OUT_OF_VIPS);
        return ResponseFactory.getErrorResponse(opResp);
    }

    @GET
    @Path("ImmutableEntity")
    public Response getImmutableEntity() {
        OperationResponse opResp = new OperationResponse();
        opResp.setErrorReason(ErrorReason.IMMUTABLE_ENTITY);
        return ResponseFactory.getErrorResponse(opResp);
    }


    @GET
    @Path("UnprocessableEntity")
    public Response getUnprocessableEntity() {
        OperationResponse opResp = new OperationResponse();
        opResp.setErrorReason(ErrorReason.UNPROCESSABLE_ENTITY);
        return ResponseFactory.getErrorResponse(opResp);
    }

    @GET
    @Path("ServiceUnavailable")
    public Response getServiceUnavailable() {
        OperationResponse opResp = new OperationResponse();
        opResp.setErrorReason(ErrorReason.SERVICE_UNAVAILABLE);
        return ResponseFactory.getErrorResponse(opResp);
    }
}
