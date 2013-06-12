package org.openstack.atlas.usagerefactor;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsageEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@RunWith(Enclosed.class)
public class MigrationProcessorTest {

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
        }

        @Test
        public void NonNullEvent() {
            usage.setEventType(UsageEvent.SSL_MIXED_ON.name());

            List<LoadBalancerMergedHostUsage> mergedHostUsages = migrationProcessor.convertLoadBalancerUsage(usage);
            Assert.assertEquals(2, mergedHostUsages.size());
        }
    }

    public static class WhenRemovingDuplicateEvents {

        private MigrationProcessor migrationProcessor = new MigrationProcessor();
        List<LoadBalancerUsageEvent> loadBalancerUsageEvents = new ArrayList<LoadBalancerUsageEvent>();
        List<LoadBalancerHostUsage> loadBalancerHostUsages = new ArrayList<LoadBalancerHostUsage>();
        private Integer accountId = 1234;
        private Integer lbId = 1234;
        private LoadBalancerUsageEvent createEvent;
        private LoadBalancerHostUsage createEventHost1;
        private LoadBalancerHostUsage createEventHost2;

        @Before
        public void standUp() {
            Calendar now = Calendar.getInstance();

            createEvent = new LoadBalancerUsageEvent();
            createEvent.setAccountId(accountId);
            createEvent.setLoadbalancerId(lbId);
            createEvent.setEventType(UsageEvent.CREATE_LOADBALANCER.name());
            createEvent.setStartTime(now);

            loadBalancerUsageEvents.add(createEvent);

            createEventHost1 = new LoadBalancerHostUsage(accountId, lbId, 1, 0L, 0L, 0L, 0L, 0L, 0L, 1, 0, now, UsageEvent.CREATE_LOADBALANCER);
            createEventHost2 = new LoadBalancerHostUsage(accountId, lbId, 2, 0L, 0L, 0L, 0L, 0L, 0L, 1, 0, now, UsageEvent.CREATE_LOADBALANCER);

            loadBalancerHostUsages.add(createEventHost1);
            loadBalancerHostUsages.add(createEventHost2);

        }

        @After
        public void tearDown() {
            loadBalancerUsageEvents.clear();
            loadBalancerHostUsages.clear();
        }

        @Test
        public void shouldRemoveDuplicatesWhenMultipleHostEvents() {
            List<LoadBalancerUsageEvent> listWithOutDuplicates = migrationProcessor.removeDuplicateEvents(loadBalancerUsageEvents, loadBalancerHostUsages);
            Assert.assertEquals(0, listWithOutDuplicates.size());
        }

        @Test
        public void shouldRemoveDuplicatesWhenOneHostHasEvent() {
            loadBalancerHostUsages.remove(createEventHost1);

            List<LoadBalancerUsageEvent> listWithOutDuplicates = migrationProcessor.removeDuplicateEvents(loadBalancerUsageEvents, loadBalancerHostUsages);
            Assert.assertEquals(0, listWithOutDuplicates.size());
        }

        @Test
        public void shouldNotRemoveAnyEventsWhenNoDuplicates() {
            loadBalancerHostUsages.clear();

            List<LoadBalancerUsageEvent> listWithOutDuplicates = migrationProcessor.removeDuplicateEvents(loadBalancerUsageEvents, loadBalancerHostUsages);
            Assert.assertEquals(1, listWithOutDuplicates.size());
        }
    }
}
