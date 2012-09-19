package org.openstack.atlas.usage.logic;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
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
            Assert.assertEquals(loadBalancerUsageCreate.getStartTime(), usagesToCreate.get(0).getStartTime());
            Assert.assertEquals(loadBalancerUsageSslOn.getStartTime(), usagesToCreate.get(0).getEndTime());
            Assert.assertEquals(loadBalancerUsageSslOn.getStartTime(), usagesToCreate.get(1).getStartTime());
            Assert.assertEquals(loadBalancerUsageSslOn.getEndTime(), usagesToCreate.get(1).getEndTime());

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

            Assert.assertEquals(2, usagesToCreate.size());

            // Check timestamps
            Assert.assertEquals(loadBalancerUsageCreate.getStartTime(), usagesToCreate.get(0).getStartTime());
            Assert.assertEquals(loadBalancerUsageSslOn.getStartTime(), usagesToCreate.get(0).getEndTime());
            Assert.assertEquals(loadBalancerUsageSslOn.getStartTime(), usagesToCreate.get(1).getStartTime());
            Assert.assertEquals(loadBalancerUsageSslOn.getEndTime(), usagesToCreate.get(1).getEndTime());

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
        public void shouldSucceedWhenEventsOccurInDifferentDays() {
            sslOnUsageStartTime.set(Calendar.DAY_OF_MONTH, 2);
            sslOnUsageEndTime.set(Calendar.DAY_OF_MONTH, 2);

            usageRollupProcessor.process();
            final List<Usage> usagesToCreate = usageRollupProcessor.getUsagesToCreate();

            printUsageRecords("shouldSucceedWhenEventsOccurInDifferentDays", usagesToCreate);

            Assert.assertEquals(3, usagesToCreate.size());

            // Check timestamps
            Assert.assertEquals(loadBalancerUsageCreate.getStartTime(), usagesToCreate.get(0).getStartTime());
            Assert.assertEquals(usagesToCreate.get(1).getStartTime().getTimeInMillis() - 1, usagesToCreate.get(0).getEndTime().getTimeInMillis());
            Assert.assertEquals(loadBalancerUsageSslOn.getStartTime(), usagesToCreate.get(1).getEndTime());
            Assert.assertEquals(loadBalancerUsageSslOn.getStartTime(), usagesToCreate.get(2).getStartTime());
            Assert.assertEquals(loadBalancerUsageSslOn.getEndTime(), usagesToCreate.get(2).getEndTime());

            // Check tags
            Assert.assertEquals(0, usagesToCreate.get(0).getTags().intValue());
            Assert.assertEquals(0, usagesToCreate.get(1).getTags().intValue());
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
            Assert.assertEquals(1, usagesToCreate.size());

            // Check timestamps
            Assert.assertEquals(recentUsage.getStartTime(), usagesToUpdate.get(0).getStartTime());
            Assert.assertEquals(loadBalancerUsageSslOn.getStartTime(), usagesToUpdate.get(0).getEndTime());

            Assert.assertEquals(loadBalancerUsageSslOn.getStartTime(), usagesToCreate.get(0).getStartTime());
            Assert.assertEquals(loadBalancerUsageSslOn.getEndTime(), usagesToCreate.get(0).getEndTime());

            // Check tags
            Assert.assertEquals(0, usagesToUpdate.get(0).getTags().intValue());
            Assert.assertEquals(BitTag.SSL.tagValue() + BitTag.SSL_MIXED_MODE.tagValue(), usagesToCreate.get(0).getTags().intValue());

            // Check usage
            Assert.assertEquals(new Long(200), usagesToUpdate.get(0).getIncomingTransfer());
            Assert.assertEquals(new Long(100), usagesToUpdate.get(0).getIncomingTransferSsl());
            Assert.assertEquals(new Long(200), usagesToUpdate.get(0).getOutgoingTransfer());
            Assert.assertEquals(new Long(100), usagesToUpdate.get(0).getOutgoingTransferSsl());
//            Assert.assertEquals(new Double(0), usagesToUpdate.get(0).getAverageConcurrentConnections());
//            Assert.assertEquals(new Double(0), usagesToUpdate.get(0).getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(new Integer(17), usagesToUpdate.get(0).getNumberOfPolls());

            Assert.assertEquals(new Long(0), usagesToCreate.get(0).getIncomingTransfer());
            Assert.assertEquals(new Long(0), usagesToCreate.get(0).getIncomingTransferSsl());
            Assert.assertEquals(new Long(0), usagesToCreate.get(0).getOutgoingTransfer());
            Assert.assertEquals(new Long(0), usagesToCreate.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(new Double(0), usagesToCreate.get(0).getAverageConcurrentConnections());
            Assert.assertEquals(new Double(0), usagesToCreate.get(0).getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(new Integer(0), usagesToCreate.get(0).getNumberOfPolls());

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
            Assert.assertEquals(2, usagesToCreate.size());

            // Check timestamps
            Assert.assertEquals(recentUsage.getStartTime(), usagesToUpdate.get(0).getStartTime());
            Assert.assertEquals(usagesToCreate.get(0).getStartTime().getTimeInMillis() - 1, usagesToUpdate.get(0).getEndTime().getTimeInMillis());

            Assert.assertEquals(loadBalancerUsageSslOn.getStartTime(), usagesToCreate.get(0).getEndTime());
            Assert.assertEquals(loadBalancerUsageSslOn.getStartTime(), usagesToCreate.get(1).getStartTime());
            Assert.assertEquals(loadBalancerUsageSslOn.getEndTime(), usagesToCreate.get(1).getEndTime());

            // Check tags
            Assert.assertEquals(0, usagesToUpdate.get(0).getTags().intValue());
            Assert.assertEquals(0, usagesToCreate.get(0).getTags().intValue());
            Assert.assertEquals(BitTag.SSL.tagValue() + BitTag.SSL_MIXED_MODE.tagValue(), usagesToCreate.get(1).getTags().intValue());

            // Check usage
            Assert.assertEquals(new Long(100), usagesToUpdate.get(0).getIncomingTransfer());
            Assert.assertEquals(new Long(100), usagesToUpdate.get(0).getIncomingTransferSsl());
            Assert.assertEquals(new Long(100), usagesToUpdate.get(0).getOutgoingTransfer());
            Assert.assertEquals(new Long(100), usagesToUpdate.get(0).getOutgoingTransferSsl());
//            Assert.assertEquals(new Double(0), usagesToUpdate.get(0).getAverageConcurrentConnections());
//            Assert.assertEquals(new Double(0), usagesToUpdate.get(0).getAverageConcurrentConnectionsSsl());
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
    }
}
