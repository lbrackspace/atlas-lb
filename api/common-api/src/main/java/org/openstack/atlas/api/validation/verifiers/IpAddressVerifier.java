package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.api.validation.util.IPString.IPUtils;

public class IpAddressVerifier implements Verifier<String> {

    @Override
    public VerifierResult verify(String ipAddress) {
        if(ipAddress.matches(".*[a-zA-Z]+.*")) {
            return new VerifierResult(true);
        }
        return new VerifierResult(IPUtils.isValidIpv4String(ipAddress) || IPUtils.isValidIpv6String(ipAddress));
    }
}