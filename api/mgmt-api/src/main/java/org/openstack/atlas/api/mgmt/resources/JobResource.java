package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Job;
import org.openstack.atlas.service.domain.entities.JobState;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

public class JobResource extends ManagementDependencyProvider {
    private int id;

    public void setId(int id) {
        this.id = id;
    }

    @GET
    public Response getHost() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }

        try {
            JobState domainJob = jobStateService.getById(id);
            return Response.status(200).entity(getDozerMapper().map(domainJob, Job.class)).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }
}
