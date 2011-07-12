package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;
import static org.openstack.atlas.util.ip.IPUtils.isValidIpv4String;

public class MustBeValidIpv4Address implements Verifier {

    @Override
    public VerifierResult verify(Object obj) {
        boolean out;
        out = isValidIpv4String((String) obj);
        return new VerifierResult(out); // False by default
    }
}
