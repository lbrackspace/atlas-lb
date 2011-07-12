package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.service.domain.entities.GroupRateLimit;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

public class GroupsResource extends ManagementDependencyProvider {

    private GroupResource groupResource;
    private int id;


    @Path("{id: [1-9][0-9]*}")
    public GroupResource retrieveGroupResource(@PathParam("id") int id) {
        groupResource.setId(id);
        return groupResource;
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML})
    public Response addGroups(@QueryParam("name") String name, @QueryParam("default") String defaultValue) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }

        GroupRateLimit grp = new GroupRateLimit();
        grp.setName(name);
        grp.setDescription(name);
        if (defaultValue != null && defaultValue.equalsIgnoreCase("Y")) {
            grp.setDefault(true);
        } else {
            grp.setDefault(false);
        }

        try {
            groupService.createGroup(grp);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }

    }

    @GET
    public Response retrieveGroups() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }


        List<GroupRateLimit> domainGroupRateLimits;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.GroupRateLimits lts = new org.openstack.atlas.docs.loadbalancers.api.management.v1.GroupRateLimits();

        try {
            domainGroupRateLimits = groupRepository.getAll();
            for (org.openstack.atlas.service.domain.entities.GroupRateLimit domain : domainGroupRateLimits) {
                lts.getGroupRateLimits().add(getDozerMapper().map(domain, org.openstack.atlas.docs.loadbalancers.api.management.v1.GroupRateLimit.class, "SIMPLE_LIMIT"));
            }
            return Response.status(200).entity(lts).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }


    public GroupResource getGroupResource() {
        return groupResource;
    }

    public void setGroupResource(GroupResource groupResource) {
        this.groupResource = groupResource;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
