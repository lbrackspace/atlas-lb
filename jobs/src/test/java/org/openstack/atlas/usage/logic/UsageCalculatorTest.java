package org.openstack.atlas.usage.logic;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;

import java.util.HashMap;
import java.util.Map;

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

    public static class WhenCalculatingBandwidthIn {

        @Test
        public void shouldReturnZeroWhenCurrentValueIsZeroLastBandwidthBytesInIsNull() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesIn(0l);
            usageRecord.setLastBandwidthBytesIn(null);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesIn(usageRecord, 0l);
            org.junit.Assert.assertEquals(new Long(0), actualValue);
        }

        @Test
        public void shouldReturnZeroWhenCurrentValueIsZeroLastBandwidthBytesInSslIsNull() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesInSsl(0l);
            usageRecord.setLastBandwidthBytesInSsl(null);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesInSsl(usageRecord, 0l);
            org.junit.Assert.assertEquals(new Long(0), actualValue);
        }

        @Test
        public void shouldReturnCurrentValueWhenCurrentValueIsZeroLastBandwidthBytesInIsNull() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesIn(1000l);
            usageRecord.setLastBandwidthBytesIn(null);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesIn(usageRecord, 0l);
            org.junit.Assert.assertEquals(usageRecord.getCumulativeBandwidthBytesIn(), actualValue);
        }

        @Test
        public void shouldReturnCurrentValueWhenCurrentValueIsZeroLastBandwidthBytesInSslIsNull() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesInSsl(1000l);
            usageRecord.setLastBandwidthBytesInSsl(null);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesInSsl(usageRecord, 0l);
            org.junit.Assert.assertEquals(usageRecord.getCumulativeBandwidthBytesInSsl(), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesInWhenUsageIsZero() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesIn(0l);
            usageRecord.setLastBandwidthBytesIn(0l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesIn(usageRecord, 0l);
            org.junit.Assert.assertEquals(new Long(0), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesInSslWhenUsageIsZero() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesInSsl(0l);
            usageRecord.setLastBandwidthBytesInSsl(0l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesInSsl(usageRecord, 0l);
            org.junit.Assert.assertEquals(new Long(0), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesInWhenUsageIsIncreasingFromZero() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesIn(0l);
            usageRecord.setLastBandwidthBytesIn(0l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesIn(usageRecord, 1024l);
            org.junit.Assert.assertEquals(new Long(1024), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesInSslWhenUsageIsIncreasingFromZero() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesInSsl(0l);
            usageRecord.setLastBandwidthBytesInSsl(0l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesInSsl(usageRecord, 1024l);
            org.junit.Assert.assertEquals(new Long(1024), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesInWhenUsageIsIncreasingFromNonZero() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesIn(1024l);
            usageRecord.setLastBandwidthBytesIn(1024l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesIn(usageRecord, 2048l);
            org.junit.Assert.assertEquals(new Long(2048), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesInSslWhenUsageIsIncreasingFromNonZero() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesInSsl(1024l);
            usageRecord.setLastBandwidthBytesInSsl(1024l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesInSsl(usageRecord, 2048l);
            org.junit.Assert.assertEquals(new Long(2048), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesInWhenZeusResetsMemory() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesIn(2048l);
            usageRecord.setLastBandwidthBytesIn(1024l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesIn(usageRecord, 512l);
            org.junit.Assert.assertEquals(new Long(2560), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesInSslWhenZeusResetsMemory() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesInSsl(2048l);
            usageRecord.setLastBandwidthBytesInSsl(1024l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesInSsl(usageRecord, 512l);
            org.junit.Assert.assertEquals(new Long(2560), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesInWhenNoUsageChange() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesIn(2560l);
            usageRecord.setLastBandwidthBytesIn(512l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesIn(usageRecord, 512l);
            org.junit.Assert.assertEquals(new Long(2560), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesInSslWhenNoUsageChange() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesInSsl(2560l);
            usageRecord.setLastBandwidthBytesInSsl(512l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesInSsl(usageRecord, 512l);
            org.junit.Assert.assertEquals(new Long(2560), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesInWhenUsageRecordWithOldZeusValue() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesIn(0l);
            usageRecord.setLastBandwidthBytesIn(1024l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesIn(usageRecord, 2048l);
            org.junit.Assert.assertEquals(new Long(1024), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesInSslWhenUsageRecordWithOldZeusValue() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesInSsl(0l);
            usageRecord.setLastBandwidthBytesInSsl(1024l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesInSsl(usageRecord, 2048l);
            org.junit.Assert.assertEquals(new Long(1024), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesInWhenUsageRecordWithOldZeusValueAndZeusResets() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesIn(0l);
            usageRecord.setLastBandwidthBytesIn(1024l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesIn(usageRecord, 512l);
            org.junit.Assert.assertEquals(new Long(512), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesInSslWhenUsageRecordWithOldZeusValueAndZeusResets() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesInSsl(0l);
            usageRecord.setLastBandwidthBytesInSsl(1024l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesInSsl(usageRecord, 512l);
            org.junit.Assert.assertEquals(new Long(512), actualValue);
        }
    }

    public static class WhenCalculatingBandwidthOut {

        @Test
        public void shouldReturnZeroWhenCurrentValueIsZeroLastBandwidthBytesOutIsNull() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesOut(0l);
            usageRecord.setLastBandwidthBytesOut(null);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesOut(usageRecord, 0l);
            org.junit.Assert.assertEquals(new Long(0), actualValue);
        }

        @Test
        public void shouldReturnZeroWhenCurrentValueIsZeroLastBandwidthBytesOutSslIsNull() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesOutSsl(0l);
            usageRecord.setLastBandwidthBytesOutSsl(null);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesOutSsl(usageRecord, 0l);
            org.junit.Assert.assertEquals(new Long(0), actualValue);
        }

        @Test
        public void shouldReturnCurrentValueWhenCurrentValueIsZeroLastBandwidthBytesOutIsNull() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesOut(1000l);
            usageRecord.setLastBandwidthBytesOut(null);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesOut(usageRecord, 0l);
            org.junit.Assert.assertEquals(usageRecord.getCumulativeBandwidthBytesOut(), actualValue);
        }

        @Test
        public void shouldReturnCurrentValueWhenCurrentValueIsZeroLastBandwidthBytesOutSslIsNull() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesOutSsl(1000l);
            usageRecord.setLastBandwidthBytesOutSsl(null);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesOutSsl(usageRecord, 0l);
            org.junit.Assert.assertEquals(usageRecord.getCumulativeBandwidthBytesOutSsl(), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesOutWhenUsageIsZero() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesOut(0l);
            usageRecord.setLastBandwidthBytesOut(0l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesOut(usageRecord, 0l);
            org.junit.Assert.assertEquals(new Long(0), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesOutSslWhenUsageIsZero() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesOutSsl(0l);
            usageRecord.setLastBandwidthBytesOutSsl(0l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesOutSsl(usageRecord, 0l);
            org.junit.Assert.assertEquals(new Long(0), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesOutWhenUsageIsIncreasingFromZero() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesOut(0l);
            usageRecord.setLastBandwidthBytesOut(0l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesOut(usageRecord, 1024l);
            org.junit.Assert.assertEquals(new Long(1024), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesOutSslWhenUsageIsIncreasingFromZero() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesOutSsl(0l);
            usageRecord.setLastBandwidthBytesOutSsl(0l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesOutSsl(usageRecord, 1024l);
            org.junit.Assert.assertEquals(new Long(1024), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesOutWhenUsageIsIncreasingFromNonZero() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesOut(1024l);
            usageRecord.setLastBandwidthBytesOut(1024l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesOut(usageRecord, 2048l);
            org.junit.Assert.assertEquals(new Long(2048), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesOutSslWhenUsageIsIncreasingFromNonZero() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesOutSsl(1024l);
            usageRecord.setLastBandwidthBytesOutSsl(1024l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesOutSsl(usageRecord, 2048l);
            org.junit.Assert.assertEquals(new Long(2048), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesOutWhenZeusResetsMemory() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesOut(2048l);
            usageRecord.setLastBandwidthBytesOut(1024l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesOut(usageRecord, 512l);
            org.junit.Assert.assertEquals(new Long(2560), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesOutSslWhenZeusResetsMemory() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesOutSsl(2048l);
            usageRecord.setLastBandwidthBytesOutSsl(1024l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesOutSsl(usageRecord, 512l);
            org.junit.Assert.assertEquals(new Long(2560), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesOutWhenNoUsageChange() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesOut(2560l);
            usageRecord.setLastBandwidthBytesOut(512l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesOut(usageRecord, 512l);
            org.junit.Assert.assertEquals(new Long(2560), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesOutSslWhenNoUsageChange() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesOutSsl(2560l);
            usageRecord.setLastBandwidthBytesOutSsl(512l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesOutSsl(usageRecord, 512l);
            org.junit.Assert.assertEquals(new Long(2560), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesOutWhenUsageRecordWithOldZeusValue() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesOut(0l);
            usageRecord.setLastBandwidthBytesOut(1024l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesOut(usageRecord, 2048l);
            org.junit.Assert.assertEquals(new Long(1024), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesOutSslWhenUsageRecordWithOldZeusValue() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesOutSsl(0l);
            usageRecord.setLastBandwidthBytesOutSsl(1024l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesOutSsl(usageRecord, 2048l);
            org.junit.Assert.assertEquals(new Long(1024), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesOutWhenUsageRecordWithOldZeusValueAndZeusResets() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesOut(0l);
            usageRecord.setLastBandwidthBytesOut(1024l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesOut(usageRecord, 512l);
            org.junit.Assert.assertEquals(new Long(512), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesOutSslWhenUsageRecordWithOldZeusValueAndZeusResets() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesOutSsl(0l);
            usageRecord.setLastBandwidthBytesOutSsl(1024l);

            Long actualValue = UsageCalculator.calculateCumBandwidthBytesOutSsl(usageRecord, 512l);
            org.junit.Assert.assertEquals(new Long(512), actualValue);
        }
    }
}
