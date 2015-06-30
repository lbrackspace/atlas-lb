package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.ValidationErrors;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip6;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.entities.VirtualIpv6;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;

import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.ACTIVE;
import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.PENDING_UPDATE;

public class ChangeHostResource extends ManagementDependencyProvider {

    private static final String SSLTERMBREAK = "This operation will result in this Load Balancer (or another LB sharing a VIP)  entering ERROR status, as the sslTermination is invalid. Consider deleting the sslTermination on this LB before attempting to change hosts.";
    private static final String NOMOVESUSPENDED = "This Load Balancer (or another LB sharing VIP) is Suspended, Please Check With Operations For Further Information...";
    private static final String BADHOST = "Invalid newHostId supplied.";
    private static final String BADCLUSTER = "Invalid newClusterId supplied, or unable to find a valid host for the supplied cluster.";
    private static final String SAMEHOST = "The supplied newHostId is the same as the Load Balancer's existing HostID. No action will be performed.";
    private static final String SHAREDLOCKFAIL = "This Load Balancer uses a shared VIP, and a lock could not be established on all LBs sharing that VIP. No action will be performed.";
    private static final ZeusUtils zeusUtils;
    private int loadBalancerId;

    static {
        zeusUtils = new ZeusUtils();
    }

    @PUT
    public Response changeHost(@QueryParam("newHostId") Integer newHostId, @QueryParam("newClusterId") Integer newClusterId) {
        if (!isUserInRole("ops")) {
            return ResponseFactory.accessDenied();
        }

        try {
            //create requestObject
            MessageDataContainer mdc = new MessageDataContainer();

            Host newHost;
            try {
                // Pick the specified host, or calculate one from a specified cluster
                newHost = (newHostId != null) ? hostService.getById(newHostId) : hostRepository.getDefaultActiveHost(newClusterId);
                mdc.setMoveHost(newHost);
            } catch (EntityNotFoundException hnfe) {
                BadRequestException bre = new BadRequestException((newHostId != null) ? BADHOST : BADCLUSTER);
                return ResponseFactory.getErrorResponse(bre, null, null);
            }

            LoadBalancer lb = loadBalancerService.get(loadBalancerId);
            if (lb.getHost().getId().equals(newHost.getId())) {
                BadRequestException bre = new BadRequestException(SAMEHOST);
                return ResponseFactory.getErrorResponse(bre, null, null);
            }

            Map<Integer, LoadBalancer> LBsToMove = new HashMap<Integer, LoadBalancer>();
            LBsToMove.put(lb.getId(), lb);

            // Get any additional LBs that share any IPs
            for (LoadBalancerJoinVip joinVip : lb.getLoadBalancerJoinVipSet()) {
                VirtualIp virtualIp = joinVip.getVirtualIp();
                if (virtualIpService.isVipAllocatedToMultipleLoadBalancers(virtualIp)) {
                    for (LoadBalancer extraLB : virtualIpService.getLoadBalancerByVipId(virtualIp.getId())) {
                        if (!LBsToMove.containsKey(extraLB.getId())) {
                            LBsToMove.put(extraLB.getId(), extraLB);
                        }
                    }
                }
            }
            for (LoadBalancerJoinVip6 joinVip : lb.getLoadBalancerJoinVip6Set()) {
                VirtualIpv6 virtualIp = joinVip.getVirtualIp();
                if (virtualIpService.isIpv6VipAllocatedToAnotherLoadBalancer(lb, virtualIp)) {
                    for (LoadBalancer extraLB : virtualIpService.getLoadBalancerByVip6Id(virtualIp.getId())) {
                        if (!LBsToMove.containsKey(extraLB.getId())) {
                            LBsToMove.put(extraLB.getId(), extraLB);
                        }
                    }
                }
            }

            // Verify good SSLTerm and not SUSPENDED status for all LBs
            for (LoadBalancer lbToMove : LBsToMove.values()) {
                SslTermination sslTerm = lbToMove.getSslTermination();
                // Verify sslTerm won't break the LB during the move attempt
                if (sslTerm != null) {
                    String crt = sslTerm.getCertificate();
                    String key = sslTerm.getPrivatekey();
                    String imd = sslTerm.getIntermediateCertificate();
                    ZeusCrtFile zcf = zeusUtils.buildZeusCrtFileLbassValidation(key, crt, imd);
                    if (zcf.hasFatalErrors()) {
                        BadRequest sslFault = new BadRequest();
                        sslFault.setValidationErrors(new ValidationErrors());
                        sslFault.getValidationErrors().getMessages().add(SSLTERMBREAK);
                        sslFault.getValidationErrors().getMessages().add("SSL Termination broken for LB #" + lbToMove.getId());
                        sslFault.getValidationErrors().getMessages().addAll(zcf.getFatalErrorList());
                        return Response.status(Response.Status.BAD_REQUEST).entity(sslFault).build();
                    }
                }
                // Don't try to move suspended LBs, because the process will activate them (this is fixable...)
                if (lbToMove.getStatus().equals(LoadBalancerStatus.SUSPENDED)) {
                    BadRequest suspendedFail = new BadRequest();
                    suspendedFail.setValidationErrors(new ValidationErrors());
                    suspendedFail.getValidationErrors().getMessages().add(NOMOVESUSPENDED);
                    suspendedFail.getValidationErrors().getMessages().add("Will not attempt to move Suspended LB #" + lbToMove.getId());
                    return Response.status(Response.Status.BAD_REQUEST).entity(suspendedFail).build();
                }
            }

            List<LoadBalancer> pendingLBs = new ArrayList<LoadBalancer>();
            List<Integer> lbIds = new ArrayList<Integer>();
            // Try to get PENDING lock for all shared LBs
            Integer lastLockAttempt = -1;
            for (LoadBalancer lbToMove : LBsToMove.values()) {
                lastLockAttempt = lbToMove.getId();
                if (loadBalancerService.testAndSetStatus(lbToMove, PENDING_UPDATE)) {
                    pendingLBs.add(lbToMove);
                    lbIds.add(lbToMove.getId());
                } else {
                    // We failed to get a lock on one of the LBs, might as well give up now
                    break;
                }
            }

            // Make sure we got a lock on ALL of the LBs we need
            if (pendingLBs.size() < LBsToMove.size()) {
                // If not, we need to set the ones we did get back to ACTIVE
                for (LoadBalancer pendingLB : pendingLBs) {
                    loadBalancerService.setStatus(pendingLB, ACTIVE);
                }
                BadRequest lockFail = new BadRequest();
                lockFail.setValidationErrors(new ValidationErrors());
                lockFail.getValidationErrors().getMessages().add(SHAREDLOCKFAIL);
                lockFail.getValidationErrors().getMessages().add("Could not obtain lock for LB #" + lastLockAttempt);
                return Response.status(Response.Status.BAD_REQUEST).entity(lockFail).build();
            }

            mdc.setIds(lbIds);

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
