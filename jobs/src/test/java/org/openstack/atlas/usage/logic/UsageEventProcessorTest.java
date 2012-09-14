package org.openstack.atlas.usage.logic;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsageEvent;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageRepository;

import java.util.*;

import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class UsageEventProcessorTest {

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenEventsAreBackToBackBetweenPolls {
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

            loadBalancerUsageCreateEvent = new LoadBalancerUsageEvent(accountId, loadBalancerId, createEventTime, 1, "CREATE_LOADBALANCER", null, null, null, null, null, null);
            loadBalancerUsageSslOnEvent = new LoadBalancerUsageEvent(accountId, loadBalancerId, sslOnEventTime, 1, "SSL_ONLY_ON", null, null, null, null, null, null);

            usageEventEntries = new ArrayList<LoadBalancerUsageEvent>();
            usageEventEntries.add(loadBalancerUsageCreateEvent);
            usageEventEntries.add(loadBalancerUsageSslOnEvent);

            usageEventProcessor = new UsageEventProcessor(usageEventEntries, hourlyUsageRepository, loadBalancerRepository);

            when(hourlyUsageRepository.getMostRecentUsageForLoadBalancer(Matchers.<Integer>any())).thenReturn(null);
            when(loadBalancerRepository.getVipsByAccountIdLoadBalancerId(Matchers.<Integer>any(), Matchers.<Integer>any())).thenReturn(new HashSet<VirtualIp>());
        }

        @Test
        public void shouldCreateProperTimestampsWhenProcessed() {
            usageEventProcessor.process();
            final List<LoadBalancerUsage> usagesToCreate = usageEventProcessor.getUsagesToCreate();

            Assert.assertEquals(loadBalancerUsageCreateEvent.getStartTime(), usagesToCreate.get(0).getStartTime());
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getStartTime(), usagesToCreate.get(0).getEndTime());
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getStartTime(), usagesToCreate.get(1).getStartTime());
            Assert.assertEquals(loadBalancerUsageSslOnEvent.getStartTime(), usagesToCreate.get(1).getEndTime());
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

            Assert.assertEquals(1, bufferRecords.size());
            Assert.assertEquals(lb1EndTime.getTimeInMillis(), bufferRecords.get(0).getStartTime().getTimeInMillis());
            Assert.assertEquals(lb2StartTime.getTimeInMillis(), bufferRecords.get(0).getEndTime().getTimeInMillis());

            printBufferRecords("Case 1", bufferRecords);
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

            Assert.assertEquals(1, bufferRecords.size());
            Assert.assertEquals(lb1EndTime.getTimeInMillis(), bufferRecords.get(0).getStartTime().getTimeInMillis());
            Assert.assertEquals(lb2StartTime.getTimeInMillis() - 1, bufferRecords.get(0).getEndTime().getTimeInMillis());

            printBufferRecords("Case 2", bufferRecords);
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

            Assert.assertEquals(2, bufferRecords.size());
            Assert.assertEquals(lb1EndTime.getTimeInMillis(), bufferRecords.get(0).getStartTime().getTimeInMillis());
            Assert.assertEquals(hourMark.getTimeInMillis() - 1, bufferRecords.get(0).getEndTime().getTimeInMillis());
            Assert.assertEquals(hourMark.getTimeInMillis(), bufferRecords.get(1).getStartTime().getTimeInMillis());
            Assert.assertEquals(lb2StartTime.getTimeInMillis(), bufferRecords.get(1).getEndTime().getTimeInMillis());

            printBufferRecords("Case 3", bufferRecords);
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

            Assert.assertEquals(26, bufferRecords.size());

            printBufferRecords("Case 4", bufferRecords);
        }

        private void printBufferRecords(String testCase, List<LoadBalancerUsage> bufferRecords) {
            for (LoadBalancerUsage bufferRecord : bufferRecords) {
                System.out.println(String.format("(%s) Buffer Record: %s - %s", testCase, bufferRecord.getStartTime().getTime(), bufferRecord.getEndTime().getTime()));
            }
        }
    }
}
