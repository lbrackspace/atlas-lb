package org.openstack.atlas.api.validation.verifiers;

import java.util.Collection;

public class MustBeEmptyOrNull implements Verifier {

    public VerifierResult verify(Object obj) {
        if (obj == null) {
            return new VerifierResult(true);
        }

        if (obj instanceof Collection) {
            return new VerifierResult(((Collection) obj).isEmpty());
        }
        if (obj instanceof String) {
            return new VerifierResult("".equals(obj));
        }
        

        return new VerifierResult(false); // False by default
    }
}
