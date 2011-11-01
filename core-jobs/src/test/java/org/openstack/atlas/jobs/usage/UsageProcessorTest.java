package org.openstack.atlas.jobs.usage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.jobs.usage.helper.UsageRecordHelper;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.UsageRecord;
import org.openstack.atlas.service.domain.repository.UsageRepository;

import java.util.*;

import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class UsageProcessorTest {

    public static class WhenPerformingUnderNormalOperation {
        private Map<Integer, Long> bytesInMap;
        private Map<Integer, Long> bytesOutMap;
        private UsageRepository usageRepository;
        private UsageProcessor usageProcessor;
        private List<LoadBalancer> loadBalancers;
        LoadBalancer lb1;
        LoadBalancer lb2;

        @Before
        public void standUp() {
            loadBalancers = new ArrayList<LoadBalancer>();
            lb1 = new LoadBalancer();
            lb1.setId(1);
            lb2 = new LoadBalancer();
            lb2.setId(2);
            loadBalancers.add(lb1);
            loadBalancers.add(lb2);

            bytesInMap = new HashMap<Integer, Long>();
            bytesOutMap = new HashMap<Integer, Long>();
            usageRepository = mock(UsageRepository.class);
            usageProcessor = new UsageProcessor(usageRepository, bytesInMap, bytesOutMap);
        }

        @Test
        public void shouldNotReturnAnyRecordsWhenNoUsage() {
            usageProcessor.execute(loadBalancers);
            Assert.assertTrue(usageProcessor.getRecordsToInsert().isEmpty());
            Assert.assertTrue(usageProcessor.getRecordsToUpdate().isEmpty());
        }

        @Test
        public void shouldReturnOnlyNewRecordsWhenNoneCurrentlyExist() {
            bytesInMap.put(lb1.getId(), 100l);
            bytesOutMap.put(lb1.getId(), 1000l);
            bytesInMap.put(lb2.getId(), 200l);
            bytesOutMap.put(lb2.getId(), 2000l);

            when(usageRepository.getMostRecentUsageRecordsForLoadBalancers(anySetOf(Integer.class))).thenReturn(new ArrayList<UsageRecord>());

            usageProcessor.execute(loadBalancers);
            Assert.assertTrue(usageProcessor.getRecordsToUpdate().isEmpty());
            Assert.assertFalse(usageProcessor.getRecordsToInsert().isEmpty());

            for (UsageRecord usageRecord : usageProcessor.getRecordsToInsert()) {
                if (usageRecord.getLoadBalancer().getId().equals(lb1.getId())) {
                    Assert.assertEquals(0l, usageRecord.getTransferBytesIn().longValue());
                    Assert.assertEquals(0l, usageRecord.getTransferBytesOut().longValue());
                    Assert.assertEquals(100l, usageRecord.getLastBytesInCount().longValue());
                    Assert.assertEquals(1000l, usageRecord.getLastBytesOutCount().longValue());
                }

                if (usageRecord.getLoadBalancer().getId().equals(lb2.getId())) {
                    Assert.assertEquals(0l, usageRecord.getTransferBytesIn().longValue());
                    Assert.assertEquals(0l, usageRecord.getTransferBytesOut().longValue());
                    Assert.assertEquals(200l, usageRecord.getLastBytesInCount().longValue());
                    Assert.assertEquals(2000l, usageRecord.getLastBytesOutCount().longValue());
                }
            }
        }

        @Test
        public void shouldReturnOnlyUpdatedRecordsWhenRecordsCurrentlyExist() {
            bytesInMap.put(lb1.getId(), 100l);
            bytesOutMap.put(lb1.getId(), 1000l);
            bytesInMap.put(lb2.getId(), 200l);
            bytesOutMap.put(lb2.getId(), 2000l);

            Calendar now = Calendar.getInstance();

            List<UsageRecord> usageRecords = new ArrayList<UsageRecord>();
            usageRecords.add(UsageRecordHelper.createUsageRecord(lb1.getId(), 0l, 0l, 0l, 0l, now, now));
            usageRecords.add(UsageRecordHelper.createUsageRecord(lb2.getId(), 0l, 0l, 0l, 0l, now, now));

            when(usageRepository.getMostRecentUsageRecordsForLoadBalancers(anySetOf(Integer.class))).thenReturn(usageRecords);

            usageProcessor.execute(loadBalancers);
            Assert.assertTrue(usageProcessor.getRecordsToInsert().isEmpty());
            Assert.assertFalse(usageProcessor.getRecordsToUpdate().isEmpty());

            for (UsageRecord usageRecord : usageProcessor.getRecordsToUpdate()) {
                if (usageRecord.getLoadBalancer().getId().equals(lb1.getId())) {
                    Assert.assertEquals(100l, usageRecord.getTransferBytesIn().longValue());
                    Assert.assertEquals(1000l, usageRecord.getTransferBytesOut().longValue());
                    Assert.assertEquals(100l, usageRecord.getLastBytesInCount().longValue());
                    Assert.assertEquals(1000l, usageRecord.getLastBytesOutCount().longValue());
                }

                if (usageRecord.getLoadBalancer().getId().equals(lb2.getId())) {
                    Assert.assertEquals(200l, usageRecord.getTransferBytesIn().longValue());
                    Assert.assertEquals(2000l, usageRecord.getTransferBytesOut().longValue());
                    Assert.assertEquals(200l, usageRecord.getLastBytesInCount().longValue());
                    Assert.assertEquals(2000l, usageRecord.getLastBytesOutCount().longValue());
                }

                Assert.assertEquals(now, usageRecord.getStartTime());
            }
        }

        @Test
        public void shouldReturnNewAndUpdatedRecordsWhenSomeRecordExist() {
            bytesInMap.put(lb1.getId(), 100l);
            bytesOutMap.put(lb1.getId(), 1000l);
            bytesInMap.put(lb2.getId(), 200l);
            bytesOutMap.put(lb2.getId(), 2000l);

            Calendar now = Calendar.getInstance();

            List<UsageRecord> usageRecords = new ArrayList<UsageRecord>();
            usageRecords.add(UsageRecordHelper.createUsageRecord(lb1.getId(), 0l, 0l, 0l, 0l, now, now));

            when(usageRepository.getMostRecentUsageRecordsForLoadBalancers(anySetOf(Integer.class))).thenReturn(usageRecords);

            usageProcessor.execute(loadBalancers);
            Assert.assertFalse(usageProcessor.getRecordsToInsert().isEmpty());
            Assert.assertFalse(usageProcessor.getRecordsToUpdate().isEmpty());

            for (UsageRecord usageRecord : usageProcessor.getRecordsToUpdate()) {
                if (usageRecord.getLoadBalancer().getId().equals(lb1.getId())) {
                    Assert.assertEquals(100l, usageRecord.getTransferBytesIn().longValue());
                    Assert.assertEquals(1000l, usageRecord.getTransferBytesOut().longValue());
                    Assert.assertEquals(100l, usageRecord.getLastBytesInCount().longValue());
                    Assert.assertEquals(1000l, usageRecord.getLastBytesOutCount().longValue());
                    Assert.assertEquals(now, usageRecord.getStartTime());
                }
            }

            for (UsageRecord usageRecord : usageProcessor.getRecordsToInsert()) {
                if (usageRecord.getLoadBalancer().getId().equals(lb2.getId())) {
                    Assert.assertEquals(0l, usageRecord.getTransferBytesIn().longValue());
                    Assert.assertEquals(0l, usageRecord.getTransferBytesOut().longValue());
                    Assert.assertEquals(200l, usageRecord.getLastBytesInCount().longValue());
                    Assert.assertEquals(2000l, usageRecord.getLastBytesOutCount().longValue());
                }
            }
        }

        @Test
        public void shouldReturnOnlyNewRecordsWhenNewDayStarts() {
            bytesInMap.put(lb1.getId(), 200l);
            bytesOutMap.put(lb1.getId(), 0l);
            bytesInMap.put(lb2.getId(), 200l);
            bytesOutMap.put(lb2.getId(), 2000l);

            Calendar nowYesterday = Calendar.getInstance();
            nowYesterday.roll(Calendar.DAY_OF_YEAR, -1);

            List<UsageRecord> usageRecords = new ArrayList<UsageRecord>();
            usageRecords.add(UsageRecordHelper.createUsageRecord(lb1.getId(), 200l, 100l, 100l, 0l, nowYesterday, nowYesterday));
            usageRecords.add(UsageRecordHelper.createUsageRecord(lb2.getId(), 0l, 0l, 0l, 0l, nowYesterday, nowYesterday));

            when(usageRepository.getMostRecentUsageRecordsForLoadBalancers(anySetOf(Integer.class))).thenReturn(usageRecords);

            usageProcessor.execute(loadBalancers);
            Assert.assertFalse(usageProcessor.getRecordsToInsert().isEmpty());
            Assert.assertTrue(usageProcessor.getRecordsToUpdate().isEmpty());

            for (UsageRecord usageRecord : usageProcessor.getRecordsToInsert()) {
                if (usageRecord.getLoadBalancer().getId().equals(lb1.getId())) {
                    Assert.assertEquals(100l, usageRecord.getTransferBytesIn().longValue());
                    Assert.assertEquals(0l, usageRecord.getTransferBytesOut().longValue());
                    Assert.assertEquals(200l, usageRecord.getLastBytesInCount().longValue());
                    Assert.assertEquals(0l, usageRecord.getLastBytesOutCount().longValue());
                }

                if (usageRecord.getLoadBalancer().getId().equals(lb2.getId())) {
                    Assert.assertEquals(200l, usageRecord.getTransferBytesIn().longValue());
                    Assert.assertEquals(2000l, usageRecord.getTransferBytesOut().longValue());
                    Assert.assertEquals(200l, usageRecord.getLastBytesInCount().longValue());
                    Assert.assertEquals(2000l, usageRecord.getLastBytesOutCount().longValue());
                }
            }
        }
    }
}
