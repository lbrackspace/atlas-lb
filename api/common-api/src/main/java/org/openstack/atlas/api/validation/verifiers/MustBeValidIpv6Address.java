package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;
import static org.openstack.atlas.util.ip.IPUtils.isValidIpv6String;
public class MustBeValidIpv6Address implements Verifier{
    @Override
    public VerifierResult verify(Object ipStr) {
        boolean out;
        out = isValidIpv6String((String)ipStr);
        return new VerifierResult(out);
    }
}
