package org.openstack.atlas.api.validation.verifiers;

public interface Verifier <T> {
    public VerifierResult verify(T obj);
}
