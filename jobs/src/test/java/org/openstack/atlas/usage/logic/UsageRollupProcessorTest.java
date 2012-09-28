package org.openstack.atlas.usage.logic;

import com.rackspace.docs.core.event.EventType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;

import java.util.*;

import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class UsageRollupProcessorTest {

    private static void printUsageRecords(String testCase, List<Usage> usageRecords) {
        for (Usage usageRecord : usageRecords) {
            System.out.println(String.format("[%s] Usage Record: %s - %s (Tags: %d, Event: %s, Cumulative Bytes In: %d, Cumulative Bytes In Ssl: %d, Cumulative Bytes Out: %d, Cumulative Bytes Out Ssl: %d, Average CC's: %f, Average CC's Ssl: %f, Num polls: %d)", testCase, usageRecord.getStartTime().getTime(), usageRecord.getEndTime().getTime(), usageRecord.getTags(), usageRecord.getEventType(), usageRecord.getIncomingTransfer(), usageRecord.getIncomingTransferSsl(), usageRecord.getOutgoingTransfer(), usageRecord.getOutgoingTransferSsl(), usageRecord.getAverageConcurrentConnections(), usageRecord.getAverageConcurrentConnectionsSsl(), usageRecord.getNumberOfPolls()));
        }
    }

    private static void assertTimestampsAreContiguous(List<Usage> usages) {
        Usage firstUsage = null;
        Usage secondUsage = null;

        for (int i = 0; i < usages.size(); i++) {
            if (firstUsage != null && secondUsage != null) {
                Assert.assertEquals(firstUsage.getEndTime().getTimeInMillis(), secondUsage.getStartTime().getTimeInMillis(), 0);
            }

            if( i < usages.size() - 1) {
                firstUsage = usages.get(i);
                secondUsage = usages.get(i + 1);
            }
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenProcessingWithNoRecentRecords {
        @Mock
        private UsageRepository rollUpUsageRepository;
        private UsageRollupProcessor usageRollupProcessor;

        private final int accountId = 1234;
        private final int loadBalancerId = 1;
        private LoadBalancerUsage loadBalancerUsageCreate;
        private LoadBalancerUsage loadBalancerUsageSslOn;
        private Calendar createUsageStartTime;
        private Calendar createUsageEndTime;
        private Calendar sslOnUsageStartTime;
        private Calendar sslOnUsageEndTime;
        private List<LoadBalancerUsage> inOrderUsages;

        @Before
        public void standUp() throws EntityNotFoundException, DeletedStatusException {
            createUsageStartTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 33, 10);
            createUsageEndTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 58, 26);
            sslOnUsageStartTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 59, 1);
            sslOnUsageEndTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 59, 45);

            loadBalancerUsageCreate = new LoadBalancerUsage(accountId, loadBalancerId, 0.0, 100l, 100l, 0l, 0l, 0.0, 0l, 0l, 0l, 0l, createUsageStartTime, createUsageEndTime, 1, 1, 0, "CREATE_LOADBALANCER");
            loadBalancerUsageSslOn = new LoadBalancerUsage(accountId, loadBalancerId, 0.0, 0l, 0l, 100l, 100l, 0.0, 0l, 0l, 0l, 0l, sslOnUsageStartTime, sslOnUsageEndTime, 0, 1, 5, "SSL_MIXED_ON");

            inOrderUsages = new ArrayList<LoadBalancerUsage>();
            inOrderUsages.add(loadBalancerUsageCreate);
            inOrderUsages.add(loadBalancerUsageSslOn);

            usageRollupProcessor = new UsageRollupProcessor(inOrderUsages, rollUpUsageRepository);

            when(rollUpUsageRepository.getMostRecentUsageForLoadBalancer(Matchers.<Integer>any())).thenReturn(null);
        }

        @Test
        public void shouldSucceedWhenUsagesAreBackToBackWithinTheSameHour() {
            usageRollupProcessor.process();
            final List<Usage> usagesToCreate = usageRollupProcessor.getUsagesToCreate();

            printUsageRecords("shouldSucceedWhenEventsAreBackToBackWithinTheSameHour", usagesToCreate);

            Assert.assertEquals(2, usagesToCreate.size());

            // Check timestamps
            assertTimestampsAreContiguous(usagesToCreate);
            Assert.assertEquals(createUsageStartTime, usagesToCreate.get(0).getStartTime());
            Assert.assertEquals(sslOnUsageStartTime, usagesToCreate.get(0).getEndTime());
            Assert.assertEquals(sslOnUsageStartTime, usagesToCreate.get(1).getStartTime());
            Assert.assertEquals(sslOnUsageEndTime.get(Calendar.DAY_OF_YEAR), usagesToCreate.get(1).getEndTime().get(Calendar.DAY_OF_YEAR));
            Assert.assertEquals(sslOnUsageEndTime.get(Calendar.HOUR_OF_DAY) + 1, usagesToCreate.get(1).getEndTime().get(Calendar.HOUR_OF_DAY));
            Assert.assertEquals(0, usagesToCreate.get(1).getEndTime().get(Calendar.MINUTE));
            Assert.assertEquals(0, usagesToCreate.get(1).getEndTime().get(Calendar.SECOND));
            Assert.assertEquals(0, usagesToCreate.get(1).getEndTime().get(Calendar.MILLISECOND));

            // Check tags
            Assert.assertEquals(0, usagesToCreate.get(0).getTags().intValue());
            Assert.assertEquals(BitTag.SSL.tagValue() + BitTag.SSL_MIXED_MODE.tagValue(), usagesToCreate.get(1).getTags().intValue());

            // Check usage
            Assert.assertEquals(loadBalancerUsageSslOn.getLastBandwidthBytesIn(), usagesToCreate.get(0).getIncomingTransfer());
            Assert.assertEquals(loadBalancerUsageSslOn.getLastBandwidthBytesInSsl(), usagesToCreate.get(0).getIncomingTransferSsl());
            Assert.assertEquals(loadBalancerUsageSslOn.getLastBandwidthBytesOut(), usagesToCreate.get(0).getOutgoingTransfer());
            Assert.assertEquals(loadBalancerUsageSslOn.getLastBandwidthBytesOutSsl(), usagesToCreate.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(new Double(0), usagesToCreate.get(0).getAverageConcurrentConnections());
            Assert.assertEquals(new Double(0), usagesToCreate.get(0).getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(new Integer(1), usagesToCreate.get(0).getNumberOfPolls());

            Assert.assertEquals(new Long(0), usagesToCreate.get(1).getIncomingTransfer());
            Assert.assertEquals(new Long(0), usagesToCreate.get(1).getIncomingTransferSsl());
            Assert.assertEquals(new Long(0), usagesToCreate.get(1).getOutgoingTransfer());
            Assert.assertEquals(new Long(0), usagesToCreate.get(1).getOutgoingTransferSsl());
            Assert.assertEquals(new Double(0), usagesToCreate.get(1).getAverageConcurrentConnections());
            Assert.assertEquals(new Double(0), usagesToCreate.get(1).getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(new Integer(0), usagesToCreate.get(1).getNumberOfPolls());

        }

        @Test
        public void shouldSucceedWhenEventsOccurInDifferentHours() {
            sslOnUsageStartTime.set(Calendar.HOUR_OF_DAY, 4);
            sslOnUsageEndTime.set(Calendar.HOUR_OF_DAY, 4);

            usageRollupProcessor.process();
            final List<Usage> usagesToCreate = usageRollupProcessor.getUsagesToCreate();

            printUsageRecords("shouldSucceedWhenEventsOccurInDifferentHours", usagesToCreate);

            Assert.assertEquals(3, usagesToCreate.size());

            // Check timestamps
            assertTimestampsAreContiguous(usagesToCreate);
            Assert.assertEquals(createUsageStartTime, usagesToCreate.get(0).getStartTime());
            Assert.assertEquals(sslOnUsageStartTime, usagesToCreate.get(2).getStartTime());
            Assert.assertEquals(sslOnUsageEndTime.get(Calendar.DAY_OF_YEAR), usagesToCreate.get(2).getEndTime().get(Calendar.DAY_OF_YEAR));
            Assert.assertEquals(sslOnUsageEndTime.get(Calendar.HOUR_OF_DAY) + 1, usagesToCreate.get(2).getEndTime().get(Calendar.HOUR_OF_DAY));
            Assert.assertEquals(0, usagesToCreate.get(2).getEndTime().get(Calendar.MINUTE));
            Assert.assertEquals(0, usagesToCreate.get(2).getEndTime().get(Calendar.SECOND));
            Assert.assertEquals(0, usagesToCreate.get(2).getEndTime().get(Calendar.MILLISECOND));

            // Check tags
            Assert.assertEquals(0, usagesToCreate.get(0).getTags().intValue());
            Assert.assertEquals(BitTag.SSL.tagValue() + BitTag.SSL_MIXED_MODE.tagValue(), usagesToCreate.get(2).getTags().intValue());

            // Check usage
            Assert.assertEquals(loadBalancerUsageSslOn.getLastBandwidthBytesIn(), usagesToCreate.get(0).getIncomingTransfer());
            Assert.assertEquals(loadBalancerUsageSslOn.getLastBandwidthBytesInSsl(), usagesToCreate.get(0).getIncomingTransferSsl());
            Assert.assertEquals(loadBalancerUsageSslOn.getLastBandwidthBytesOut(), usagesToCreate.get(0).getOutgoingTransfer());
            Assert.assertEquals(loadBalancerUsageSslOn.getLastBandwidthBytesOutSsl(), usagesToCreate.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(new Double(0), usagesToCreate.get(0).getAverageConcurrentConnections());
            Assert.assertEquals(new Double(0), usagesToCreate.get(0).getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(new Integer(1), usagesToCreate.get(0).getNumberOfPolls());

            Assert.assertEquals(new Long(0), usagesToCreate.get(1).getIncomingTransfer());
            Assert.assertEquals(new Long(0), usagesToCreate.get(1).getIncomingTransferSsl());
            Assert.assertEquals(new Long(0), usagesToCreate.get(1).getOutgoingTransfer());
            Assert.assertEquals(new Long(0), usagesToCreate.get(1).getOutgoingTransferSsl());
            Assert.assertEquals(new Double(0), usagesToCreate.get(1).getAverageConcurrentConnections());
            Assert.assertEquals(new Double(0), usagesToCreate.get(1).getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(new Integer(0), usagesToCreate.get(1).getNumberOfPolls());

            Assert.assertEquals(new Long(0), usagesToCreate.get(2).getIncomingTransfer());
            Assert.assertEquals(new Long(0), usagesToCreate.get(2).getIncomingTransferSsl());
            Assert.assertEquals(new Long(0), usagesToCreate.get(2).getOutgoingTransfer());
            Assert.assertEquals(new Long(0), usagesToCreate.get(2).getOutgoingTransferSsl());
            Assert.assertEquals(new Double(0), usagesToCreate.get(2).getAverageConcurrentConnections());
            Assert.assertEquals(new Double(0), usagesToCreate.get(2).getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(new Integer(0), usagesToCreate.get(2).getNumberOfPolls());
        }

        @Test
        public void shouldSucceedWhenEventsOccurInDifferentDays() {
            sslOnUsageStartTime.set(Calendar.DAY_OF_MONTH, 2);
            sslOnUsageEndTime.set(Calendar.DAY_OF_MONTH, 2);

            usageRollupProcessor.process();
            final List<Usage> usagesToCreate = usageRollupProcessor.getUsagesToCreate();

            printUsageRecords("shouldSucceedWhenEventsOccurInDifferentDays", usagesToCreate);

            Assert.assertEquals(26, usagesToCreate.size());

            // Check timestamps
            assertTimestampsAreContiguous(usagesToCreate);
            Assert.assertEquals(createUsageStartTime, usagesToCreate.get(0).getStartTime());
            Assert.assertEquals(sslOnUsageStartTime, usagesToCreate.get(25).getStartTime());
            Assert.assertEquals(sslOnUsageEndTime.get(Calendar.DAY_OF_YEAR), usagesToCreate.get(25).getEndTime().get(Calendar.DAY_OF_YEAR));
            Assert.assertEquals(sslOnUsageEndTime.get(Calendar.HOUR_OF_DAY) + 1, usagesToCreate.get(25).getEndTime().get(Calendar.HOUR_OF_DAY));
            Assert.assertEquals(0, usagesToCreate.get(25).getEndTime().get(Calendar.MINUTE));
            Assert.assertEquals(0, usagesToCreate.get(25).getEndTime().get(Calendar.SECOND));
            Assert.assertEquals(0, usagesToCreate.get(25).getEndTime().get(Calendar.MILLISECOND));

            // Check tags
            Assert.assertEquals(0, usagesToCreate.get(0).getTags().intValue());
            Assert.assertEquals(0, usagesToCreate.get(1).getTags().intValue());
            Assert.assertEquals(BitTag.SSL.tagValue() + BitTag.SSL_MIXED_MODE.tagValue(), usagesToCreate.get(25).getTags().intValue());

            // Check usage
            Assert.assertEquals(loadBalancerUsageSslOn.getLastBandwidthBytesIn(), usagesToCreate.get(0).getIncomingTransfer());
            Assert.assertEquals(loadBalancerUsageSslOn.getLastBandwidthBytesInSsl(), usagesToCreate.get(0).getIncomingTransferSsl());
            Assert.assertEquals(loadBalancerUsageSslOn.getLastBandwidthBytesOut(), usagesToCreate.get(0).getOutgoingTransfer());
            Assert.assertEquals(loadBalancerUsageSslOn.getLastBandwidthBytesOutSsl(), usagesToCreate.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(new Double(0), usagesToCreate.get(0).getAverageConcurrentConnections());
            Assert.assertEquals(new Double(0), usagesToCreate.get(0).getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(new Integer(1), usagesToCreate.get(0).getNumberOfPolls());

            Assert.assertEquals(new Long(0), usagesToCreate.get(25).getIncomingTransfer());
            Assert.assertEquals(new Long(0), usagesToCreate.get(25).getIncomingTransferSsl());
            Assert.assertEquals(new Long(0), usagesToCreate.get(25).getOutgoingTransfer());
            Assert.assertEquals(new Long(0), usagesToCreate.get(25).getOutgoingTransferSsl());
            Assert.assertEquals(new Double(0), usagesToCreate.get(25).getAverageConcurrentConnections());
            Assert.assertEquals(new Double(0), usagesToCreate.get(25).getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(new Integer(0), usagesToCreate.get(25).getNumberOfPolls());
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenProcessingWithARecentRecord {
        @Mock
        private UsageRepository rollUpUsageRepository;
        private UsageRollupProcessor usageRollupProcessor;

        private final int accountId = 1234;
        private final int loadBalancerId = 1;
        private LoadBalancerUsage regularUsageRecord;
        private LoadBalancerUsage loadBalancerUsageSslOn;
        private Calendar createUsageStartTime;
        private Calendar createUsageEndTime;
        private Calendar regularUsageStartTime;
        private Calendar regularUsageEndTime;
        private Calendar sslOnUsageStartTime;
        private Calendar sslOnUsageEndTime;
        private List<LoadBalancerUsage> inOrderUsages;
        private Usage recentUsage;

        @Before
        public void standUp() throws EntityNotFoundException, DeletedStatusException {
            createUsageStartTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 33, 10);
            createUsageEndTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 58, 26);
            regularUsageStartTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 4, 3, 10);
            regularUsageEndTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 4, 58, 26);
            sslOnUsageStartTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 4, 59, 1);
            sslOnUsageEndTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 4, 59, 45);

            LoadBalancer lb = new LoadBalancer();
            lb.setId(loadBalancerId);
            recentUsage = new Usage(lb, 100.0, 100l, 100l, 100.0, 100l, 100l, createUsageStartTime, createUsageEndTime, 5, 1, 0, "CREATE_LOADBALANCER", accountId, 0, false);
            recentUsage.setId(1234);
            regularUsageRecord = new LoadBalancerUsage(accountId, loadBalancerId, 0.0, 100l, 100l, 0l, 0l, 0.0, 0l, 0l, 0l, 0l, regularUsageStartTime, regularUsageEndTime, 12, 1, 0, null);
            loadBalancerUsageSslOn = new LoadBalancerUsage(accountId, loadBalancerId, 0.0, 0l, 0l, 100l, 100l, 0.0, 0l, 0l, 0l, 0l, sslOnUsageStartTime, sslOnUsageEndTime, 0, 1, 5, "SSL_MIXED_ON");

            inOrderUsages = new ArrayList<LoadBalancerUsage>();
            inOrderUsages.add(regularUsageRecord);
            inOrderUsages.add(loadBalancerUsageSslOn);

            usageRollupProcessor = new UsageRollupProcessor(inOrderUsages, rollUpUsageRepository);

            List<Usage> recentUsages = new ArrayList<Usage>();
            recentUsages.add(recentUsage);
            when(rollUpUsageRepository.getMostRecentUsageForLoadBalancers(Matchers.<Collection<Integer>>any())).thenReturn(recentUsages);
        }

        @Test
        public void shouldSucceedWhenUsagesAreBackToBackWithinTheSameHour() {
            usageRollupProcessor.process();
            final List<Usage> usagesToUpdate = usageRollupProcessor.getUsagesToUpdate();
            final List<Usage> usagesToCreate = usageRollupProcessor.getUsagesToCreate();

            printUsageRecords("shouldSucceedWhenEventsAreBackToBackWithinTheSameHour", usagesToUpdate);
            printUsageRecords("shouldSucceedWhenEventsAreBackToBackWithinTheSameHour", usagesToCreate);

            Assert.assertEquals(1, usagesToUpdate.size());
            Assert.assertEquals(2, usagesToCreate.size());

            // Check timestamps
            assertTimestampsAreContiguous(usagesToUpdate);
            assertTimestampsAreContiguous(usagesToCreate);
            Assert.assertEquals(createUsageStartTime, usagesToUpdate.get(0).getStartTime());

            Assert.assertEquals(sslOnUsageStartTime, usagesToCreate.get(1).getStartTime());
            Assert.assertEquals(sslOnUsageEndTime.get(Calendar.DAY_OF_YEAR), usagesToCreate.get(1).getEndTime().get(Calendar.DAY_OF_YEAR));
            Assert.assertEquals(sslOnUsageEndTime.get(Calendar.HOUR_OF_DAY) + 1, usagesToCreate.get(1).getEndTime().get(Calendar.HOUR_OF_DAY));
            Assert.assertEquals(0, usagesToCreate.get(1).getEndTime().get(Calendar.MINUTE));
            Assert.assertEquals(0, usagesToCreate.get(1).getEndTime().get(Calendar.SECOND));
            Assert.assertEquals(0, usagesToCreate.get(1).getEndTime().get(Calendar.MILLISECOND));

            // Check tags
            Assert.assertEquals(0, usagesToUpdate.get(0).getTags().intValue());
            Assert.assertEquals(BitTag.SSL.tagValue() + BitTag.SSL_MIXED_MODE.tagValue(), usagesToCreate.get(1).getTags().intValue());

            // Check usage
            Assert.assertEquals(new Long(100), usagesToUpdate.get(0).getIncomingTransfer());
            Assert.assertEquals(new Long(100), usagesToUpdate.get(0).getIncomingTransferSsl());
            Assert.assertEquals(new Long(100), usagesToUpdate.get(0).getOutgoingTransfer());
            Assert.assertEquals(new Long(100), usagesToUpdate.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(new Double(100), usagesToUpdate.get(0).getAverageConcurrentConnections());
            Assert.assertEquals(new Double(100), usagesToUpdate.get(0).getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(new Integer(5), usagesToUpdate.get(0).getNumberOfPolls());

            Assert.assertEquals(new Long(100), usagesToCreate.get(0).getIncomingTransfer());
            Assert.assertEquals(new Long(0), usagesToCreate.get(0).getIncomingTransferSsl());
            Assert.assertEquals(new Long(100), usagesToCreate.get(0).getOutgoingTransfer());
            Assert.assertEquals(new Long(0), usagesToCreate.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(new Double(0), usagesToCreate.get(0).getAverageConcurrentConnections());
            Assert.assertEquals(new Double(0), usagesToCreate.get(0).getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(new Integer(12), usagesToCreate.get(0).getNumberOfPolls());

            Assert.assertEquals(new Long(0), usagesToCreate.get(1).getIncomingTransfer());
            Assert.assertEquals(new Long(0), usagesToCreate.get(1).getIncomingTransferSsl());
            Assert.assertEquals(new Long(0), usagesToCreate.get(1).getOutgoingTransfer());
            Assert.assertEquals(new Long(0), usagesToCreate.get(1).getOutgoingTransferSsl());
            Assert.assertEquals(new Double(0), usagesToCreate.get(1).getAverageConcurrentConnections());
            Assert.assertEquals(new Double(0), usagesToCreate.get(1).getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(new Integer(0), usagesToCreate.get(1).getNumberOfPolls());
        }

        @Test
        public void shouldSucceedWhenEventsOccurInDifferentDays() {
            regularUsageStartTime.set(Calendar.DAY_OF_MONTH, 2);
            regularUsageEndTime.set(Calendar.DAY_OF_MONTH, 2);
            sslOnUsageStartTime.set(Calendar.DAY_OF_MONTH, 2);
            sslOnUsageEndTime.set(Calendar.DAY_OF_MONTH, 2);

            usageRollupProcessor.process();
            final List<Usage> usagesToUpdate = usageRollupProcessor.getUsagesToUpdate();
            final List<Usage> usagesToCreate = usageRollupProcessor.getUsagesToCreate();

            printUsageRecords("shouldSucceedWhenEventsOccurInDifferentDays", usagesToUpdate);
            printUsageRecords("shouldSucceedWhenEventsOccurInDifferentDays", usagesToCreate);

            Assert.assertEquals(1, usagesToUpdate.size());
            Assert.assertEquals(26, usagesToCreate.size());

            // Check timestamps
            assertTimestampsAreContiguous(usagesToUpdate);
            assertTimestampsAreContiguous(usagesToCreate);
            Assert.assertEquals(recentUsage.getStartTime(), usagesToUpdate.get(0).getStartTime());

            Assert.assertEquals(sslOnUsageStartTime, usagesToCreate.get(25).getStartTime());
            Assert.assertEquals(sslOnUsageEndTime.get(Calendar.DAY_OF_YEAR), usagesToCreate.get(25).getEndTime().get(Calendar.DAY_OF_YEAR));
            Assert.assertEquals(sslOnUsageEndTime.get(Calendar.HOUR_OF_DAY) + 1, usagesToCreate.get(25).getEndTime().get(Calendar.HOUR_OF_DAY));
            Assert.assertEquals(0, usagesToCreate.get(25).getEndTime().get(Calendar.MINUTE));
            Assert.assertEquals(0, usagesToCreate.get(25).getEndTime().get(Calendar.SECOND));
            Assert.assertEquals(0, usagesToCreate.get(25).getEndTime().get(Calendar.MILLISECOND));

            // Check tags
            Assert.assertEquals(0, usagesToUpdate.get(0).getTags().intValue());
            Assert.assertEquals(0, usagesToCreate.get(0).getTags().intValue());
            Assert.assertEquals(BitTag.SSL.tagValue() + BitTag.SSL_MIXED_MODE.tagValue(), usagesToCreate.get(25).getTags().intValue());

            // Check usage
            Assert.assertEquals(new Long(100), usagesToUpdate.get(0).getIncomingTransfer());
            Assert.assertEquals(new Long(100), usagesToUpdate.get(0).getIncomingTransferSsl());
            Assert.assertEquals(new Long(100), usagesToUpdate.get(0).getOutgoingTransfer());
            Assert.assertEquals(new Long(100), usagesToUpdate.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(new Double(100), usagesToUpdate.get(0).getAverageConcurrentConnections());
            Assert.assertEquals(new Double(100), usagesToUpdate.get(0).getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(new Integer(5), usagesToUpdate.get(0).getNumberOfPolls());

            Assert.assertEquals(new Long(100), usagesToCreate.get(24).getIncomingTransfer());
            Assert.assertEquals(new Long(0), usagesToCreate.get(24).getIncomingTransferSsl());
            Assert.assertEquals(new Long(100), usagesToCreate.get(24).getOutgoingTransfer());
            Assert.assertEquals(new Long(0), usagesToCreate.get(24).getOutgoingTransferSsl());
            Assert.assertEquals(new Double(0), usagesToCreate.get(24).getAverageConcurrentConnections());
            Assert.assertEquals(new Double(0), usagesToCreate.get(24).getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(new Integer(12), usagesToCreate.get(24).getNumberOfPolls());

            Assert.assertEquals(new Long(0), usagesToCreate.get(25).getIncomingTransfer());
            Assert.assertEquals(new Long(0), usagesToCreate.get(25).getIncomingTransferSsl());
            Assert.assertEquals(new Long(0), usagesToCreate.get(25).getOutgoingTransfer());
            Assert.assertEquals(new Long(0), usagesToCreate.get(25).getOutgoingTransferSsl());
            Assert.assertEquals(new Double(0), usagesToCreate.get(25).getAverageConcurrentConnections());
            Assert.assertEquals(new Double(0), usagesToCreate.get(25).getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(new Integer(0), usagesToCreate.get(25).getNumberOfPolls());
        }
    }


    @RunWith(MockitoJUnitRunner.class)
    public static class WhenProcessingWithARecentRecordCase2 {
        @Mock
        private UsageRepository rollUpUsageRepository;
        private UsageRollupProcessor usageRollupProcessor;

        private final int accountId = 1234;
        private final int loadBalancerId = 1;
        private LoadBalancerUsage regularUsageRecord;
        private Calendar createUsageStartTime;
        private Calendar createUsageEndTime;
        private Calendar regularUsageStartTime;
        private Calendar regularUsageEndTime;
        private List<LoadBalancerUsage> inOrderUsages;
        private Usage recentUsage;

        @Before
        public void standUp() throws EntityNotFoundException, DeletedStatusException {
            createUsageStartTime = new GregorianCalendar(2012, Calendar.SEPTEMBER, 18, 20, 25, 25);
            createUsageEndTime = new GregorianCalendar(2012, Calendar.SEPTEMBER, 18, 20, 25, 25);
            regularUsageStartTime = new GregorianCalendar(2012, Calendar.SEPTEMBER, 20, 11, 0, 19);
            regularUsageEndTime = new GregorianCalendar(2012, Calendar.SEPTEMBER, 20, 11, 56, 8);

            LoadBalancer lb = new LoadBalancer();
            lb.setId(loadBalancerId);
            recentUsage = new Usage(lb, 0.0, 0l, 0l, 0.0, 0l, 0l, createUsageStartTime, createUsageEndTime, 0, 0, 0, "CREATE_LOADBALANCER", accountId, 1, true);
            recentUsage.setId(1234);
            regularUsageRecord = new LoadBalancerUsage(accountId, loadBalancerId, 0.0, 0l, 0l, null, null, 0.0, 0l, 0l, null, null, regularUsageStartTime, regularUsageEndTime, 11, 1, 0, null);

            inOrderUsages = new ArrayList<LoadBalancerUsage>();
            inOrderUsages.add(regularUsageRecord);

            usageRollupProcessor = new UsageRollupProcessor(inOrderUsages, rollUpUsageRepository);

            List<Usage> recentUsages = new ArrayList<Usage>();
            recentUsages.add(recentUsage);
            when(rollUpUsageRepository.getMostRecentUsageForLoadBalancers(Matchers.<Collection<Integer>>any())).thenReturn(recentUsages);
        }

        @Test
        public void shouldSucceedWhenUsagesAreBackToBackWithinTheSameHour() {
            usageRollupProcessor.process();
            final List<Usage> usagesToUpdate = usageRollupProcessor.getUsagesToUpdate();
            final List<Usage> usagesToCreate = usageRollupProcessor.getUsagesToCreate();

            printUsageRecords("shouldSucceedWhenEventsAreBackToBackWithinTheSameHour", usagesToUpdate);
            printUsageRecords("shouldSucceedWhenEventsAreBackToBackWithinTheSameHour", usagesToCreate);

            Assert.assertEquals(1, usagesToUpdate.size());
            Assert.assertEquals(40, usagesToCreate.size());

            // Check timestamps
            assertTimestampsAreContiguous(usagesToUpdate);
            assertTimestampsAreContiguous(usagesToCreate);
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenProcessingWithARecentRecordCase3 {
        @Mock
        private UsageRepository rollUpUsageRepository;
        private UsageRollupProcessor usageRollupProcessor;

        private final int accountId = 1234;
        private final int loadBalancerId = 1;
        private LoadBalancerUsage regularUsageRecord;
        private Calendar createUsageStartTime;
        private Calendar createUsageEndTime;
        private Calendar regularUsageStartTime;
        private Calendar regularUsageEndTime;
        private List<LoadBalancerUsage> inOrderUsages;
        private Usage recentUsage;

        @Before
        public void standUp() throws EntityNotFoundException, DeletedStatusException {
            createUsageStartTime = new GregorianCalendar(2012, Calendar.SEPTEMBER, 25, 16, 8, 16);
            createUsageEndTime = new GregorianCalendar(2012, Calendar.SEPTEMBER, 25, 16, 59, 59);
            regularUsageStartTime = new GregorianCalendar(2012, Calendar.SEPTEMBER, 25, 17, 3, 40);
            regularUsageEndTime = new GregorianCalendar(2012, Calendar.SEPTEMBER, 25, 17, 58, 39);

            LoadBalancer lb = new LoadBalancer();
            lb.setId(loadBalancerId);
            recentUsage = new Usage(lb, 9.09090909090908, 26500l, 783835032l, 0.0, 0l, 0l, createUsageStartTime, createUsageEndTime, 11, 1, 0, "CREATE_LOADBALANCER", accountId, 1, true);
            recentUsage.setId(1234);
            regularUsageRecord = new LoadBalancerUsage(accountId, loadBalancerId, 0.0, 1038l, 3073l, 27538l, 783838105l, 0.0, 0l, 0l, null, null, regularUsageStartTime, regularUsageEndTime, 12, 1, 0, null);

            inOrderUsages = new ArrayList<LoadBalancerUsage>();
            inOrderUsages.add(regularUsageRecord);

            usageRollupProcessor = new UsageRollupProcessor(inOrderUsages, rollUpUsageRepository);

            List<Usage> recentUsages = new ArrayList<Usage>();
            recentUsages.add(recentUsage);
            when(rollUpUsageRepository.getMostRecentUsageForLoadBalancers(Matchers.<Collection<Integer>>any())).thenReturn(recentUsages);
        }

        @Test
        public void shouldSucceedWhenUsagesAreBackToBackWithinTheSameHour() {
            usageRollupProcessor.process();
            final List<Usage> usagesToUpdate = usageRollupProcessor.getUsagesToUpdate();
            final List<Usage> usagesToCreate = usageRollupProcessor.getUsagesToCreate();

            printUsageRecords("shouldSucceedWhenEventsAreBackToBackWithinTheSameHour", usagesToUpdate);
            printUsageRecords("shouldSucceedWhenEventsAreBackToBackWithinTheSameHour", usagesToCreate);

            Assert.assertEquals(1, usagesToUpdate.size());
            Assert.assertEquals(1, usagesToCreate.size());

            // Check timestamps
            assertTimestampsAreContiguous(usagesToUpdate);
            assertTimestampsAreContiguous(usagesToCreate);
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenProcessingSuspendedRecords {
        @Mock
        private UsageRepository rollUpUsageRepository;
        private UsageRollupProcessor usageRollupProcessor;

        private final int accountId = 1234;
        private final int loadBalancerId = 1;
        private LoadBalancerUsage regularUsageRecord;
        private Calendar createUsageStartTime;
        private Calendar createUsageEndTime;
        private Calendar regularUsageStartTime;
        private Calendar regularUsageEndTime;
        private List<LoadBalancerUsage> inOrderUsages;
        private Usage recentUsage;

        @Before
        public void standUp() throws EntityNotFoundException, DeletedStatusException {
            createUsageStartTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 33, 10);
            createUsageEndTime = new GregorianCalendar(2012, Calendar.JUNE, 1, 3, 58, 26);
            regularUsageStartTime = new GregorianCalendar(2012, Calendar.JUNE, 2, 4, 3, 10);
            regularUsageEndTime = new GregorianCalendar(2012, Calendar.JUNE, 2, 4, 58, 26);

            LoadBalancer lb = new LoadBalancer();
            lb.setId(loadBalancerId);
            recentUsage = new Usage(lb, 0.0, 0l, 0l, 0.0, 0l, 0l, createUsageStartTime, createUsageEndTime, 0, 1, BitTag.SERVICENET_LB.tagValue(), "SUSPEND_LOADBALANCER", accountId, 0, false);
            recentUsage.setId(1234);
            regularUsageRecord = new LoadBalancerUsage(accountId, loadBalancerId, 0.0, 0l, 0l, 0l, 0l, 0.0, 0l, 0l, 0l, 0l, regularUsageStartTime, regularUsageEndTime, 0, 1, BitTag.SERVICENET_LB.tagValue(), "SUSPENDED_LOADBALANCER");

            inOrderUsages = new ArrayList<LoadBalancerUsage>();
            inOrderUsages.add(regularUsageRecord);

            usageRollupProcessor = new UsageRollupProcessor(inOrderUsages, rollUpUsageRepository);

            List<Usage> recentUsages = new ArrayList<Usage>();
            recentUsages.add(recentUsage);
            when(rollUpUsageRepository.getMostRecentUsageForLoadBalancers(Matchers.<Collection<Integer>>any())).thenReturn(recentUsages);
        }

        @Test
        public void shouldSucceed() {
            usageRollupProcessor.process();
            final List<Usage> usagesToUpdate = usageRollupProcessor.getUsagesToUpdate();
            final List<Usage> usagesToCreate = usageRollupProcessor.getUsagesToCreate();

            printUsageRecords("shouldSucceedWhenEventsAreBackToBackWithinTheSameHour", usagesToUpdate);
            printUsageRecords("shouldSucceedWhenEventsAreBackToBackWithinTheSameHour", usagesToCreate);

            Assert.assertEquals(1, usagesToUpdate.size());
            Assert.assertEquals(25, usagesToCreate.size());

            // Check timestamps
            assertTimestampsAreContiguous(usagesToUpdate);
            assertTimestampsAreContiguous(usagesToCreate);
            Assert.assertEquals(createUsageStartTime, usagesToUpdate.get(0).getStartTime());

            Assert.assertEquals(0, usagesToCreate.get(1).getEndTime().get(Calendar.MINUTE));
            Assert.assertEquals(0, usagesToCreate.get(1).getEndTime().get(Calendar.SECOND));
            Assert.assertEquals(0, usagesToCreate.get(1).getEndTime().get(Calendar.MILLISECOND));

            // Check tags
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue(), usagesToUpdate.get(0).getTags().intValue());
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue(), usagesToCreate.get(1).getTags().intValue());

            // Check usage
            Assert.assertEquals(new Long(0), usagesToUpdate.get(0).getIncomingTransfer());
            Assert.assertEquals(new Long(0), usagesToUpdate.get(0).getIncomingTransferSsl());
            Assert.assertEquals(new Long(0), usagesToUpdate.get(0).getOutgoingTransfer());
            Assert.assertEquals(new Long(0), usagesToUpdate.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(new Double(0), usagesToUpdate.get(0).getAverageConcurrentConnections());
            Assert.assertEquals(new Double(0), usagesToUpdate.get(0).getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(new Integer(0), usagesToUpdate.get(0).getNumberOfPolls());

            for (Usage usage : usagesToCreate) {
                Assert.assertEquals(new Long(0), usage.getIncomingTransfer());
                Assert.assertEquals(new Long(0), usage.getIncomingTransferSsl());
                Assert.assertEquals(new Long(0), usage.getOutgoingTransfer());
                Assert.assertEquals(new Long(0), usage.getOutgoingTransferSsl());
                Assert.assertEquals(new Double(0), usage.getAverageConcurrentConnections());
                Assert.assertEquals(new Double(0), usage.getAverageConcurrentConnectionsSsl());
                Assert.assertEquals(new Integer(0), usage.getNumberOfPolls());                
            }

            // Check that events are all SUSPENDED_LOADBALANCER
            for (Usage usage : usagesToCreate) {
                Assert.assertEquals(UsageEvent.SUSPENDED_LOADBALANCER.name(), usage.getEventType());
            }
        }
    }
}
