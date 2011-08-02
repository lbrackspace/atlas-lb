package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.LBDeviceEvent;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeStatus;
import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;

import javax.ws.rs.POST;
import javax.ws.rs.core.Response;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.UPDATE_NODE;

public class CallbackResource extends ManagementDependencyProvider {

    @POST
    public Response receiveCallbackMessage(LBDeviceEvent event) {
        try {
            org.openstack.atlas.service.domain.pojos.LBDeviceEvent lbDeviceEvent = getDozerMapper().map(event, org.openstack.atlas.service.domain.pojos.LBDeviceEvent.class);
            callbackService.handleLBDeviceEvent(lbDeviceEvent);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

}
