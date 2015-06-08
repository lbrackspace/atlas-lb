package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.ValidationErrors;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;

import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.PENDING_UPDATE;

public class ChangeHostResource extends ManagementDependencyProvider {

    private static final String SSLTERMBREAK = "This operation will result in this Load Balancer entering ERROR status, as the sslTermination is invalid. Consider deleting the sslTermination on this LB before attempting to change hosts.";
    private static final String BADHOST = "Invalid newHostId supplied.";
    private static final String SAMEHOST = "The supplied newHostId is the same as the Load Balancer's existing HostID. No action will be performed.";
    private static final ZeusUtils zeusUtils;
    private int loadBalancerId;

    static {
        zeusUtils = new ZeusUtils();
    }

    @PUT
    public Response changeHost(@QueryParam("newHostId") Integer newHostId) {
        if (!isUserInRole("ops")) {
            return ResponseFactory.accessDenied();
        }

        try {
            //create requestObject
            MessageDataContainer mdc = new MessageDataContainer();
            mdc.setLoadBalancerId(loadBalancerId);

            Host newHost;
            try {
                newHost = hostService.getById(newHostId);
                mdc.setMoveHost(newHost);
            } catch (EntityNotFoundException hnfe) {
                BadRequestException bre = new BadRequestException(BADHOST);
                return ResponseFactory.getErrorResponse(bre, null, null);
            }

            LoadBalancer lb = loadBalancerService.get(loadBalancerId);
            if (lb.getHost().getId().equals(newHost.getId())) {
                BadRequestException bre = new BadRequestException(BADHOST);
                return ResponseFactory.getErrorResponse(bre, null, null);
            }

            mdc.setAccountId(lb.getAccountId());
            mdc.setLoadBalancerStatus(lb.getStatus());
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
                BadRequestException bre = new BadRequestException("Cannot Move a Suspended Load Balancer, Please Check With Operations For Further Information...");
                return ResponseFactory.getErrorResponse(bre, null, null);
            }
            loadBalancerService.setStatus(lb, PENDING_UPDATE);
            getManagementAsyncService().callAsyncLoadBalancingOperation(Operation.CHANGE_HOST, mdc);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public void setLoadBalancerId(int id) {
        this.loadBalancerId = id;
    }
}
