package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.helpers.PaginationHelper;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancersStatusHistory;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatusHistory;
import org.w3.atom.Link;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

public class SaveStateHistoryResource extends ManagementDependencyProvider {
    private int id;

    @GET
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getHost(@QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit, @QueryParam("marker") Integer marker, @QueryParam("page") Integer page) {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }

        org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerStatusHistory rlb = null;
        LoadBalancersStatusHistory loadBalancersStatusHistory = new LoadBalancersStatusHistory();
        try {
            limit = PaginationHelper.determinePageLimit(limit);
            offset = PaginationHelper.determinePageOffset(offset);

            List<LoadBalancerStatusHistory> stateHistory = loadBalancerStatusHistoryRepository.getStateHistoryForAccount(id, offset, limit, marker);
            if (stateHistory != null) {
                for (LoadBalancerStatusHistory lbsh : stateHistory) {
                    rlb = getDozerMapper().map(lbsh, org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerStatusHistory.class);
                    loadBalancersStatusHistory.getLoadBalancerStatusHistories().add(rlb);
                }
            }
            if (loadBalancersStatusHistory.getLoadBalancerStatusHistories().size() > limit) {
                String relativeUri = String.format("/management/loadbalancers/%d?offset=%d&limit=%d", id, PaginationHelper.calculateNextOffset(offset, limit), limit);
                Link nextLink = PaginationHelper.createLink(PaginationHelper.NEXT, relativeUri);
                loadBalancersStatusHistory.getLinks().add(nextLink);
                loadBalancersStatusHistory.getLoadBalancerStatusHistories().remove(limit.intValue()); // Remove limit+1 item
            }

            if (offset > 0) {
                String relativeUri = String.format("/management/loadbalancers/%d?offset=%d&limit=%d", id, PaginationHelper.calculatePreviousOffset(offset, limit), limit);
                Link nextLink = PaginationHelper.createLink(PaginationHelper.PREVIOUS, relativeUri);
                loadBalancersStatusHistory.getLinks().add(nextLink);
            }
            return Response.status(200).entity(loadBalancersStatusHistory).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public void setId(int id) {
        this.id = id;
    }
}
