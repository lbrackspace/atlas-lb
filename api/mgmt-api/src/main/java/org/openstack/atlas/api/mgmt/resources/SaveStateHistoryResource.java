package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancersStatusHistory;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatusHistory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

public class SaveStateHistoryResource extends ManagementDependencyProvider {
    private int id;

    @GET
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getHost() {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }

        org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerStatusHistory rlb = null;
        LoadBalancersStatusHistory loadBalancersStatusHistory = new LoadBalancersStatusHistory();
        try {
            List<LoadBalancerStatusHistory> stateHistory = loadBalancerStatusHistoryRepository.getStateHistoryForAccount(id);
            if (stateHistory != null) {
                for (LoadBalancerStatusHistory lbsh : stateHistory) {
                    rlb = getDozerMapper().map(lbsh, org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerStatusHistory.class);
                    loadBalancersStatusHistory.getLoadBalancerStatusHistories().add(rlb);
                }
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
