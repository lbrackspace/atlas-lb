package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;

import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.ValidationErrors;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;

import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.PENDING_UPDATE;

public class SyncResource extends ManagementDependencyProvider {

    private static final String SSLTERMBREAK = "SyncCall will result in this loadbalancer going into error status as the sslTermination is invalid. Consider deleting the ssltermination on this Lb before attempting to sync.";
    private static final ZeusUtils zeusUtils;
    private int loadBalancerId;

    static {
        zeusUtils = new ZeusUtils();
    }

    @PUT
    public Response sync() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }

        try {
            //create requestObject
            MessageDataContainer mdc = new MessageDataContainer();
            mdc.setLoadBalancerId(loadBalancerId);

            LoadBalancer lb = loadBalancerService.get(loadBalancerId);
            mdc.setAccountId(lb.getAccountId());
            mdc.setLoadBalancerStatus(lb.getStatus());
            SslTermination sslTerm = lb.getSslTermination();

            // Validate private key and related credentials, store encrypted keys in the case db has yet to be updated
            // These will throw BadRequestExceptions if keys fail to validate or store
            if (sslTerm != null) {
                sslTerminationService.validatePrivateKey(loadBalancerId, lb.getAccountId(), sslTerm, true);
            }
            certificateMappingService.validatePrivateKeys(lb, true);

            if (lb.getStatus().equals(LoadBalancerStatus.SUSPENDED)) {
                BadRequestException bre = new BadRequestException("Cannot Sync a Suspended Load Balancer, Please Check With Operations For Further Information...");
                return ResponseFactory.getErrorResponse(bre, null, null);
            }
            loadBalancerService.setStatus(lb, PENDING_UPDATE);
            getManagementAsyncService().callAsyncLoadBalancingOperation(Operation.SYNC, mdc);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public void setLoadBalancerId(int id) {
        this.loadBalancerId = id;
    }
}
