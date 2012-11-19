package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.SyncLocation;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;

import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;

public class SyncResource extends ManagementDependencyProvider {

    private int loadBalancerId;

    @PUT
    public Response sync() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }

        try {
            org.openstack.atlas.service.domain.pojos.Sync domainSyncObject = new org.openstack.atlas.service.domain.pojos.Sync();
            domainSyncObject.setLoadBalancerId(loadBalancerId);
            domainSyncObject.setLocationToSyncFrom(SyncLocation.DATABASE);

            //create requestObject
            EsbRequest req = new EsbRequest();
            req.setSyncObject(domainSyncObject);
         /*   OperationResponse response = getManagementEsbService().callLoadBalancingOperation(Operation.SYNC, req);
            if (response.isExecutedOkay()) {
                return Response.status(Response.Status.ACCEPTED).build();
            } else {
                return ResponseFactory.getErrorResponse(response);
            }   */


            LoadBalancer lb = loadBalancerService.get(loadBalancerId);
            if (lb.getStatus().equals(LoadBalancerStatus.SUSPENDED)) {
                BadRequestException bre = new BadRequestException("Cannot Sync a Suspended Load Balancer, Please Check With Operations For Further Information...");
                return ResponseFactory.getErrorResponse(bre, null, null);
            }
            getManagementAsyncService().callAsyncLoadBalancingOperation(Operation.SYNC, req);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public void setLoadBalancerId(int id) {
        this.loadBalancerId = id;
    }
}
