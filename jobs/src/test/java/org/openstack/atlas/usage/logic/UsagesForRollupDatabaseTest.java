package org.openstack.atlas.usage.logic;

import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.usage.helpers.ConfigurationKeys;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class UsagesForRollupDatabaseTest {
/*    public static class WhenRollingUpLoadBalancerWithNoActivity {
        private static Configuration configuration;
        private Map<Integer, List<UsagesForDay>> lbIdUsageMap;
        private Map<Integer, Usage> lbIdRollupUsageMap;
        private Integer lbId;
        private UsageRollupProcessor usagesForRollupDatabase;

        @BeforeClass
        public static void standUpMocks() {
            configuration = mock(Configuration.class);
            when(configuration.getString(ConfigurationKeys.usage_timezone_code)).thenReturn("CST");
        }

        @Before
        public void standUp() {
            lbId = 1234;
            lbIdUsageMap = new HashMap<Integer, List<UsagesForDay>>();
            lbIdRollupUsageMap = new HashMap<Integer, Usage>();
            usagesForRollupDatabase = new UsageRollupProcessor(lbIdUsageMap, lbIdRollupUsageMap);
            usagesForRollupDatabase.setConfiguration(configuration);
        }

        @Test
        public void shouldReturnOneEntryToInsertWhenRollupDatabaseContainsNoMatchingEntries() {
            Calendar startTime = new GregorianCalendar(2010, Calendar.NOVEMBER, 4, 1, 5, 0);
            Calendar endTime = new GregorianCalendar(2010, Calendar.NOVEMBER, 4, 1, 25, 0);
            LoadBalancerUsage usage = createPolledUsageRecord(lbId, 50.0, 100l, 200l, 95l, 195l, 50.0, 100l, 200l, 95l, 195l, 5, 2, 0, startTime, endTime, null);

            UsagesForDay usagesForDay = new UsagesForDay();
            usagesForDay.getUsages().add(usage);
            usagesForDay.setDayOfYear(startTime.get(Calendar.DAY_OF_YEAR));

            List<UsagesForDay> polledUsageRecords = new ArrayList<UsagesForDay>();
            polledUsageRecords.add(usagesForDay);

            lbIdUsageMap.put(lbId, polledUsageRecords);

            usagesForRollupDatabase.process();

            Assert.assertEquals(1, usagesForRollupDatabase.getUsagesToCreate().size());
            Assert.assertEquals(0, usagesForRollupDatabase.getUsagesToUpdate().size());

            Usage usageRecordToInsert = usagesForRollupDatabase.getUsagesToCreate().get(0);
            Assert.assertEquals(50.0, 0.0, usageRecordToInsert.getAverageConcurrentConnections());
            Assert.assertEquals(50.0, 0.0, usageRecordToInsert.getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(100, usageRecordToInsert.getIncomingTransfer().longValue());
            Assert.assertEquals(100, usageRecordToInsert.getIncomingTransferSsl().longValue());
            Assert.assertEquals(200, usageRecordToInsert.getOutgoingTransfer().longValue());
            Assert.assertEquals(200, usageRecordToInsert.getOutgoingTransferSsl().longValue());
            Assert.assertEquals(5, usageRecordToInsert.getNumberOfPolls().intValue());
            Assert.assertEquals(2, usageRecordToInsert.getNumVips().intValue());
        }

        @Test
        public void shouldReturnOneEntryToInsertWhenMergingMultipleEntriesAndRollupDatabaseContainsNoMatchingEntries() {
            Calendar startTime1 = new GregorianCalendar(2010, Calendar.NOVEMBER, 4, 1, 5, 0);
            Calendar endTime1 = new GregorianCalendar(2010, Calendar.NOVEMBER, 4, 1, 25, 0);
            Calendar startTime2 = new GregorianCalendar(2010, Calendar.NOVEMBER, 4, 2, 5, 0);
            Calendar endTime2 = new GregorianCalendar(2010, Calendar.NOVEMBER, 4, 2, 25, 0);
            LoadBalancerUsage usage1 = createPolledUsageRecord(lbId, 50.0, 100l, 200l, 95l, 195l, 50.0, 100l, 200l, 95l, 195l, 5, 2, 0, startTime1, endTime1, null);
            LoadBalancerUsage usage2 = createPolledUsageRecord(lbId, 100.0, 100l, 200l, 95l, 195l, 100.0, 100l, 200l, 95l, 195l, 5, 2, 0, startTime2, endTime2, null);

            UsagesForDay usagesForDay = new UsagesForDay();
            usagesForDay.getUsages().add(usage1);
            usagesForDay.getUsages().add(usage2);
            usagesForDay.setDayOfYear(startTime1.get(Calendar.DAY_OF_YEAR));

            List<UsagesForDay> polledUsageRecords = new ArrayList<UsagesForDay>();
            polledUsageRecords.add(usagesForDay);

            lbIdUsageMap.put(lbId, polledUsageRecords);

            usagesForRollupDatabase.process();

            Assert.assertEquals(1, usagesForRollupDatabase.getUsagesToCreate().size());
            Assert.assertEquals(0, usagesForRollupDatabase.getUsagesToUpdate().size());

            Usage usageRecordToInsert = usagesForRollupDatabase.getUsagesToCreate().get(0);
            Assert.assertEquals(75.0, 0.0, usageRecordToInsert.getAverageConcurrentConnections());
            Assert.assertEquals(75.0, 0.0, usageRecordToInsert.getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(200, usageRecordToInsert.getIncomingTransfer().longValue());
            Assert.assertEquals(200, usageRecordToInsert.getIncomingTransferSsl().longValue());
            Assert.assertEquals(400, usageRecordToInsert.getOutgoingTransfer().longValue());
            Assert.assertEquals(400, usageRecordToInsert.getOutgoingTransferSsl().longValue());
            Assert.assertEquals(10, usageRecordToInsert.getNumberOfPolls().intValue());
            Assert.assertEquals(2, usageRecordToInsert.getNumVips().intValue());
        }

        @Test
        public void shouldReturnTwoEntriesToInsertWhenRollupDatabaseContainsNoMatchingEntriesAndThereIsABitMaskChange() {
            Calendar startTime1 = new GregorianCalendar(2010, Calendar.NOVEMBER, 4, 1, 5, 0);
            Calendar endTime1 = new GregorianCalendar(2010, Calendar.NOVEMBER, 4, 1, 25, 0);
            Calendar startTime2 = new GregorianCalendar(2010, Calendar.NOVEMBER, 4, 2, 5, 0);
            Calendar endTime2 = new GregorianCalendar(2010, Calendar.NOVEMBER, 4, 2, 25, 0);
            LoadBalancerUsage usage1 = createPolledUsageRecord(lbId, 50.0, 100l, 200l, 95l, 195l, 50.0, 100l, 200l, 95l, 195l, 5, 2, 0, startTime1, endTime1, null);
            LoadBalancerUsage usage2 = createPolledUsageRecord(lbId, 100.0, 100l, 200l, 95l, 195l, 100.0, 100l, 200l, 95l, 195l, 5, 2, 1, startTime2, endTime2, null);

            UsagesForDay usagesForDay = new UsagesForDay();
            usagesForDay.getUsages().add(usage1);
            usagesForDay.getUsages().add(usage2);
            usagesForDay.setDayOfYear(startTime1.get(Calendar.DAY_OF_YEAR));

            List<UsagesForDay> polledUsageRecords = new ArrayList<UsagesForDay>();
            polledUsageRecords.add(usagesForDay);

            lbIdUsageMap.put(lbId, polledUsageRecords);

            usagesForRollupDatabase.process();

            Assert.assertEquals(2, usagesForRollupDatabase.getUsagesToCreate().size());
            Assert.assertEquals(0, usagesForRollupDatabase.getUsagesToUpdate().size());

            Usage usageRecordToInsert = usagesForRollupDatabase.getUsagesToCreate().get(0);
            Assert.assertEquals(50.0, 0.0, usageRecordToInsert.getAverageConcurrentConnections());
            Assert.assertEquals(50.0, 0.0, usageRecordToInsert.getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(100, usageRecordToInsert.getIncomingTransfer().longValue());
            Assert.assertEquals(100, usageRecordToInsert.getIncomingTransferSsl().longValue());
            Assert.assertEquals(200, usageRecordToInsert.getOutgoingTransfer().longValue());
            Assert.assertEquals(200, usageRecordToInsert.getOutgoingTransferSsl().longValue());
            Assert.assertEquals(5, usageRecordToInsert.getNumberOfPolls().intValue());
            Assert.assertEquals(2, usageRecordToInsert.getNumVips().intValue());
        }

        @Test
        public void shouldReturnTwoEntriesToInsertWhenRollupDatabaseContainsTwoContiguousEvents() {
            Calendar startTime1 = new GregorianCalendar(2010, Calendar.NOVEMBER, 4, 1, 5, 0);
            Calendar endTime1 = new GregorianCalendar(2010, Calendar.NOVEMBER, 4, 1, 5, 0);
            Calendar startTime2 = new GregorianCalendar(2010, Calendar.NOVEMBER, 4, 1, 6, 0);
            Calendar endTime2 = new GregorianCalendar(2010, Calendar.NOVEMBER, 4, 1, 6, 0);
            LoadBalancerUsage usage1 = createPolledUsageRecord(lbId, 0.0, 0l, 0l, 0l, 0l, 0.0, 0l, 0l, 0l, 0l, 0, 1, 0, startTime1, endTime1, UsageEvent.CREATE_LOADBALANCER);
            LoadBalancerUsage usage2 = createPolledUsageRecord(lbId, 0.0, 0l, 0l, 0l, 0l, 0.0, 0l, 0l, 0l, 0l, 0, 1, 0, startTime2, endTime2, UsageEvent.SUSPEND_LOADBALANCER);

            UsagesForDay usagesForDay = new UsagesForDay();
            usagesForDay.getUsages().add(usage1);
            usagesForDay.getUsages().add(usage2);
            usagesForDay.setDayOfYear(startTime1.get(Calendar.DAY_OF_YEAR));

            List<UsagesForDay> polledUsageRecords = new ArrayList<UsagesForDay>();
            polledUsageRecords.add(usagesForDay);

            lbIdUsageMap.put(lbId, polledUsageRecords);

            usagesForRollupDatabase.process();

            Assert.assertEquals(2, usagesForRollupDatabase.getUsagesToCreate().size());
            Assert.assertEquals(0, usagesForRollupDatabase.getUsagesToUpdate().size());

            Usage usageRecordToInsert = usagesForRollupDatabase.getUsagesToCreate().get(0);
            Assert.assertEquals(0.0, 0.0, usageRecordToInsert.getAverageConcurrentConnections());
            Assert.assertEquals(0.0, 0.0, usageRecordToInsert.getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(0, usageRecordToInsert.getIncomingTransfer().longValue());
            Assert.assertEquals(0, usageRecordToInsert.getIncomingTransferSsl().longValue());
            Assert.assertEquals(0, usageRecordToInsert.getOutgoingTransfer().longValue());
            Assert.assertEquals(0, usageRecordToInsert.getOutgoingTransferSsl().longValue());
            Assert.assertEquals(0, usageRecordToInsert.getNumberOfPolls().intValue());
            Assert.assertEquals(1, usageRecordToInsert.getNumVips().intValue());
            Assert.assertEquals("CREATE_LOADBALANCER", usageRecordToInsert.getEventType());

            usageRecordToInsert = usagesForRollupDatabase.getUsagesToCreate().get(1);
            Assert.assertEquals(0.0, 0.0, usageRecordToInsert.getAverageConcurrentConnections());
            Assert.assertEquals(0.0, 0.0, usageRecordToInsert.getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(0, usageRecordToInsert.getIncomingTransfer().longValue());
            Assert.assertEquals(0, usageRecordToInsert.getIncomingTransferSsl().longValue());
            Assert.assertEquals(0, usageRecordToInsert.getOutgoingTransfer().longValue());
            Assert.assertEquals(0, usageRecordToInsert.getOutgoingTransferSsl().longValue());
            Assert.assertEquals(0, usageRecordToInsert.getNumberOfPolls().intValue());
            Assert.assertEquals(1, usageRecordToInsert.getNumVips().intValue());
            Assert.assertEquals("SUSPEND_LOADBALANCER", usageRecordToInsert.getEventType());
        }

        @Test
        public void shouldReturnOneEntryToInsertWhenEventFollowedByNonEvent() {
            Calendar startTime1 = new GregorianCalendar(2010, Calendar.NOVEMBER, 4, 1, 5, 0);
            Calendar endTime1 = new GregorianCalendar(2010, Calendar.NOVEMBER, 4, 1, 5, 0);
            Calendar startTime2 = new GregorianCalendar(2010, Calendar.NOVEMBER, 4, 1, 6, 0);
            Calendar endTime2 = new GregorianCalendar(2010, Calendar.NOVEMBER, 4, 1, 6, 0);
            LoadBalancerUsage usage1 = createPolledUsageRecord(lbId, 1.0, 1l, 1l, 1l, 1l, 1.0, 1l, 1l, 1l, 1l, 0, 1, 0, startTime1, endTime1, UsageEvent.CREATE_LOADBALANCER);
            LoadBalancerUsage usage2 = createPolledUsageRecord(lbId, 1.0, 1l, 1l, 1l, 1l, 1.0, 1l, 1l, 1l, 1l, 1, 1, 0, startTime2, endTime2, null);

            UsagesForDay usagesForDay = new UsagesForDay();
            usagesForDay.getUsages().add(usage1);
            usagesForDay.getUsages().add(usage2);
            usagesForDay.setDayOfYear(startTime1.get(Calendar.DAY_OF_YEAR));

            List<UsagesForDay> polledUsageRecords = new ArrayList<UsagesForDay>();
            polledUsageRecords.add(usagesForDay);

            lbIdUsageMap.put(lbId, polledUsageRecords);

            usagesForRollupDatabase.process();

            Assert.assertEquals(1, usagesForRollupDatabase.getUsagesToCreate().size());
            Assert.assertEquals(0, usagesForRollupDatabase.getUsagesToUpdate().size());

            Usage usageRecordToInsert = usagesForRollupDatabase.getUsagesToCreate().get(0);
            Assert.assertEquals(1.0, 0.0, usageRecordToInsert.getAverageConcurrentConnections());
            Assert.assertEquals(1.0, 0.0, usageRecordToInsert.getAverageConcurrentConnectionsSsl());
            Assert.assertEquals(2, usageRecordToInsert.getIncomingTransfer().longValue());
            Assert.assertEquals(2, usageRecordToInsert.getIncomingTransferSsl().longValue());
            Assert.assertEquals(2, usageRecordToInsert.getOutgoingTransfer().longValue());
            Assert.assertEquals(2, usageRecordToInsert.getOutgoingTransferSsl().longValue());
            Assert.assertEquals(1, usageRecordToInsert.getNumberOfPolls().intValue());
            Assert.assertEquals(1, usageRecordToInsert.getNumVips().intValue());
            Assert.assertEquals("CREATE_LOADBALANCER", usageRecordToInsert.getEventType());
        }

        private LoadBalancerUsage createPolledUsageRecord(Integer lbId, Double avgCcs, Long cumBIn, Long cumBOut, Long lastBIn, Long lastBOut, Double avgCcsSsl, Long cumBInSsl, Long cumBOutSsl, Long lastBInSsl, Long lastBOutSsl, Integer numPolls, Integer numVips, Integer bitMask, Calendar startTime, Calendar endTime, UsageEvent eventType) {
            LoadBalancerUsage usage = new LoadBalancerUsage();
            usage.setLoadbalancerId(lbId);
            usage.setAverageConcurrentConnections(avgCcs);
            usage.setAverageConcurrentConnectionsSsl(avgCcsSsl);
            usage.setCumulativeBandwidthBytesIn(cumBIn);
            usage.setCumulativeBandwidthBytesInSsl(cumBInSsl);
            usage.setCumulativeBandwidthBytesOut(cumBOut);
            usage.setCumulativeBandwidthBytesOutSsl(cumBOutSsl);
            usage.setLastBandwidthBytesIn(lastBIn);
            usage.setLastBandwidthBytesInSsl(lastBInSsl);
            usage.setLastBandwidthBytesOut(lastBOut);
            usage.setLastBandwidthBytesOutSsl(lastBOutSsl);
            usage.setNumberOfPolls(numPolls);
            usage.setNumVips(numVips);
            usage.setTags(bitMask);
            usage.setStartTime(startTime);
            usage.setEndTime(endTime);
            if(eventType != null) usage.setEventType(eventType.toString());
            return usage;
        }
    }*/
}
