package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;

public class MustBeNonNegativeInteger implements Verifier<Integer> {
    @Override
    public VerifierResult verify(Integer i) {
        return new VerifierResult(i != null && i >= 0);
    }
}
