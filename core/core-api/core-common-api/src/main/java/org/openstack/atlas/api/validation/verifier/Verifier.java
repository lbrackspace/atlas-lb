package org.openstack.atlas.api.validation.verifier;

public interface Verifier <T> {
    public VerifierResult verify(T obj);
}
