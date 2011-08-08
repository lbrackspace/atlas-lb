package org.openstack.atlas.usage.logic;

import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InOrder;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;

@RunWith(Enclosed.class)
public class UsagesForPollingDatabaseTest {
    public static class WhenUpdatingCurrentRecord {
        private UsagesForPollingDatabase usagesForDatabase;
        private Map<String, Long> bytesInMap;
        private Map<String, Long> bytesOutMap;
        private Map<String, Integer> currentConnectionsMap;
        private Map<Integer, LoadBalancerUsage> usagesAsMap;
        private String name;
        private LoadBalancerUsage currentUsage;

        @Before
        public void standUp() {
            name = "1234_1234";
            bytesInMap = new HashMap<String, Long>();
            bytesOutMap = new HashMap<String, Long>();
            currentConnectionsMap = new HashMap<String, Integer>();
            usagesAsMap = new HashMap<Integer, LoadBalancerUsage>();
            bytesInMap.put(name, 100l);
            bytesOutMap.put(name, 200l);
            currentConnectionsMap.put(name, 50);

            currentUsage = new LoadBalancerUsage();
            currentUsage.setAccountId(1234);
            currentUsage.setLoadbalancerId(1234);
            currentUsage.setAverageConcurrentConnections(125.0);
            currentUsage.setCumulativeBandwidthBytesIn(125l);
            currentUsage.setCumulativeBandwidthBytesOut(125l);
            currentUsage.setLastBandwidthBytesIn(150l);
            currentUsage.setLastBandwidthBytesOut(150l);
            currentUsage.setNumberOfPolls(5);
            currentUsage.setNumVips(1);

            usagesAsMap.put(1234, currentUsage);
            usagesForDatabase = new UsagesForPollingDatabase(null, bytesInMap, bytesOutMap, currentConnectionsMap, null, usagesAsMap);
        }

        @Test
        public void shouldRunInProperOrderWhenNumPollsIsZero() {
            LoadBalancerUsage mockedUsageRecord = mock(LoadBalancerUsage.class);
            mockedUsageRecord.setNumberOfPolls(0);
            usagesForDatabase.updateCurrentRecord(name, mockedUsageRecord);
            InOrder inOrder = inOrder(mockedUsageRecord);

            inOrder.verify(mockedUsageRecord).setNumberOfPolls(0);
            inOrder.verify(mockedUsageRecord).setEndTime(any(Calendar.class));
            inOrder.verify(mockedUsageRecord).setNumberOfPolls(1);
            inOrder.verify(mockedUsageRecord).setAverageConcurrentConnections(50.0);
            inOrder.verify(mockedUsageRecord).setCumulativeBandwidthBytesIn(0l);
            inOrder.verify(mockedUsageRecord).setCumulativeBandwidthBytesOut(0l);
            inOrder.verify(mockedUsageRecord).setLastBandwidthBytesIn(100l);
            inOrder.verify(mockedUsageRecord).setLastBandwidthBytesOut(200l);
        }

        @Test
        public void shouldRunInProperOrderWhenNumPollsIsNotZero() {
            LoadBalancerUsage spy = spy(currentUsage);
            usagesForDatabase.updateCurrentRecord(name, spy);
            InOrder inOrder = inOrder(spy);

            inOrder.verify(spy).setEndTime(any(Calendar.class));
            inOrder.verify(spy).setNumberOfPolls(6);
            inOrder.verify(spy).setAverageConcurrentConnections(112.5);
            inOrder.verify(spy).setCumulativeBandwidthBytesIn(225l);
            inOrder.verify(spy).setCumulativeBandwidthBytesOut(175l);
            inOrder.verify(spy).setLastBandwidthBytesIn(100l);
            inOrder.verify(spy).setLastBandwidthBytesOut(200l);
        }

    }

    public static class WhenCalculatingBandwidth {
        private UsagesForPollingDatabase usagesForDatabase;
        private Map<String, Long> bytesInMap;
        private Map<String, Long> bytesOutMap;
        private Map<String, Integer> currentConnectionsMap;
        private Map<Integer, LoadBalancerUsage> usagesAsMap;
        private String name;

        @Before
        public void standUp() {
            name = "1234_1234";
            bytesInMap = new HashMap<String, Long>();
            bytesOutMap = new HashMap<String, Long>();
            currentConnectionsMap = new HashMap<String, Integer>();
            usagesAsMap = new HashMap<Integer, LoadBalancerUsage>();
            bytesInMap.put(name, 100l);
            bytesOutMap.put(name, 200l);
            currentConnectionsMap.put(name, 50);
            
            usagesForDatabase = new UsagesForPollingDatabase(null, bytesInMap, bytesOutMap, currentConnectionsMap, null, usagesAsMap);
        }
        
        @Test
        public void shouldCalculateCumBandwidthBytesInWhenUsageIsZero() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesIn(0l);
            usageRecord.setLastBandwidthBytesIn(0l);

            Long actualValue = usagesForDatabase.calculateCumBandwidthBytesIn(usageRecord, 0l);
            Assert.assertEquals(new Long(0), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesInWhenUsageIsIncreasingFromZero() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesIn(0l);
            usageRecord.setLastBandwidthBytesIn(0l);

            Long actualValue = usagesForDatabase.calculateCumBandwidthBytesIn(usageRecord, 1024l);
            Assert.assertEquals(new Long(1024), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesInWhenUsageIsIncreasingFromNonZero() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesIn(1024l);
            usageRecord.setLastBandwidthBytesIn(1024l);

            Long actualValue = usagesForDatabase.calculateCumBandwidthBytesIn(usageRecord, 2048l);
            Assert.assertEquals(new Long(2048), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesInWhenLBDeviceResetsMemory() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesIn(2048l);
            usageRecord.setLastBandwidthBytesIn(1024l);

            Long actualValue = usagesForDatabase.calculateCumBandwidthBytesIn(usageRecord, 512l);
            Assert.assertEquals(new Long(2560), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesInWhenNoUsageChange() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesIn(2560l);
            usageRecord.setLastBandwidthBytesIn(512l);

            Long actualValue = usagesForDatabase.calculateCumBandwidthBytesIn(usageRecord, 512l);
            Assert.assertEquals(new Long(2560), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesInWhenUsageRecordWithOldLBDeviceValue() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesIn(0l);
            usageRecord.setLastBandwidthBytesIn(1024l);

            Long actualValue = usagesForDatabase.calculateCumBandwidthBytesIn(usageRecord, 2048l);
            Assert.assertEquals(new Long(1024), actualValue);
        }

        @Test
        public void shouldCalculateCumBandwidthBytesInWhenUsageRecordWithOldLBDeviceValueAndLBDeviceResets() {
            LoadBalancerUsage usageRecord = new LoadBalancerUsage();
            usageRecord.setCumulativeBandwidthBytesIn(0l);
            usageRecord.setLastBandwidthBytesIn(1024l);

            Long actualValue = usagesForDatabase.calculateCumBandwidthBytesIn(usageRecord, 512l);
            Assert.assertEquals(new Long(512), actualValue);
        }
    }
}
