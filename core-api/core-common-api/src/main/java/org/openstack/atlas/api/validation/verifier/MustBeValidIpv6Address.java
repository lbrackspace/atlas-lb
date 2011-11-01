package org.openstack.atlas.api.validation.verifier;


import org.openstack.atlas.api.validation.util.IPString.IPUtils;

public class MustBeValidIpv6Address implements Verifier{
    @Override
    public VerifierResult verify(Object ipStr) {
        boolean out;
        out = IPUtils.isValidIpv6String((String)ipStr);
        return new VerifierResult(out);
    }
}
