package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.SyncLocation;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;

import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.ValidationErrors;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;

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
            org.openstack.atlas.service.domain.pojos.Sync domainSyncObject = new org.openstack.atlas.service.domain.pojos.Sync();
            domainSyncObject.setLoadBalancerId(loadBalancerId);
            domainSyncObject.setLocationToSyncFrom(SyncLocation.DATABASE);

            //create requestObject
            EsbRequest req = new EsbRequest();
            req.setSyncObject(domainSyncObject);

            LoadBalancer lb = loadBalancerService.get(loadBalancerId);
            SslTermination sslTerm = lb.getSslTermination();
            if (sslTerm != null) {
                // Verify sslTerm won't break the LB during sync attempt
                String crt = sslTerm.getCertificate();
                String key = sslTerm.getPrivatekey();
                String imd = sslTerm.getIntermediateCertificate();
                ZeusCrtFile zcf = zeusUtils.buildZeusCrtFileLbassValidation(key, crt, imd);
                if (zcf.hasFatalErrors()) {
                    BadRequest sslFault = new BadRequest();
                    sslFault.setValidationErrors(new ValidationErrors());
                    sslFault.getValidationErrors().getMessages().add(SSLTERMBREAK); // Complain about SSL borkage
                    sslFault.getValidationErrors().getMessages().addAll(zcf.getFatalErrorList());
                    return Response.status(Response.Status.BAD_REQUEST).entity(sslFault).build();
                }
            }
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
