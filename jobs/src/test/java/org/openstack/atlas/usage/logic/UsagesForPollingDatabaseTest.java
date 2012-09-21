package org.openstack.atlas.usage.logic;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.openstack.atlas.usage.helpers.LoadBalancerNameMap;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class UsagesForPollingDatabaseTest {

    public static class WhenCreatingANewRecord {
        private UsagesForPollingDatabase usagesForDatabase;
        private Map<String, Long> bytesInMap;
        private Map<String, Long> bytesOutMap;
        private Map<String, Integer> currentConnectionsMap;
        private Map<String, Long> bytesInMapSsl;
        private Map<String, Long> bytesOutMapSsl;
        private Map<String, Integer> currentConnectionsMapSsl;
        private Map<Integer, LoadBalancerUsage> usagesAsMap;
        private Map<Integer, Usage> rollupUsagesAsMap;
        private String zxtmName;
        private String zxtmSslName;
        private Collection<LoadBalancerNameMap> loadBalancerNameMapCollection;
        private LoadBalancerNameMap loadBalancerNameMap;
        private LoadBalancerUsage currentUsage;
        private Usage latestRolledUpUsageRecord;
        private LoadBalancerRepository loadBalancerRepository;

        @Before
        public void standUp() throws EntityNotFoundException, DeletedStatusException {
            loadBalancerRepository = mock(LoadBalancerRepository.class);
            zxtmName = "1234_1234";
            zxtmSslName = ZxtmNameBuilder.genSslVSName(zxtmName);
            loadBalancerNameMapCollection = new ArrayList<LoadBalancerNameMap>();
            loadBalancerNameMap = new LoadBalancerNameMap();
            loadBalancerNameMap.setLoadBalancerId(1234);
            loadBalancerNameMap.setAccountId(1234);
            loadBalancerNameMap.setNonSslVirtualServerName(zxtmName);
            loadBalancerNameMap.setSslVirtualServerName(zxtmSslName);
            loadBalancerNameMapCollection.add(loadBalancerNameMap);
            bytesInMap = new HashMap<String, Long>();
            bytesInMapSsl = new HashMap<String, Long>();
            bytesOutMap = new HashMap<String, Long>();
            bytesOutMapSsl = new HashMap<String, Long>();
            currentConnectionsMap = new HashMap<String, Integer>();
            currentConnectionsMapSsl = new HashMap<String, Integer>();
            usagesAsMap = new HashMap<Integer, LoadBalancerUsage>();
            bytesInMap.put(zxtmName, 100l);
            bytesInMapSsl.put(ZxtmNameBuilder.genSslVSName(zxtmName), 100l);
            bytesOutMap.put(zxtmName, 200l);
            bytesOutMapSsl.put(ZxtmNameBuilder.genSslVSName(zxtmName), 200l);
            currentConnectionsMap.put(zxtmName, 50);
            currentConnectionsMapSsl.put(ZxtmNameBuilder.genSslVSName(zxtmName), 50);

            latestRolledUpUsageRecord = new Usage();
            latestRolledUpUsageRecord.setAccountId(1234);
            latestRolledUpUsageRecord.setLoadbalancer(new LoadBalancer());
            latestRolledUpUsageRecord.getLoadbalancer().setId(1234);
            latestRolledUpUsageRecord.setTags(5);

            rollupUsagesAsMap = new HashMap<Integer, Usage>();
            rollupUsagesAsMap.put(1234, latestRolledUpUsageRecord);
            when(loadBalancerRepository.getVipsByAccountIdLoadBalancerId(Matchers.<Integer>any(), Matchers.<Integer>any())).thenReturn(new HashSet<VirtualIp>());
            usagesForDatabase = new UsagesForPollingDatabase(loadBalancerRepository, loadBalancerNameMapCollection, bytesInMap, bytesOutMap, currentConnectionsMap, bytesInMapSsl, bytesOutMapSsl, currentConnectionsMapSsl, null, usagesAsMap, rollupUsagesAsMap);
        }

        @Test
        public void shouldCopyTagsFromLatestRolledUpUsageRecord() {
            usagesForDatabase.invoke();
            Assert.assertEquals(1, usagesForDatabase.getRecordsToInsert().size());
            final LoadBalancerUsage loadBalancerUsage = usagesForDatabase.getRecordsToInsert().get(0);
            Assert.assertEquals(latestRolledUpUsageRecord.getTags(), loadBalancerUsage.getTags());
        }

    }

    public static class WhenUpdatingCurrentRecord {
        private UsagesForPollingDatabase usagesForDatabase;
        private Map<String, Long> bytesInMap;
        private Map<String, Long> bytesOutMap;
        private Map<String, Integer> currentConnectionsMap;
        private Map<String, Long> bytesInMapSsl;
        private Map<String, Long> bytesOutMapSsl;
        private Map<String, Integer> currentConnectionsMapSsl;
        private Map<Integer, LoadBalancerUsage> usagesAsMap;
        private Map<Integer, Usage> rollupUsagesAsMap;
        private String zxtmName;
        private String zxtmSslName;
        private LoadBalancerNameMap loadBalancerNameMap;
        private LoadBalancerUsage currentUsage;

        @Before
        public void standUp() {
            zxtmName = "1234_1234";
            zxtmSslName = ZxtmNameBuilder.genSslVSName(zxtmName);
            loadBalancerNameMap = new LoadBalancerNameMap();
            loadBalancerNameMap.setLoadBalancerId(1234);
            loadBalancerNameMap.setAccountId(1234);
            loadBalancerNameMap.setNonSslVirtualServerName(zxtmName);
            loadBalancerNameMap.setSslVirtualServerName(zxtmSslName);
            bytesInMap = new HashMap<String, Long>();
            bytesInMapSsl = new HashMap<String, Long>();
            bytesOutMap = new HashMap<String, Long>();
            bytesOutMapSsl = new HashMap<String, Long>();
            currentConnectionsMap = new HashMap<String, Integer>();
            currentConnectionsMapSsl = new HashMap<String, Integer>();
            usagesAsMap = new HashMap<Integer, LoadBalancerUsage>();
            bytesInMap.put(zxtmName, 100l);
            bytesInMapSsl.put(ZxtmNameBuilder.genSslVSName(zxtmName), 100l);
            bytesOutMap.put(zxtmName, 200l);
            bytesOutMapSsl.put(ZxtmNameBuilder.genSslVSName(zxtmName), 200l);
            currentConnectionsMap.put(zxtmName, 50);
            currentConnectionsMapSsl.put(ZxtmNameBuilder.genSslVSName(zxtmName), 50);

            currentUsage = new LoadBalancerUsage();
            currentUsage.setAccountId(1234);
            currentUsage.setLoadbalancerId(1234);
            currentUsage.setAverageConcurrentConnections(125.0);
            currentUsage.setAverageConcurrentConnectionsSsl(125.0);
            currentUsage.setCumulativeBandwidthBytesIn(125l);
            currentUsage.setCumulativeBandwidthBytesInSsl(125l);
            currentUsage.setCumulativeBandwidthBytesOut(125l);
            currentUsage.setCumulativeBandwidthBytesOutSsl(125l);
            currentUsage.setLastBandwidthBytesIn(150l);
            currentUsage.setLastBandwidthBytesInSsl(150l);
            currentUsage.setLastBandwidthBytesOut(150l);
            currentUsage.setLastBandwidthBytesOutSsl(150l);
            currentUsage.setNumberOfPolls(5);
            currentUsage.setNumVips(1);

            usagesAsMap.put(1234, currentUsage);
            usagesForDatabase = new UsagesForPollingDatabase(null, null, bytesInMap, bytesOutMap, currentConnectionsMap, bytesInMapSsl, bytesOutMapSsl, currentConnectionsMapSsl, null, usagesAsMap, rollupUsagesAsMap);
        }

        @Test
        public void shouldRunInProperOrderWhenNumPollsIsZero() {
            LoadBalancerUsage mockedUsageRecord = mock(LoadBalancerUsage.class);
            mockedUsageRecord.setNumberOfPolls(0);
            usagesForDatabase.updateCurrentRecord(loadBalancerNameMap, mockedUsageRecord);
            InOrder inOrder = inOrder(mockedUsageRecord);

            inOrder.verify(mockedUsageRecord).setNumberOfPolls(0);
            inOrder.verify(mockedUsageRecord).setEndTime(any(Calendar.class));
            inOrder.verify(mockedUsageRecord).setNumberOfPolls(1);
            inOrder.verify(mockedUsageRecord).setAverageConcurrentConnections(50.0);
            inOrder.verify(mockedUsageRecord).setAverageConcurrentConnectionsSsl(50.0);
            inOrder.verify(mockedUsageRecord).setCumulativeBandwidthBytesIn(anyLong());
            inOrder.verify(mockedUsageRecord).setCumulativeBandwidthBytesInSsl(anyLong());
            inOrder.verify(mockedUsageRecord).setCumulativeBandwidthBytesOut(anyLong());
            inOrder.verify(mockedUsageRecord).setCumulativeBandwidthBytesOutSsl(anyLong());
            inOrder.verify(mockedUsageRecord).setLastBandwidthBytesIn(100l);
            inOrder.verify(mockedUsageRecord).setLastBandwidthBytesInSsl(100l);
            inOrder.verify(mockedUsageRecord).setLastBandwidthBytesOut(200l);
            inOrder.verify(mockedUsageRecord).setLastBandwidthBytesOutSsl(200l);
        }

        @Test
        public void shouldRunInProperOrderWhenNumPollsIsNotZero() {
            LoadBalancerUsage spy = spy(currentUsage);
            usagesForDatabase.updateCurrentRecord(loadBalancerNameMap, spy);
            InOrder inOrder = inOrder(spy);

            inOrder.verify(spy).setEndTime(any(Calendar.class));
            inOrder.verify(spy).setNumberOfPolls(6);
            inOrder.verify(spy).setAverageConcurrentConnections(112.5);
            inOrder.verify(spy).setAverageConcurrentConnectionsSsl(112.5);
            inOrder.verify(spy).setCumulativeBandwidthBytesIn(225l);
            inOrder.verify(spy).setCumulativeBandwidthBytesInSsl(225l);
            inOrder.verify(spy).setCumulativeBandwidthBytesOut(175l);
            inOrder.verify(spy).setCumulativeBandwidthBytesOutSsl(175l);
            inOrder.verify(spy).setLastBandwidthBytesIn(100l);
            inOrder.verify(spy).setLastBandwidthBytesInSsl(100l);
            inOrder.verify(spy).setLastBandwidthBytesOut(200l);
            inOrder.verify(spy).setLastBandwidthBytesOutSsl(200l);
        }

    }

}
