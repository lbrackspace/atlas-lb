package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.api.validation.util.IPString.IPUtils;

public class IpAddressVerifier implements Verifier<String> {
    private static final int MAX_LENGTH = 128;

    @Override
    public VerifierResult verify(String ipAddress) {
        if(ipAddress.matches(".*[a-zA-Z]+.*") && ipAddress.length() <= MAX_LENGTH) {
            return new VerifierResult(true);
        }
        return new VerifierResult(IPUtils.isValidIpv4String(ipAddress) || IPUtils.isValidIpv6String(ipAddress));
    }
}