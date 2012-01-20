package org.openstack.atlas.service.domain.services.helpers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.util.StringUtilities;
import org.openstack.atlas.util.ca.zeus.ZeusCertFile;


public final class SslTerminationHelper {
    protected static final Log LOG = LogFactory.getLog(SslTerminationHelper.class);


    public static boolean modificationStatus(SslTermination sslTermination, LoadBalancer dbLoadBalancer) throws BadRequestException {
        //Validator let it through, now verify the request is for update of attributes only, skip cert validation...
        //Otherwise inform user that there is no ssl termination to update values for...
        if (sslTermination.getCertificate() == null && sslTermination.getPrivatekey() == null) {
            if (dbLoadBalancer.hasSsl()) {
                LOG.info("Updating attributes only, skipping certificate validation.");
                return true;
            } else {
                LOG.error("Cannot update values for non-existent ssl termination object...");
                throw new BadRequestException("No ssl termination to update values for.");
            }
        }
        return false;
    }

    public static boolean isProtocolSecure(LoadBalancer loadBalancer) throws BadRequestException {
        LoadBalancerProtocol protocol = loadBalancer.getProtocol();
        if (protocol == LoadBalancerProtocol.HTTPS || protocol == LoadBalancerProtocol.IMAPS
                || protocol == LoadBalancerProtocol.LDAPS || protocol == LoadBalancerProtocol.POP3S) {
            throw new BadRequestException("Can not create ssl termination on a load balancer using a secure protocol.");
        }
        return true;
    }

    public static void verifyCertificationCredentials(ZeusCertFile zeusCertFile) throws BadRequestException {
        if (zeusCertFile.getErrorList().size() > 0) {
            String errors = StringUtilities.buildDelemtedListFromStringArray(zeusCertFile.getErrorList().toArray(new String[zeusCertFile.getErrorList().size()]), ",");
            LOG.error(String.format("There was an error(s) while updating ssl termination: '%s'", errors));
            throw new BadRequestException(errors);
        }
    }

    public static org.openstack.atlas.service.domain.entities.SslTermination verifyAttributes(SslTermination queTermination,  org.openstack.atlas.service.domain.entities.SslTermination dbTermination) {
        org.openstack.atlas.service.domain.entities.SslTermination updatedTermination = new org.openstack.atlas.service.domain.entities.SslTermination();

        //Set fields to updated values
        if (queTermination.isEnabled() != null) {
            updatedTermination.setEnabled(queTermination.isEnabled());
        } else if (dbTermination != null) {
            updatedTermination.setEnabled(dbTermination.isEnabled());
        }
        if (queTermination.isSecureTrafficOnly() != null) {
            updatedTermination.setSecureTrafficOnly(queTermination.isSecureTrafficOnly());
        } else if (dbTermination != null) {
            updatedTermination.setSecureTrafficOnly(dbTermination.isSecureTrafficOnly());
        }
        if (queTermination.getSecurePort() != null) {
            updatedTermination.setSecurePort(queTermination.getSecurePort());
        } else if (dbTermination != null) {
            updatedTermination.setSecurePort(dbTermination.getSecurePort());
        }


        //The certificates are either null or populated, no updating.
        if (queTermination.getCertificate() != null) {
            updatedTermination.setCertificate(queTermination.getCertificate());
        }
        if (queTermination.getIntermediateCertificate() != null) {
            updatedTermination.setIntermediateCertificate(queTermination.getIntermediateCertificate());
        }
        if (queTermination.getPrivatekey() != null) {
            updatedTermination.setPrivatekey(queTermination.getPrivatekey());
        }
        return updatedTermination;
    }
}
