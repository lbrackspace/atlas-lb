package org.openstack.atlas.api.helpers;


import org.openstack.atlas.docs.loadbalancers.api.v1.faults.*;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.operations.OperationResponse.ErrorReason;

import javax.ws.rs.core.Response;

public class ResponseMapper {
    private static final String CONTACT_SUPPORT = "An unknown exception has occurred. Please contact support.";

    public static LbaasFault getFault(OperationResponse operationResponse, String message, String details) {
        return getFault(operationResponse.getErrorReason(), (message == null) ? operationResponse.getMessage() : message, details);
    }

    public static LbaasFault getFault(ErrorReason errorReason, String message, String details) {
        LoadBalancerFault lbf = new LoadBalancerFault();
        lbf.setMessage("");
        lbf.setDetails("");

        if (errorReason == null) errorReason = ErrorReason.UNKNOWN;

        switch (errorReason) {
            case ENTITY_NOT_FOUND:
                ItemNotFound inf = new ItemNotFound();
                inf.setDetails(details);
                inf.setMessage((message == null) ? "Object not Found" : message);
                inf.setCode(getStatus(ErrorReason.ENTITY_NOT_FOUND));
                return inf;
            case IMMUTABLE_ENTITY:
                ImmutableEntity ie = new ImmutableEntity();
                ie.setMessage((message == null) ? "The object at the specified URI is immutable and can not be overwritten." : message);
                ie.setDetails(details);
                return ie;
            case UNPROCESSABLE_ENTITY:
                UnProcessableEntity ue = new UnProcessableEntity();
                ue.setMessage((message == null) ? "The Object at the specified URI is unprocessable." : message);
                ue.setDetails(details);
                ue.setCode(getStatus(ErrorReason.UNPROCESSABLE_ENTITY));
                return ue;
            case OUT_OF_VIPS:
                OutOfVirtualIps oov = new OutOfVirtualIps();
                oov.setMessage((message == null) ? "Out of virtual IPs. Please contact support so they can allocate more virtual IPs." : message);
                oov.setDetails(details);
                return oov;
            case CLUSTER_STATUS:
                ClusterStatus cse = new ClusterStatus();
                cse.setMessage((message == null) ? "Cluster status is invalid. Please contact support." : message);
                cse.setDetails(details);
                return cse;
            case OVER_LIMIT:
                OverLimit olf = new OverLimit();
                olf.setMessage((message == null) ? "Your account is currently over the limit so your request could not be processed." : message);
                return olf;
            case SERVICE_UNAVAILABLE:
                ServiceUnavailable su = new ServiceUnavailable();
                su.setMessage((message == null) ? "The LoadBalancing API is currently not available" : message);
                su.setDetails(details);
                return su;
            case UNAUTHORIZED:
                Unauthorized uf = new Unauthorized();
                uf.setMessage((message == null) ? "You are not authorized to execute this operation." : message);
                uf.setDetails(details);
                return uf;
            case BAD_REQUEST:
                BadRequest badRequest = new BadRequest();
                badRequest.setMessage((message == null) ? "Bad request." : message);
                badRequest.setDetails(details);
                return badRequest;
            case UNKNOWN:
                lbf.setMessage((message == null) ? CONTACT_SUPPORT : message);
                lbf.setDetails(details);
                return lbf;
            case METHOD_NOT_ALLOWED:
                lbf.setMessage((message == null) ? "Method is not available." : message);
                lbf.setDetails(details);
                return lbf;
            default:
                lbf.setMessage((message == null) ? CONTACT_SUPPORT : message);
                lbf.setDetails(details);
                return lbf;
        }
    }

