package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.events.entities.AlertStatus;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class AlertResource extends ManagementDependencyProvider {

    private int id;

    @GET
    public Response retrieveAlerts(@Context SecurityContext sc) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        try {
            Alert alert = alertService.getById(id);
            org.openstack.atlas.docs.loadbalancers.api.management.v1.Alert display = getDozerMapper().map(alert, org.openstack.atlas.docs.loadbalancers.api.management.v1.Alert.class, "MESSAGE_ALERT");
            return Response.status(Response.Status.OK).entity(display).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @PUT
    @Path("acknowledged")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateStatus() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }

        try {
            Alert dbAlert = notificationService.getAlert(id);
            dbAlert.setStatus(AlertStatus.ACKNOWLEDGED);
            notificationService.updateAlert(dbAlert);

            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
