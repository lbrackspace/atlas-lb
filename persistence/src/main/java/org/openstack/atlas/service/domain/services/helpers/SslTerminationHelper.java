package org.openstack.atlas.service.domain.services.helpers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.pojos.SslDetails;
import org.openstack.atlas.service.domain.util.StringUtilities;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;

import java.util.List;
import java.util.Map;
import org.openstack.atlas.docs.loadbalancers.api.v1.SecurityProtocol;

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
        if (loadBalancer.getProtocol().equals(LoadBalancerProtocol.DNS_UDP) || loadBalancer.getProtocol().equals(LoadBalancerProtocol.UDP) || loadBalancer.getProtocol().equals(LoadBalancerProtocol.UDP_STREAM)) {
            throw new BadRequestException("Protocol UDP, UDP_STREAM and DNS_UDP cannot be configured with ssl termination. ");
        }
        return true;
    }

    public static boolean verifyPortSecurePort(LoadBalancer loadBalancer, SslTermination sslTermination, Map<Integer, List<LoadBalancer>> vipPorts, Map<Integer, List<LoadBalancer>> vip6Ports) {
        LOG.info("Verifying port and secure port are unique for loadbalancer: " + loadBalancer.getId());
        if (sslTermination != null && sslTermination.getSecurePort() != null) {
            if (loadBalancer.hasSsl()
                    && loadBalancer.getSslTermination().getSecurePort() == sslTermination.getSecurePort()) {
                return true;
            }

            if (!vipPorts.isEmpty()) {

                if (vipPorts.containsKey(sslTermination.getSecurePort())) {
                    return false;
                }
            }

            if (!vip6Ports.isEmpty()) {
                if ((vip6Ports.containsKey(sslTermination.getSecurePort()))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void verifyCertificationCredentials(ZeusCrtFile zeusCrtFile) throws BadRequestException {
        if (zeusCrtFile.getFatalErrorList().size() > 0) {
            String errors = StringUtilities.buildDelemtedListFromStringArray(zeusCrtFile.getFatalErrorList().toArray(new String[zeusCrtFile.getFatalErrorList().size()]), ", ");
            LOG.error(String.format("There was an error(s) while updating ssl termination: '%s'", errors));
            throw new BadRequestException(errors);
        }
    }

    /**
     * Copies the fields apiSslTermination from to dbTermination.
     * @param apiSslTermination
     * @param dbTermination
     * @return
     */
    public static org.openstack.atlas.service.domain.entities.SslTermination verifyAttributes(SslTermination apiSslTermination, org.openstack.atlas.service.domain.entities.SslTermination dbTermination) {
        boolean isNewDbSslTerm = false;
        if (dbTermination == null) {
            dbTermination = new org.openstack.atlas.service.domain.entities.SslTermination();
            isNewDbSslTerm = true;
        }

        // set the allowed TLS protocols for the database
        convertApiSslTermToDbTlsProtocols(apiSslTermination, dbTermination, isNewDbSslTerm);

        //Set fields to updated values
        if (apiSslTermination.getEnabled() != null) {
            dbTermination.setEnabled(apiSslTermination.getEnabled());
        }


        if (apiSslTermination.getSecureTrafficOnly() != null) {
            if ((apiSslTermination.getEnabled() != null && !apiSslTermination.getEnabled()) || (!dbTermination.getEnabled()) && (apiSslTermination.getSecureTrafficOnly() || dbTermination.getSecureTrafficOnly())) {
                dbTermination.setSecureTrafficOnly(false);
            } else {
                dbTermination.setSecureTrafficOnly(apiSslTermination.getSecureTrafficOnly());
            }
        }

        if (apiSslTermination.getSecurePort() != null) {
            dbTermination.setSecurePort(apiSslTermination.getSecurePort());
        }

        //The certificates are either null or populated, no updating.
        if (apiSslTermination.getCertificate() != null) {
            dbTermination.setCertificate(apiSslTermination.getCertificate());
        }

        if (apiSslTermination.getIntermediateCertificate() != null) {
            dbTermination.setIntermediateCertificate(apiSslTermination.getIntermediateCertificate());
        } else {
            if (apiSslTermination.getCertificate() != null && apiSslTermination.getCertificate() != null) {
                dbTermination.setIntermediateCertificate(null);
            }
        }

        if (apiSslTermination.getPrivatekey() != null) {
            dbTermination.setPrivatekey(apiSslTermination.getPrivatekey());
        }

        return dbTermination;
    }

    public static void sanitizeSslCertKeyEntries(org.openstack.atlas.service.domain.entities.SslTermination sslTermination) {
        SslDetails sslDetails = new SslDetails(sslTermination.getPrivatekey(), sslTermination.getCertificate(), sslTermination.getIntermediateCertificate());
        sslDetails = SslDetails.sanitize(sslDetails);

        sslTermination.setPrivatekey(sslDetails.getPrivateKey());
        sslTermination.setCertificate(sslDetails.getCertificate());
        sslTermination.setIntermediateCertificate(sslDetails.getIntermediateCertificate());
    }

    // I'm almost tempted into bringing the dozer dependency down here in
    // the persistance layer. 
    public static void convertApiSslTermToDbTlsProtocols(org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination apiSslTerm, org.openstack.atlas.service.domain.entities.SslTermination dbSslTerm, boolean newDbSslTerm) {
        if (newDbSslTerm) {
            dbSslTerm.setTls10Enabled(true); // if this is a new db instance then set TLS1.0 to enabled by default
            dbSslTerm.setTls11Enabled(true);
        }
        for (SecurityProtocol sp : apiSslTerm.getSecurityProtocols()) {
            switch (sp.getSecurityProtocolName()) {
                case TLS_10:
                    switch (sp.getSecurityProtocolStatus()) {
                        case DISABLED:
                            dbSslTerm.setTls10Enabled(false);
                            break;
                        case ENABLED:
                            dbSslTerm.setTls10Enabled(true);
                            break;
                        default:
                            dbSslTerm.setTls10Enabled(true);
                            // This should really be an error as this
                            // would mean the app some how allowed the user
                            // to specify neither Enabled or disabled even
                            // though its an XSD restriction. So lets play it
                            // safe and assume they wanted to enabled TLS 1.0
                            break;
                    }
                    break; // Looks like a rouge protocol name. Just ignore it
                case TLS_11:
                    switch (sp.getSecurityProtocolStatus()) {
                        case DISABLED:
                            dbSslTerm.setTls11Enabled(false);
                            break;
                        case ENABLED:
                            dbSslTerm.setTls11Enabled(true);
                            break;
                        default:
                            dbSslTerm.setTls11Enabled(true);
                            // This should really be an error, lets play it
                            // safe and assume they wanted to enable TLS 1.1
                            break;
                    }
                    break;
                default: // Looks like a rouge protocol name. Just ignore it
                    break;
            }
        }
    }
}
