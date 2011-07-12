package org.openstack.atlas.api.validation.verifiers;

import java.util.Collection;

public class MustHaveSizeOfAtLeast implements Verifier {

    private final int num;

    public MustHaveSizeOfAtLeast(int num) {
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

            verifiedCorrectly = countNonNullObjects >= num;
        } else if (obj instanceof String) {
            verifiedCorrectly = ((String) obj).length() >= num;
        } else if (obj instanceof Integer) {
            verifiedCorrectly =((Integer) obj) >= num;  
        }

        return new VerifierResult(verifiedCorrectly);
    }
}
