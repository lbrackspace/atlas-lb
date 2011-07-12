package org.openstack.atlas.api.validation.verifiers;

import java.util.Collection;

public class MustNotBeEmptyOrNull implements Verifier {

    public VerifierResult verify(Object obj) {
        if (obj == null) {
            return new VerifierResult(false);
        }

        if (obj instanceof Collection) {
            if (((Collection) obj).isEmpty()) return new VerifierResult(false);
        }
        if (obj instanceof String) {
            if (!"".equals(obj)) return new VerifierResult(false);
        }

        return new VerifierResult(true); // False by default
    }
}
