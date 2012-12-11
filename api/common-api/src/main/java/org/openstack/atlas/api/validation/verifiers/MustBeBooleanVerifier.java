package org.openstack.atlas.api.validation.verifiers;

public class MustBeBooleanVerifier implements Verifier<Object> {

    public VerifierResult verify(Object obj) {
        return new VerifierResult(obj != null && (obj.toString().equals("false") || obj.toString().equals("true")));
    }
}
