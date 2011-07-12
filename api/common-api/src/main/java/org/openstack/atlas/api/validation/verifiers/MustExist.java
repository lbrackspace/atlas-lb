package org.openstack.atlas.api.validation.verifiers;

public class MustExist<T> implements Verifier<Object> {

    public VerifierResult verify(Object obj) {
        return new VerifierResult(obj != null);
    }
}
