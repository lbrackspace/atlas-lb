package org.openstack.atlas.core.api.validation.verifier;

public interface Verifier <T> {
    public VerifierResult verify(T obj);
}
