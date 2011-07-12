package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.api.validation.util.IPString.IPUtils;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;

public class IpAddressVerifier implements Verifier<String> {

    @Override
    public VerifierResult verify(String ipAddress) {
        return new VerifierResult(IPUtils.isValidIpv4String(ipAddress) || IPUtils.isValidIpv6String(ipAddress));
    }
}