    public static LbaasFault getFault(Exception e, String message, String details) {
        if (message == null) {
            message = e.getMessage();
        }
        if (e instanceof EntityNotFoundException) {
            return getFault(ErrorReason.ENTITY_NOT_FOUND, message, details);
        } else if (e instanceof OutOfVipsException) {
            return getFault(ErrorReason.OUT_OF_VIPS, message, details);
        }else if (e instanceof ClusterStatusException) {
            return getFault(ErrorReason.CLUSTER_STATUS, message, details);
        } else if (e instanceof ServiceUnavailableException) {
            return getFault(ErrorReason.SERVICE_UNAVAILABLE, message, details);
        } else if (e instanceof SingletonEntityAlreadyExistsException) {
            return getFault(ErrorReason.IMMUTABLE_ENTITY, message, details);
        } else if (e instanceof ImmutableEntityException) {
            return getFault(ErrorReason.IMMUTABLE_ENTITY, message, details);
        } else if (e instanceof UnprocessableEntityException) {
            return getFault(ErrorReason.UNPROCESSABLE_ENTITY, message, details);
        } else if (e instanceof UnauthorizedException) {
            return getFault(ErrorReason.UNAUTHORIZED, message, details);
        } else if (e instanceof DeletedStatusException) {
            return getFault(ErrorReason.GONE, message, details);
        } else if (e instanceof BadRequestException) {
            return getFault(ErrorReason.BAD_REQUEST, message, details);
        } else if (e instanceof AccountMismatchException) {
            return getFault(ErrorReason.ENTITY_NOT_FOUND, message, details);
        } else if (e instanceof UniqueLbPortViolationException) {
            return getFault(ErrorReason.BAD_REQUEST, message, details);
        } else if (e instanceof LimitReachedException) {
            return getFault(ErrorReason.OVER_LIMIT, message, details);
        } else if (e instanceof ProtocolHealthMonitorMismatchException) {
            return getFault(ErrorReason.BAD_REQUEST, message, details);
        } else if (e instanceof TCPProtocolUnknownPortException) {
            return getFault(ErrorReason.BAD_REQUEST, message, details);
        } else if (e instanceof MethodNotAllowedException) {
            return getFault(ErrorReason.METHOD_NOT_ALLOWED, message, details);
        } else {
            LoadBalancerFault lbf = (LoadBalancerFault) getFault(OperationResponse.ErrorReason.UNKNOWN, null, null);
            lbf.setMessage(CONTACT_SUPPORT);
            lbf.setDetails(details);
            return lbf;
        }
    }

    public static Integer getStatus(Exception e) {
        Integer status;
        if (e == null) {
            status = 200;
        } else if (e instanceof EntityNotFoundException) {
            status = getStatus(ErrorReason.ENTITY_NOT_FOUND);
        } else if (e instanceof SingletonEntityAlreadyExistsException) {
            status = getStatus(ErrorReason.IMMUTABLE_ENTITY);
        } else if (e instanceof OutOfVipsException) {
            status = getStatus(ErrorReason.OUT_OF_VIPS);
        } else if (e instanceof ClusterStatusException) {
            status = getStatus(ErrorReason.CLUSTER_STATUS);
        } else if (e instanceof UnprocessableEntityException) {
            status = getStatus(ErrorReason.UNPROCESSABLE_ENTITY);
        } else if (e instanceof ImmutableEntityException) {
            status = getStatus(ErrorReason.IMMUTABLE_ENTITY);
        } else if (e instanceof ServiceUnavailableException) {
            status = getStatus(ErrorReason.SERVICE_UNAVAILABLE);
        } else if (e instanceof UnauthorizedException) {
            status = getStatus(ErrorReason.UNAUTHORIZED);
        } else if (e instanceof DeletedStatusException) {
            status = Response.Status.GONE.getStatusCode();
        } else if (e instanceof BadRequestException) {
            status = Response.Status.BAD_REQUEST.getStatusCode();
        } else if (e instanceof NullPointerException) {
            status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        } else if (e instanceof AccountMismatchException) {
            status = getStatus(ErrorReason.ENTITY_NOT_FOUND);
        } else if (e instanceof UniqueLbPortViolationException) {
            status = Response.Status.BAD_REQUEST.getStatusCode();
        } else if (e instanceof LimitReachedException) {
            status = getStatus(ErrorReason.OVER_LIMIT);
        } else if (e instanceof ProtocolHealthMonitorMismatchException) {
            status = getStatus(ErrorReason.BAD_REQUEST);
        } else if (e instanceof TCPProtocolUnknownPortException) {
            status = getStatus(ErrorReason.BAD_REQUEST);
        } else if (e instanceof MethodNotAllowedException) {
            status = getStatus(ErrorReason.METHOD_NOT_ALLOWED);
        } else {
            status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        }
        return status;
    }

    public static Integer getStatus(OperationResponse operationResponse) {
        if (operationResponse == null) {
            return 500;
        }
        return getStatus(operationResponse.getErrorReason());
    }

    public static Integer getStatus(ErrorReason errorReason) {
        Integer status;
        if (errorReason == null) errorReason = ErrorReason.UNKNOWN;

        switch (errorReason) {
            case OUT_OF_VIPS:
                status = 500;
                break;
            case CLUSTER_STATUS:
                status = 500;
                break;
            case SERVICE_UNAVAILABLE:
                status = 500;
                break;
            case OVER_LIMIT:
                status = 413;
                break;
            case UNPROCESSABLE_ENTITY:
                status = 422;
                break;
            case UNAUTHORIZED:
                status = 404;
                break;
            case UNKNOWN:
                status = 500;
                break;
            case ENTITY_NOT_FOUND:
                status = 404;
                break;
            case IMMUTABLE_ENTITY:
                status = 422;
                break;
            case BAD_REQUEST:
                status = 400;
                break;
            case GONE:
                status = 410;
                break;
            case METHOD_NOT_ALLOWED:
                status = 405;
                break;
            default:
                status = 500;
                break;
        }
        return status;
    }
}
