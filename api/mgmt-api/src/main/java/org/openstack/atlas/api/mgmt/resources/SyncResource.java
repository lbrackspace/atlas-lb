package org.openstack.atlas.api.mgmt.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;
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
import org.openstack.atlas.service.domain.services.impl.SslTerminationServiceImpl;
import org.openstack.atlas.util.b64aes.Aes;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;
import org.openstack.atlas.util.debug.Debug;
import org.springframework.beans.factory.annotation.Autowired;

import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.PENDING_UPDATE;

public class SyncResource extends ManagementDependencyProvider {

    private static final String SSLTERMBREAK = "SyncCall will result in this loadbalancer going into error status as the sslTermination is invalid. Consider deleting the ssltermination on this Lb before attempting to sync.";
    private static final ZeusUtils zeusUtils;
    private int loadBalancerId;
    @Autowired
    protected RestApiConfiguration restApiConfiguration;

    protected final Log LOG = LogFactory.getLog(SslTerminationServiceImpl.class);


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
            if (sslTerm != null) {
                // Verify sslTerm won't break the LB during sync attempt
                String crt = sslTerm.getCertificate();
                String key = sslTerm.getPrivatekey();
                try {
                    key = Aes.b64decryptGCM_str(key, restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key), lb.getAccountId() + "_" + lb.getId());
                } catch (Exception e){
                    // It's possible the database key wasn't encrypted. Let cert utils verify and return appropriate exceptions.
                    LOG.warn("Private key could not be decrypted, ");
                }
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
