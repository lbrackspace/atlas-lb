package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.repository.ValidatorRepository;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ZeusEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.POST;
import javax.ws.rs.core.Response;

public class CallbackResource extends ManagementDependencyProvider {

    private final Log LOG = LogFactory.getLog(CallbackResource.class);

    @POST
    public Response receiveCallbackMessage(ZeusEvent event) {
        try {
            LOG.info(logZeusEvent(event));

            ValidatorResult result = ValidatorRepository.getValidatorFor(ZeusEvent.class).validate(event, HttpRequestType.POST);


            if (!result.passedValidation()) {
                return Response.status(400).entity(HttpResponseBuilder.buildBadRequestResponse("Validation fault", result.getValidationErrorMessages())).build();
            }

            org.openstack.atlas.service.domain.pojos.ZeusEvent zeusEvent = getDozerMapper().map(event, org.openstack.atlas.service.domain.pojos.ZeusEvent.class);
            callbackService.handleZeusEvent(zeusEvent);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    private String logZeusEvent(ZeusEvent ze) {
        StringBuilder sb = new StringBuilder();
        sb.append("ZeusEvent callback recieved:");
        if (ze == null) {
            sb.append("null");
            return sb.toString();
        }
        sb = sb.append("{").
                append(" host=\"").append(ze.getCallbackHost()).append("\"").
                append(" eventType=\"").append(ze.getEventType()).append("\"").
                append(" paramLine=\"").append(ze.getParamLine()).append("\"").
                append("}");
        return sb.toString();
    }
}
