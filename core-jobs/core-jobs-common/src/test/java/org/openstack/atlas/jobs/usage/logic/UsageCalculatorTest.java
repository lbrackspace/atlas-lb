package org.openstack.atlas.jobs.usage.logic;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class UsageCalculatorTest {

    @Ignore
    public static class WhenCalculatingAverages {}
    
    public static class WhenCalculatingCumulativeBytes {
        @Test
        public void mimicFirstPollWithNoUsage() {
            Assert.assertEquals(0l, UsageCalculator.calculateCumulativeBytes(0l, 0l, 0l).longValue());
        }

        @Test
        public void mimicFirstPollWithUsage() {
            Assert.assertEquals(100l, UsageCalculator.calculateCumulativeBytes(0l, 0l, 100l).longValue());
        }

        @Test
        public void mimicSecondPollWithNoUsage() {
            Assert.assertEquals(100l, UsageCalculator.calculateCumulativeBytes(100l, 100l, 100l).longValue());
        }

        @Test
        public void mimicSecondPollWithNoUsageAndReset() {
            Assert.assertEquals(100l, UsageCalculator.calculateCumulativeBytes(100l, 100l, 0l).longValue());
        }

        @Test
        public void mimicSecondPollWithUsage() {
            Assert.assertEquals(200l, UsageCalculator.calculateCumulativeBytes(100l, 100l, 200l).longValue());
        }

        @Test
        public void mimicSecondPollWithUsageAndReset() {
            Assert.assertEquals(150l, UsageCalculator.calculateCumulativeBytes(100l, 100l, 50l).longValue());
        }
    }
}
