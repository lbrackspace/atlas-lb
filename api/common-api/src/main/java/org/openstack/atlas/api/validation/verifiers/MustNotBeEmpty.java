package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;

import java.util.Collection;

public class MustNotBeEmpty implements Verifier {

    @Override
    public VerifierResult verify(Object obj) {
        if (obj == null) {
            return new VerifierResult(false);
        }

        if (obj instanceof String) {
            return new VerifierResult(!((String) obj).isEmpty());
        }

        if (obj instanceof Collection) {
            return new VerifierResult(((Collection) obj).isEmpty());
        }

        return new VerifierResult(false);
    }
}
