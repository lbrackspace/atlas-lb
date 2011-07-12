package org.openstack.atlas.api.validation.verifiers;

public class MustEqualTo implements Verifier {
    private final Object obj;

    public MustEqualTo(Object obj) {
        this.obj = obj;
    }

    @Override
    public VerifierResult verify(Object obj) {
        return new VerifierResult(this.obj.equals(obj));
    }
}
