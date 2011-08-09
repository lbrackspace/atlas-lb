package org.openstack.atlas.api.validation.verifier;


import org.openstack.atlas.api.validation.util.IPString.IPUtils;

public class MustBeValidIpv4Address implements Verifier {

    @Override
    public VerifierResult verify(Object obj) {
        boolean out;
        out = IPUtils.isValidIpv4String((String) obj);
        return new VerifierResult(out); // False by default
    }
}
