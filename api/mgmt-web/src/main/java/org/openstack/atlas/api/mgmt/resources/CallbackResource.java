package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.repository.ValidatorRepository;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ZeusEvent;

import javax.ws.rs.POST;
import javax.ws.rs.core.Response;

public class CallbackResource extends ManagementDependencyProvider {

    @POST
    public Response receiveCallbackMessage(ZeusEvent event) {
        try {
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

}
