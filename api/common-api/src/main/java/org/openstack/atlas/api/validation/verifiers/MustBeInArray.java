package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;

public class MustBeInArray implements Verifier {
    private Object[] values;

    public MustBeInArray(Object[] values) {
        try {
            this.values = values;
        } catch (Exception ex) {
            this.values = new Object[values.length];
        }
    }

    @Override
    public VerifierResult verify(Object obj) {
        boolean isInEnumSet = false;
        for (Object value : values) {
            if (value.equals(obj)) {
                isInEnumSet = true;
                break;
            }
        }
        return new VerifierResult(isInEnumSet);
    }
}
