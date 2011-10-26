package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Job;
import org.openstack.atlas.service.domain.entities.JobState;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

public class JobsResource extends ManagementDependencyProvider {
    private JobResource jobResource;

    public void setJobResource(JobResource jobResource) {
        this.jobResource = jobResource;
    }

    @Path("{id: [1-9][0-9]*}")
    public JobResource retrieveJobResource(@PathParam("id") int id) {
        jobResource.setId(id);
        return jobResource;
    }

    @GET
    public Response retrieveJobs(@QueryParam("state") String state, @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit, @QueryParam("marker") Integer marker) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }

        List<JobState> domainJobs;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.Jobs dataModelJobs = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Jobs();

        try {
            if(state == null || state.equals("")) domainJobs = jobStateService.getAll(offset, limit, marker);
            else domainJobs = jobStateService.getByState(state, offset, limit);

            for (JobState domainJob : domainJobs) {
                dataModelJobs.getJobs().add(getDozerMapper().map(domainJob, Job.class));
            }
            return Response.status(200).entity(dataModelJobs).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }
}
