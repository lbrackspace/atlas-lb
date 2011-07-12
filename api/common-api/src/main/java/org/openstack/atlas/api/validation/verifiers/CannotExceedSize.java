package org.openstack.atlas.api.validation.verifiers;

import java.util.Collection;

public class CannotExceedSize implements Verifier {

    private final int num;

    public CannotExceedSize(int num) {
        this.num = num;
    }

    @Override
    public VerifierResult verify(Object obj) {
        boolean verifiedCorrectly = false;

        if (obj instanceof Collection) {
            int countNonNullObjects = 0;

            for (Object object : (Collection) obj) {
                if (object != null) {
                    countNonNullObjects++;
                }
            }

            verifiedCorrectly = countNonNullObjects <= num;
        } else if (obj instanceof String) {
            verifiedCorrectly = ((String) obj).length() <= num;
        } else if (obj == null) {
            verifiedCorrectly = true; // Let MustNotBeEmpty flag errors on Null. This validator only checks max length
        }

        return new VerifierResult(verifiedCorrectly);
    }
}
