package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountLoadBalancerServiceEvents;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;

import javax.ws.rs.GET;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

public class EventResource extends ManagementDependencyProvider {

    @GET
    @Path("account/{id: [1-9][0-9]*}/loadbalancer")
    public Response getRecentLoadBalancerEvents(@PathParam("id") int accountId, @QueryParam("startDate") String startDate, @QueryParam("endDate") String endDate) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }


        org.openstack.atlas.service.domain.events.pojos.AccountLoadBalancerServiceEvents dEvents;
        AccountLoadBalancerServiceEvents rEvents = new AccountLoadBalancerServiceEvents();
        try {
            dEvents = getEventRepository().getRecentLoadBalancerServiceEvents(accountId, startDate, endDate);
            rEvents = getDozerMapper().map(dEvents, rEvents.getClass());
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
        return Response.status(200).entity(rEvents).build();
    }
}
