package org.openstack.atlas.api.mapper.dozer.converter;

import java.util.ArrayList;
import java.util.List;
import org.dozer.CustomConverter;
import org.openstack.atlas.docs.loadbalancers.api.v1.SecurityProtocol;
import org.openstack.atlas.docs.loadbalancers.api.v1.SecurityProtocolName;
import org.openstack.atlas.docs.loadbalancers.api.v1.SecurityProtocolStatus;
import org.openstack.atlas.service.domain.exceptions.NoMappableConstantException;
import org.openstack.atlas.service.domain.services.helpers.SslTerminationHelper;

public class SslTerminationConverter implements CustomConverter {

    public static final Class apiSslTermClass;
    public static final Class dbSslTermClass;

    static {
        apiSslTermClass = org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination.class;
        dbSslTermClass = org.openstack.atlas.service.domain.entities.SslTermination.class;
    }

    // Fetches the protocols from the API object and sets them in the db object. Right now only TLS1.0 is considered.


    @Override
    public Object convert(Object dstValue, Object srcValue,
            Class dstClass, Class srcClass) {
        org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination apiSslTerm;
        org.openstack.atlas.service.domain.entities.SslTermination dbSslTerm;

        if (srcValue == null) {
            return null;
        }
        if (srcValue.getClass() == apiSslTermClass && dstClass == dbSslTermClass) {
            // Mapping from API to Database entitiy.
            apiSslTerm = (org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination) srcValue;
            dbSslTerm = new org.openstack.atlas.service.domain.entities.SslTermination();
            String crt = apiSslTerm.getCertificate();
            String key = apiSslTerm.getPrivatekey();
            String imd = apiSslTerm.getIntermediateCertificate();
            Integer securePort = apiSslTerm.getSecurePort();
            Boolean isEnabled = apiSslTerm.isEnabled();
            Boolean isSecureTrafficOnly = apiSslTerm.isSecureTrafficOnly();
            List<SecurityProtocol> securityProtocols = apiSslTerm.getSecurityProtocols();

            if (isEnabled == null) {
                isEnabled = true;
            }
            if (isSecureTrafficOnly == null) {
                isSecureTrafficOnly = false;
            }
            if (securePort == null) {
                securePort = 443;
            }
            dbSslTerm.setEnabled(isEnabled);
            dbSslTerm.setSecureTrafficOnly(isSecureTrafficOnly);
            dbSslTerm.setSecurePort(securePort);
            dbSslTerm.setCertificate(crt);
            dbSslTerm.setPrivatekey(key);
            dbSslTerm.setIntermediateCertificate(imd);
            SslTerminationHelper.convertApiSslTermToDbTlsProtocols(apiSslTerm, dbSslTerm, true);
            return dbSslTerm;
        } else if (srcValue.getClass() == dbSslTermClass && dstClass == apiSslTermClass) {
            apiSslTerm = new org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination();
            dbSslTerm = (org.openstack.atlas.service.domain.entities.SslTermination) srcValue;
            String crt = dbSslTerm.getCertificate();
            String key = null;  // We hide this field now from the customer
            String imd = dbSslTerm.getIntermediateCertificate();
            Integer securePort = dbSslTerm.getSecurePort();
            Boolean isEnabled = dbSslTerm.isEnabled();
            Boolean isSecureTrafficOnly = dbSslTerm.isSecureTrafficOnly();
            Boolean isTls10Enabled = dbSslTerm.isTls10Enabled();
            apiSslTerm.setCertificate(crt);
            apiSslTerm.setPrivatekey(key);
            apiSslTerm.setIntermediateCertificate(imd);
            apiSslTerm.setEnabled(isEnabled);
            apiSslTerm.setSecurePort(securePort);
            apiSslTerm.setSecureTrafficOnly(isSecureTrafficOnly);
            SecurityProtocol sp = new SecurityProtocol();
            sp.setSecurityProtocolName(SecurityProtocolName.TLS_10);

            if (isTls10Enabled != null) {
                if (isTls10Enabled == true) {
                    sp.setSecurityProtocolStatus(SecurityProtocolStatus.ENABLED);
                    apiSslTerm.getSecurityProtocols().add(sp);
                    return apiSslTerm;
                } else if (isTls10Enabled == false) {
                    sp.setSecurityProtocolStatus(SecurityProtocolStatus.DISABLED);
                    apiSslTerm.getSecurityProtocols().add(sp);
                    return apiSslTerm;
                }
                throw new RuntimeException("Unreachable code reached.");
            } else {
                // If its null set it to enabled
                sp.setSecurityProtocolStatus(SecurityProtocolStatus.ENABLED);
                apiSslTerm.getSecurityProtocols().add(sp);
                return apiSslTerm;
            }
        }
        throw new NoMappableConstantException("Cannot map source type: " + srcClass.getName());
    }
}
