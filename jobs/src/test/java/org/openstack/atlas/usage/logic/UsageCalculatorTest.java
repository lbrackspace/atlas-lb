package org.openstack.atlas.usage.logic;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class UsageCalculatorTest {
    public static class WhenCalculatingNewAverages {
        private Double originalAverage = 50.0;
        private Integer originalDivisor = 5;
        private Integer dataPoint1 = 50;
        private Integer dataPoint2 = 100;
        private Integer dataPoint3 = 200;
        private Double average1;
        private Double average2;
        private Double average3;

        @Before
        public void standUp() {
            average1 = UsageCalculator.calculateNewAverage(originalAverage, originalDivisor, dataPoint1);
            average2 = UsageCalculator.calculateNewAverage(average1, originalDivisor + 1, dataPoint2);
            average3 = UsageCalculator.calculateNewAverage(average2, originalDivisor + 2, dataPoint3);
        }

        @Test
        public void shouldReturnCorrectAverageWhenAddingOneNewDataPoint() {
            Assert.assertEquals(50.0, average1);
        }

        @Test
        public void shouldReturnCorrectAverageWhenAddingTwoNewDataPoints() {
            Assert.assertEquals(57.142857142857143, average2);
        }

        @Test
        public void shouldReturnCorrectAverageWhenAddingThreeNewDataPoints() {
            Assert.assertEquals(75.0, average3);
        }
    }
}
