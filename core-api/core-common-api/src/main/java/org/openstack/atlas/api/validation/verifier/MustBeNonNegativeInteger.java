package org.openstack.atlas.api.validation.verifier;

public class MustBeNonNegativeInteger implements Verifier<Integer> {
    @Override
    public VerifierResult verify(Integer i) {
        return new VerifierResult(i != null && i >= 0);
    }
}
