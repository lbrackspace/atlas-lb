package org.openstack.atlas.api.mgmt.resources;


import org.openstack.atlas.service.domain.entities.GroupRateLimit;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class GroupResource extends ManagementDependencyProvider {

    int id;

    @POST
    @Path("accounts/{id: [-+]?[0-9][0-9]*}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createAccountGroupRateLimit(@PathParam("id") int accountId) {
        try {
            org.openstack.atlas.service.domain.entities.AccountGroup dGroup = new org.openstack.atlas.service.domain.entities.AccountGroup();

            dGroup.setAccountId(accountId);
            dGroup.setGroupRateLimit(new GroupRateLimit());
            dGroup.getGroupRateLimit().setId(id);

            groupService.insertAccountGroup(dGroup);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @PUT
    @Path("setdefault")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateGroup(@QueryParam("default") String defaultValue) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }

        GroupRateLimit lbLimitGroup = new GroupRateLimit();
        lbLimitGroup.setId(id);

        if (defaultValue != null && defaultValue.equalsIgnoreCase("Y")) {
            lbLimitGroup.setDefault(true);
        } else {
            lbLimitGroup.setDefault(false);
        }

        try {

            groupService.updateGroup(lbLimitGroup);
            return Response.status(Response.Status.ACCEPTED).build();

        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    public Response deleteGroup() {
        try {
            GroupRateLimit addLimitGroup = new GroupRateLimit();
            addLimitGroup.setId(id);

            groupService.deleteGroup(addLimitGroup);
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

