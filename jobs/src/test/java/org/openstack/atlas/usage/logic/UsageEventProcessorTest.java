package org.openstack.atlas.usage.logic;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsageEvent;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageRepository;

import java.util.*;

import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class UsageEventProcessorTest {

    private static void printUsageRecords(String testCase, List<LoadBalancerUsage> usageRecords) {
        for (LoadBalancerUsage usageRecord : usageRecords) {
            System.out.println(String.format("[%s] Usage Record: %s - %s (Tags: %d, Event: %s, Last Bytes In: %d, Last Bytes In Ssl: %d, Last Bytes Out: %d, Last Bytes Out Ssl: %d, Cumulative Bytes In: %d, Cumulative Bytes In Ssl: %d, Cumulative Bytes Out: %d, Cumulative Bytes Out Ssl: %d)", testCase, usageRecord.getStartTime().getTime(), usageRecord.getEndTime().getTime(), usageRecord.getTags(), usageRecord.getEventType(), usageRecord.getLastBandwidthBytesIn(), usageRecord.getLastBandwidthBytesInSsl(), usageRecord.getLastBandwidthBytesOut(), usageRecord.getLastBandwidthBytesOutSsl(), usageRecord.getCumulativeBandwidthBytesIn(), usageRecord.getCumulativeBandwidthBytesInSsl(), usageRecord.getCumulativeBandwidthBytesOut(), usageRecord.getCumulativeBandwidthBytesOutSsl()));
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenProcessingWithNoRecentRecords {
        @Mock
        private LoadBalancerUsageRepository hourlyUsageRepository;
        @Mock
        private LoadBalancerRepository loadBalancerRepository;
        private UsageEventProcessor usageEventProcessor;

        private final int accountId = 1234;
        private final int loadBalancerId = 1;
        private LoadBalancerUsageEvent loadBalancerUsageCreateEvent;
        private LoadBalancerUsageEvent loadBalancerUsageSslOnEvent;
        private Calendar createEventTime;
        private Calendar sslOnEventTime;
        private List<LoadBalancerUsageEvent> usageEventEntries;

        @Before
        public void standUp() throws EntityNotFoundException, DeletedStatusException {
            createEventTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 33, 10);
            sslOnEventTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 33, 45);

            loadBalancerUsageCreateEvent = new LoadBalancerUsageEvent(accountId, loadBalancerId, createEventTime, 1, "CREATE_LOADBALANCER", 0l, 0l, 0, 0l, 0l, 0);
            loadBalancerUsageSslOnEvent = new LoadBalancerUsageEvent(accountId, loadBalancerId, sslOnEventTime, 1, "SSL_MIXED_ON", 100l, 100l, 1, 0l, 0l, 0);

            usageEventEntries = new ArrayList<LoadBalancerUsageEvent>();
            usageEventEntries.add(loadBalancerUsageCreateEvent);
            usageEventEntries.add(loadBalancerUsageSslOnEvent);

            usageEventProcessor = new UsageEventProcessor(usageEventEntries, hourlyUsageRepository, loadBalancerRepository);

            when(hourlyUsageRepository.getMostRecentUsageForLoadBalancer(Matchers.<Integer>any())).thenReturn(null);
            when(loadBalancerRepository.getVipsByAccountIdLoadBalancerId(Matchers.<Integer>any(), Matchers.<Integer>any())).thenReturn(new HashSet<VirtualIp>());
        }

        @Test
        public void shouldSucceedWhenEventsAreBackToBackWithinTheSameHour() {
            usageEventProcessor.process();
            final List<LoadBalancerUsage> usagesToCreate = usageEventProcessor.getUsagesToCreate();

            printUsageRecords("shouldSucceedWhenEventsAreBackToBackWithinTheSameHour", usagesToCreate);

            Assert.assertEquals(2, usagesToCreate.size());

            // Check timestamps
            Assert.assertEquals(loadBalancerUsageCreateEvent.getStartTime(), usagesToCreate.get(0).getStartTime());
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getStartTime(), usagesToCreate.get(0).getEndTime());
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getStartTime(), usagesToCreate.get(1).getStartTime());
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getStartTime(), usagesToCreate.get(1).getEndTime());

            // Check tags
            Assert.assertEquals(0, usagesToCreate.get(0).getTags().intValue());
            Assert.assertEquals(BitTag.SSL.tagValue() + BitTag.SSL_MIXED_MODE.tagValue(), usagesToCreate.get(1).getTags().intValue());

            // Check usage
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getLastBandwidthBytesIn(), usagesToCreate.get(0).getCumulativeBandwidthBytesIn());
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getLastBandwidthBytesInSsl(), usagesToCreate.get(0).getCumulativeBandwidthBytesInSsl());
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getLastBandwidthBytesOut(), usagesToCreate.get(0).getCumulativeBandwidthBytesOut());
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getLastBandwidthBytesOutSsl(), usagesToCreate.get(0).getCumulativeBandwidthBytesOutSsl());
            Assert.assertEquals(new Double(0), usagesToCreate.get(0).getAverageConcurrentConnections());
            Assert.assertEquals(new Double(0), usagesToCreate.get(0).getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(new Integer(0), usagesToCreate.get(0).getNumberOfPolls());

        }

        @Test
        public void shouldSucceedWhenEventsOccurInDifferentHours() {
            sslOnEventTime.set(Calendar.HOUR_OF_DAY, 4);

            usageEventProcessor.process();
            final List<LoadBalancerUsage> usagesToCreate = usageEventProcessor.getUsagesToCreate();

            printUsageRecords("shouldSucceedWhenEventsOccurInDifferentHours", usagesToCreate);

            Assert.assertEquals(3, usagesToCreate.size());

            // Check timestamps
            Assert.assertEquals(loadBalancerUsageCreateEvent.getStartTime(), usagesToCreate.get(0).getStartTime());
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getStartTime(), usagesToCreate.get(1).getEndTime());
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getStartTime(), usagesToCreate.get(2).getStartTime());
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getStartTime(), usagesToCreate.get(2).getEndTime());

            // Check tags
            Assert.assertEquals(0, usagesToCreate.get(0).getTags().intValue());
            Assert.assertEquals(0, usagesToCreate.get(1).getTags().intValue());
            Assert.assertEquals(BitTag.SSL.tagValue() + BitTag.SSL_MIXED_MODE.tagValue(), usagesToCreate.get(2).getTags().intValue());

            // Check usage
            for (LoadBalancerUsage loadBalancerUsage : usagesToCreate) {
                Assert.assertEquals(new Long(0), loadBalancerUsage.getCumulativeBandwidthBytesIn());
                Assert.assertEquals(new Long(0), loadBalancerUsage.getCumulativeBandwidthBytesInSsl());
                Assert.assertEquals(new Long(0), loadBalancerUsage.getCumulativeBandwidthBytesOut());
                Assert.assertEquals(new Long(0), loadBalancerUsage.getCumulativeBandwidthBytesOutSsl());
                Assert.assertEquals(new Double(0), loadBalancerUsage.getAverageConcurrentConnections());
                Assert.assertEquals(new Double(0), loadBalancerUsage.getAverageConcurrentConnectionsSsl());
                Assert.assertEquals(new Integer(0), loadBalancerUsage.getNumberOfPolls());
            }
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenProcessingWithRecentRecords {
        @Mock
        private LoadBalancerUsageRepository hourlyUsageRepository;
        @Mock
        private LoadBalancerRepository loadBalancerRepository;
        private UsageEventProcessor usageEventProcessor;

        private final int accountId = 1234;
        private final int loadBalancerId = 1;
        private LoadBalancerUsage mostRecentUsage;
        private Calendar sslOnEventTime;
        private LoadBalancerUsageEvent loadBalancerUsageSslOnEvent;
        private LoadBalancerUsageEvent loadBalancerUsageSslOffEvent;
        private Calendar sslOffEventTime;
        private List<LoadBalancerUsageEvent> usageEventEntries;

        @Before
        public void standUp() throws EntityNotFoundException, DeletedStatusException {
            sslOnEventTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 35, 45);
            sslOffEventTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 50, 10);

            loadBalancerUsageSslOnEvent = new LoadBalancerUsageEvent(accountId, loadBalancerId, sslOnEventTime, 1, "SSL_ONLY_ON", 100l, 100l, 1, 0l, 0l, 0);
            loadBalancerUsageSslOffEvent = new LoadBalancerUsageEvent(accountId, loadBalancerId, sslOffEventTime, 1, "SSL_OFF", 200l, 200l, 1, 100l, 100l, 0);

            usageEventEntries = new ArrayList<LoadBalancerUsageEvent>();
            usageEventEntries.add(loadBalancerUsageSslOnEvent);
            usageEventEntries.add(loadBalancerUsageSslOffEvent);

            usageEventProcessor = new UsageEventProcessor(usageEventEntries, hourlyUsageRepository, loadBalancerRepository);

            mostRecentUsage = new LoadBalancerUsage();
            mostRecentUsage.setAccountId(accountId);
            mostRecentUsage.setLoadbalancerId(loadBalancerId);
            mostRecentUsage.setEventType("CREATE_LOADBALANCER");
            mostRecentUsage.setStartTime(new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 33, 10));
            mostRecentUsage.setEndTime(new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 33, 10));
            mostRecentUsage.setTags(BitTag.SERVICENET_LB.tagValue());
            mostRecentUsage.setLastBandwidthBytesIn(0l);
            mostRecentUsage.setLastBandwidthBytesInSsl(0l);
            mostRecentUsage.setLastBandwidthBytesOut(0l);
            mostRecentUsage.setLastBandwidthBytesOutSsl(0l);

            when(hourlyUsageRepository.getMostRecentUsageForLoadBalancer(Matchers.<Integer>eq(loadBalancerId))).thenReturn(mostRecentUsage);
            when(loadBalancerRepository.getVipsByAccountIdLoadBalancerId(Matchers.<Integer>any(), Matchers.<Integer>any())).thenReturn(new HashSet<VirtualIp>());
        }

        @Test
        public void shouldSucceedWhenEventsAreBackToBackWithinTheSameHour() {
            usageEventProcessor.process();
            final List<LoadBalancerUsage> usagesToUpdate = usageEventProcessor.getUsagesToUpdate();
            final List<LoadBalancerUsage> usagesToCreate = usageEventProcessor.getUsagesToCreate();

            printUsageRecords("shouldSucceedWhenEventsAreBackToBackWithinTheSameHour", usagesToUpdate);
            printUsageRecords("shouldSucceedWhenEventsAreBackToBackWithinTheSameHour", usagesToCreate);

            Assert.assertEquals(1, usagesToUpdate.size());
            Assert.assertEquals(2, usagesToCreate.size());

            // Check timestamps
            Assert.assertEquals(mostRecentUsage.getStartTime(), usagesToUpdate.get(0).getStartTime());
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getStartTime(), usagesToUpdate.get(0).getEndTime());
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getStartTime(), usagesToCreate.get(0).getStartTime());
            Assert.assertEquals(loadBalancerUsageSslOffEvent.getStartTime(), usagesToCreate.get(0).getEndTime());
            Assert.assertEquals(loadBalancerUsageSslOffEvent.getStartTime(), usagesToCreate.get(1).getStartTime());
            Assert.assertEquals(loadBalancerUsageSslOffEvent.getStartTime(), usagesToCreate.get(1).getEndTime());

            // Check tags
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue(), usagesToUpdate.get(0).getTags().intValue());
            Assert.assertEquals(BitTag.SSL.tagValue() + BitTag.SERVICENET_LB.tagValue(), usagesToCreate.get(0).getTags().intValue());
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue(), usagesToCreate.get(1).getTags().intValue());

            // Check usage
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getLastBandwidthBytesIn(), usagesToUpdate.get(0).getCumulativeBandwidthBytesIn());
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getLastBandwidthBytesInSsl(), usagesToUpdate.get(0).getCumulativeBandwidthBytesInSsl());
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getLastBandwidthBytesOut(), usagesToUpdate.get(0).getCumulativeBandwidthBytesOut());
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getLastBandwidthBytesOutSsl(), usagesToUpdate.get(0).getCumulativeBandwidthBytesOutSsl());
            Assert.assertEquals(new Double(0), usagesToUpdate.get(0).getAverageConcurrentConnections());
            Assert.assertEquals(new Double(0), usagesToUpdate.get(0).getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(new Integer(0), usagesToUpdate.get(0).getNumberOfPolls());

            Assert.assertEquals(new Long(loadBalancerUsageSslOffEvent.getLastBandwidthBytesIn() - loadBalancerUsageSslOnEvent.getLastBandwidthBytesIn()), usagesToCreate.get(0).getCumulativeBandwidthBytesIn());
            Assert.assertEquals(new Long(loadBalancerUsageSslOffEvent.getLastBandwidthBytesInSsl() - loadBalancerUsageSslOnEvent.getLastBandwidthBytesInSsl()), usagesToCreate.get(0).getCumulativeBandwidthBytesInSsl());
            Assert.assertEquals(new Long(loadBalancerUsageSslOffEvent.getLastBandwidthBytesOut() - loadBalancerUsageSslOnEvent.getLastBandwidthBytesOut()), usagesToCreate.get(0).getCumulativeBandwidthBytesOut());
            Assert.assertEquals(new Long(loadBalancerUsageSslOffEvent.getLastBandwidthBytesOutSsl() - loadBalancerUsageSslOnEvent.getLastBandwidthBytesOutSsl()), usagesToCreate.get(0).getCumulativeBandwidthBytesOutSsl());
            Assert.assertEquals(new Double(0), usagesToCreate.get(0).getAverageConcurrentConnections());
            Assert.assertEquals(new Double(0), usagesToCreate.get(0).getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(new Integer(0), usagesToCreate.get(0).getNumberOfPolls());

            Assert.assertEquals(new Long(0), usagesToCreate.get(1).getCumulativeBandwidthBytesIn());
            Assert.assertEquals(new Long(0), usagesToCreate.get(1).getCumulativeBandwidthBytesInSsl());
            Assert.assertEquals(new Long(0), usagesToCreate.get(1).getCumulativeBandwidthBytesOut());
            Assert.assertEquals(new Long(0), usagesToCreate.get(1).getCumulativeBandwidthBytesOutSsl());
            Assert.assertEquals(new Double(0), usagesToCreate.get(1).getAverageConcurrentConnections());
            Assert.assertEquals(new Double(0), usagesToCreate.get(1).getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(new Integer(0), usagesToCreate.get(1).getNumberOfPolls());
        }

        @Test
        public void shouldSucceedWhenEventsOccurInDifferentHours() {
            sslOnEventTime.set(Calendar.HOUR_OF_DAY, 4);
            sslOffEventTime.set(Calendar.HOUR_OF_DAY, 5);

            usageEventProcessor.process();
            final List<LoadBalancerUsage> usagesToUpdate = usageEventProcessor.getUsagesToUpdate();
            final List<LoadBalancerUsage> usagesToCreate = usageEventProcessor.getUsagesToCreate();

            printUsageRecords("shouldSucceedWhenEventsOccurInDifferentHours", usagesToUpdate);
            printUsageRecords("shouldSucceedWhenEventsOccurInDifferentHours", usagesToCreate);

            Assert.assertEquals(1, usagesToUpdate.size());
            Assert.assertEquals(4, usagesToCreate.size());

            // Check timestamps
            Assert.assertEquals(mostRecentUsage.getStartTime(), usagesToUpdate.get(0).getStartTime());
            Assert.assertEquals(usagesToCreate.get(0).getStartTime().getTimeInMillis() - 1, usagesToUpdate.get(0).getEndTime().getTimeInMillis());

            Assert.assertEquals(usagesToUpdate.get(0).getEndTime().getTimeInMillis() + 1, usagesToCreate.get(0).getStartTime().getTimeInMillis());
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getStartTime(), usagesToCreate.get(0).getEndTime());
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getStartTime(), usagesToCreate.get(1).getStartTime());
            Assert.assertEquals(usagesToCreate.get(2).getStartTime().getTimeInMillis() - 1, usagesToCreate.get(1).getEndTime().getTimeInMillis());
            Assert.assertEquals(usagesToCreate.get(1).getEndTime().getTimeInMillis() + 1, usagesToCreate.get(2).getStartTime().getTimeInMillis());
            Assert.assertEquals(loadBalancerUsageSslOffEvent.getStartTime(), usagesToCreate.get(2).getEndTime());
            Assert.assertEquals(loadBalancerUsageSslOffEvent.getStartTime(), usagesToCreate.get(3).getStartTime());
            Assert.assertEquals(loadBalancerUsageSslOffEvent.getStartTime(), usagesToCreate.get(3).getEndTime());

            // Check tags
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue(), usagesToUpdate.get(0).getTags().intValue());
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue(), usagesToCreate.get(0).getTags().intValue());
            Assert.assertEquals(BitTag.SSL.tagValue() + BitTag.SERVICENET_LB.tagValue(), usagesToCreate.get(1).getTags().intValue());
            Assert.assertEquals(BitTag.SSL.tagValue() + BitTag.SERVICENET_LB.tagValue(), usagesToCreate.get(2).getTags().intValue());
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue(), usagesToCreate.get(3).getTags().intValue());

            // Check usage
            for (LoadBalancerUsage loadBalancerUsage : usagesToCreate) {
                Assert.assertEquals(new Long(0), loadBalancerUsage.getCumulativeBandwidthBytesIn());
                Assert.assertEquals(new Long(0), loadBalancerUsage.getCumulativeBandwidthBytesInSsl());
                Assert.assertEquals(new Long(0), loadBalancerUsage.getCumulativeBandwidthBytesOut());
                Assert.assertEquals(new Long(0), loadBalancerUsage.getCumulativeBandwidthBytesOutSsl());
                Assert.assertEquals(new Double(0), loadBalancerUsage.getAverageConcurrentConnections());
                Assert.assertEquals(new Double(0), loadBalancerUsage.getAverageConcurrentConnectionsSsl());
                Assert.assertEquals(new Integer(0), loadBalancerUsage.getNumberOfPolls());
            }
        }
    }

    public static class WhenCreatingBufferRecords {

        private LoadBalancerUsage lbUsage1;
        private LoadBalancerUsage lbUsage2;

        @Before
        public void standUp() {
            lbUsage1 = new LoadBalancerUsage();
            lbUsage1.setAccountId(1234);
            lbUsage1.setLoadbalancerId(1);
            lbUsage1.setLastBandwidthBytesIn(1234l);
            lbUsage1.setLastBandwidthBytesInSsl(12345l);
            lbUsage1.setLastBandwidthBytesOut(123456l);
            lbUsage1.setLastBandwidthBytesOutSsl(1234567l);
            lbUsage1.setTags(5);
            lbUsage1.setNumVips(1);
            lbUsage1.setStartTime(null);
            lbUsage1.setEndTime(null);

            lbUsage2 = new LoadBalancerUsage();
            lbUsage2.setAccountId(1234);
            lbUsage2.setLoadbalancerId(1);
            lbUsage2.setLastBandwidthBytesIn(1234l);
            lbUsage2.setLastBandwidthBytesInSsl(12345l);
            lbUsage2.setLastBandwidthBytesOut(123456l);
            lbUsage2.setLastBandwidthBytesOutSsl(1234567l);
            lbUsage2.setTags(5);
            lbUsage2.setNumVips(1);
            lbUsage2.setStartTime(null);
            lbUsage2.setEndTime(null);
        }

        /*
         *  Case 1:
         *  First record has an end time close to the hour mark and then an event
         *  slips in right before the hour is over.
         */
        @Test
        public void shouldCreateContiguousBufferRecordsCase1() {
            Calendar lb1StartTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 0, 0);
            Calendar lb1EndTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 58, 13);
            Calendar lb2StartTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 59, 5);
            Calendar lb2EndTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 59, 5);

            lbUsage1.setStartTime(lb1StartTime);
            lbUsage1.setEndTime(lb1EndTime);
            lbUsage2.setStartTime(lb2StartTime);
            lbUsage2.setEndTime(lb2EndTime);

            final List<LoadBalancerUsage> bufferRecords = UsageEventProcessor.createBufferRecordsIfNeeded(lbUsage1, lbUsage2);

            printUsageRecords("shouldCreateContiguousBufferRecordsCase1", bufferRecords);

            Assert.assertEquals(1, bufferRecords.size());
            Assert.assertEquals(lb1EndTime.getTimeInMillis(), bufferRecords.get(0).getStartTime().getTimeInMillis());
            Assert.assertEquals(lb2StartTime.getTimeInMillis(), bufferRecords.get(0).getEndTime().getTimeInMillis());

            Assert.assertEquals(lbUsage1.getTags(), bufferRecords.get(0).getTags());
        }

        /*
         *  Case 2:
         *  First record has an end time close to the hour mark and then an event
         *  slips in right at the beginning of the new hour.
         */
        @Test
        public void shouldCreateContiguousBufferRecordsCase2() {
            Calendar lb1StartTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 0, 0);
            Calendar lb1EndTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 58, 13);
            Calendar lb2StartTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 4, 0, 0);
            Calendar lb2EndTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 4, 0, 0);

            lbUsage1.setStartTime(lb1StartTime);
            lbUsage1.setEndTime(lb1EndTime);
            lbUsage2.setStartTime(lb2StartTime);
            lbUsage2.setEndTime(lb2EndTime);

            final List<LoadBalancerUsage> bufferRecords = UsageEventProcessor.createBufferRecordsIfNeeded(lbUsage1, lbUsage2);

            printUsageRecords("shouldCreateContiguousBufferRecordsCase2", bufferRecords);

            Assert.assertEquals(1, bufferRecords.size());
            Assert.assertEquals(lb1EndTime.getTimeInMillis(), bufferRecords.get(0).getStartTime().getTimeInMillis());
            Assert.assertEquals(lb2StartTime.getTimeInMillis() - 1, bufferRecords.get(0).getEndTime().getTimeInMillis());

            Assert.assertEquals(lbUsage1.getTags(), bufferRecords.get(0).getTags());
        }

        /*
         *  Case 3:
         *  First record has an end time close to the hour mark and then an event
         *  slips in right after the hour is over.
         */
        @Test
        public void shouldCreateContiguousBufferRecordsCase3() {
            Calendar lb1StartTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 0, 0);
            Calendar lb1EndTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 58, 13);
            Calendar lb2StartTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 4, 0, 5);
            Calendar lb2EndTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 4, 0, 5);

            Calendar hourMark = new GregorianCalendar(2012, Calendar.JUNE, 1, 4, 0, 0);

            lbUsage1.setStartTime(lb1StartTime);
            lbUsage1.setEndTime(lb1EndTime);
            lbUsage2.setStartTime(lb2StartTime);
            lbUsage2.setEndTime(lb2EndTime);

            final List<LoadBalancerUsage> bufferRecords = UsageEventProcessor.createBufferRecordsIfNeeded(lbUsage1, lbUsage2);

            printUsageRecords("shouldCreateContiguousBufferRecordsCase3", bufferRecords);

            Assert.assertEquals(2, bufferRecords.size());
            Assert.assertEquals(lb1EndTime.getTimeInMillis(), bufferRecords.get(0).getStartTime().getTimeInMillis());
            Assert.assertEquals(hourMark.getTimeInMillis() - 1, bufferRecords.get(0).getEndTime().getTimeInMillis());
            Assert.assertEquals(hourMark.getTimeInMillis(), bufferRecords.get(1).getStartTime().getTimeInMillis());
            Assert.assertEquals(lb2StartTime.getTimeInMillis(), bufferRecords.get(1).getEndTime().getTimeInMillis());

            Assert.assertEquals(lbUsage1.getTags(), bufferRecords.get(0).getTags());
            Assert.assertEquals(lbUsage1.getTags(), bufferRecords.get(1).getTags());
        }

        /*
         *  Case 4:
         *  First record has an end time close to the hour mark and then an event
         *  occurs the next day due to the fact that the usage poller went down.
         */
        @Test
        public void shouldCreateContiguousBufferRecordsCase4() {
            Calendar lb1StartTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 0, 0);
            Calendar lb1EndTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 58, 13);
            Calendar lb2StartTime = new GregorianCalendar(2012, Calendar.JUNE, 2, 4, 0, 5);
            Calendar lb2EndTime = new GregorianCalendar(2012, Calendar.JUNE, 2, 4, 0, 5);

            lbUsage1.setStartTime(lb1StartTime);
            lbUsage1.setEndTime(lb1EndTime);
            lbUsage2.setStartTime(lb2StartTime);
            lbUsage2.setEndTime(lb2EndTime);

            final List<LoadBalancerUsage> bufferRecords = UsageEventProcessor.createBufferRecordsIfNeeded(lbUsage1, lbUsage2);

            printUsageRecords("shouldCreateContiguousBufferRecordsCase4", bufferRecords);

            Assert.assertEquals(26, bufferRecords.size());

            for (LoadBalancerUsage bufferRecord : bufferRecords) {
                Assert.assertEquals(lbUsage1.getTags(), bufferRecord.getTags());
            }
        }

    }
}
