package org.openstack.atlas.usagerefactor;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;

import java.util.Calendar;
import java.util.List;

@RunWith(Enclosed.class)
public class MigrationProcessorTest {

    @RunWith(MockitoJUnitRunner.class)
    public static class TestConvertLoadBalancerUsage {

        private MigrationProcessor migrationProcessor = new MigrationProcessor();
        private LoadBalancerUsage usage;

        @Before
        public void standUp() {
            usage = new LoadBalancerUsage();
            usage.setAccountId(1);
            usage.setAverageConcurrentConnections(2d);
            usage.setAverageConcurrentConnectionsSsl(3d);
            usage.setCumulativeBandwidthBytesIn(4L);
            usage.setCumulativeBandwidthBytesInSsl(5L);
            usage.setCumulativeBandwidthBytesOut(6L);
            usage.setCumulativeBandwidthBytesOutSsl(7L);
            usage.setLastBandwidthBytesIn(8L);
            usage.setLastBandwidthBytesInSsl(9L);
            usage.setLastBandwidthBytesOut(10L);
            usage.setLastBandwidthBytesOutSsl(11L);
            usage.setLoadbalancerId(12);
            usage.setNumberOfPolls(13);
            usage.setNumVips(14);
            Calendar startTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();
            endTime.setTime(startTime.getTime());
            endTime.add(Calendar.MINUTE, 1);
            usage.setStartTime(startTime);
            usage.setEndTime(endTime);
            usage.setTags(5);
            usage.setId(15);
        }

        @Test
        public void NullEvent() {
            List<LoadBalancerMergedHostUsage> mergedHostUsages = migrationProcessor.convertLoadBalancerUsage(usage);
            Assert.assertEquals(1, mergedHostUsages.size());

            LoadBalancerMergedHostUsage mergedHostUsage = mergedHostUsages.get(0);
//            Assert.assertEquals();
        }

        @Test
        public void NonNullEvent() {
            usage.setEventType(UsageEvent.SSL_MIXED_ON.name());

            List<LoadBalancerMergedHostUsage> mergedHostUsages = migrationProcessor.convertLoadBalancerUsage(usage);
            Assert.assertEquals(2, mergedHostUsages.size());
        }
    }
}
