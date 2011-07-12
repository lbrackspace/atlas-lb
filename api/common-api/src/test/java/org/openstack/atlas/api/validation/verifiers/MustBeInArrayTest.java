package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class MustBeInArrayTest {

    public static class WhenValidatingEnumerations {
        private Verifier verifier = new MustBeInArray(SampleTypes1.values());

        @Test
        public void shouldAcceptValidEnum() {
            VerifierResult result = verifier.verify(SampleTypes1.S1);
            assertTrue(result.passed());
        }

        @Test
        public void shouldRejectInEnumValueFromDifferentList() {
            VerifierResult result = verifier.verify(SampleTypes2.S1);
            assertFalse(result.passed());
        }

        @Test
        public void shouldRejectRandomObject() {
            VerifierResult result = verifier.verify(0);
            assertFalse(result.passed());
        }
    }

    private enum SampleTypes1 {
        S1, S2;
    }

    private enum SampleTypes2 {
        S1, S2;
    }
}
