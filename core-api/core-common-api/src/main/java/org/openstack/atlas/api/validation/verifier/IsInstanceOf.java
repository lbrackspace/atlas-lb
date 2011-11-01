package org.openstack.atlas.api.validation.verifier;

public class IsInstanceOf implements Verifier {
    private final Class classType;

    public IsInstanceOf(Class classType) {
        this.classType = classType;
    }

    @Override
    public VerifierResult verify(Object obj) {
        return new VerifierResult(obj.getClass().equals(classType));
    }
}
