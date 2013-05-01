package org.openstack.atlas.service.domain.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.services.impl.UsageRefactorServiceImpl;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.repository.HostUsageRefactorRepository;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerMergedHostUsageRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class UsageRefactorServiceImplTest {

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenHandlingUsageRecords {
        List<LoadBalancerHostUsage> hostUsageList;
        List<LoadBalancerMergedHostUsage> mergedUsageList;
        int entryCount;

        @Mock
        HostUsageRefactorRepository hostUsageRefactorRepository;

        @Mock
        LoadBalancerMergedHostUsageRepository loadBalancerMergedHostUsageRepository;

        @InjectMocks
        UsageRefactorService usageRefactorService = new UsageRefactorServiceImpl();

        @Before
        public void standUp() {
            hostUsageList = new ArrayList<LoadBalancerHostUsage>();
            mergedUsageList = new ArrayList<LoadBalancerMergedHostUsage>();
            LoadBalancerHostUsage hostUsage1 = new LoadBalancerHostUsage();
            hostUsage1.setId(1);
            hostUsage1.setAccountId(1234321);
            hostUsage1.setConcurrentConnections(2);
            hostUsage1.setConcurrentConnectionsSsl(0);
            hostUsage1.setIncomingTransfer(50);
            hostUsage1.setOutgoingTransfer(25);
            hostUsage1.setIncomingTransferSsl(0);
            hostUsage1.setOutgoingTransferSsl(0);
            hostUsage1.setLoadbalancerId(123);
            hostUsage1.setNumVips(2);
            hostUsage1.setPollTime(Calendar.getInstance());
            hostUsage1.setTagsBitmask(0);
            hostUsage1.setHostId(10);
            hostUsageList.add(hostUsage1);
            LoadBalancerHostUsage hostUsage2 = new LoadBalancerHostUsage();
            hostUsage2.setId(1);
            hostUsage2.setAccountId(1234321);
            hostUsage2.setConcurrentConnections(2);
            hostUsage2.setConcurrentConnectionsSsl(0);
            hostUsage2.setIncomingTransfer(50);
            hostUsage2.setOutgoingTransfer(25);
            hostUsage2.setIncomingTransferSsl(0);
            hostUsage2.setOutgoingTransferSsl(0);
            hostUsage2.setLoadbalancerId(123);
            hostUsage2.setNumVips(2);
            hostUsage2.setPollTime(Calendar.getInstance());
            hostUsage2.setTagsBitmask(0);
            hostUsage2.setHostId(10);
            hostUsageList.add(hostUsage2);
            entryCount = 2;
            LoadBalancerMergedHostUsage usage = new LoadBalancerMergedHostUsage();
            usage.setId(1);
            usage.setAccountId(1234321);
            usage.setConcurrentConnections(2);
            usage.setConcurrentConnectionsSsl(0);
            usage.setIncomingTransfer(100);
            usage.setOutgoingTransfer(50);
            usage.setIncomingTransferSsl(0);
            usage.setOutgoingTransferSsl(0);
            usage.setLoadbalancerId(123);
            usage.setNumVips(2);
            usage.setPollTime(Calendar.getInstance());
            usage.setTagsBitmask(0);
            mergedUsageList.add(usage);
            when(hostUsageRefactorRepository.getAllLoadBalancerHostUsageRecords()).thenReturn(hostUsageList);
            when(loadBalancerMergedHostUsageRepository.getAllUsageRecordsInOrder()).thenReturn(mergedUsageList);
            when(hostUsageRefactorRepository.getMostRecentUsageRecordForLbId(anyInt())).thenReturn(hostUsage2);
            Mockito.doNothing().when(hostUsageRefactorRepository.getMostRecentUsageRecordForLbId(anyInt()));
        }

        @Test
        public void shouldCollectAllPreviousFiveMinutesUsageEntries() {
            Map<Integer, List<LoadBalancerHostUsage>> map = usageRefactorService.getAllLoadBalancerHostUsages();
            List<LoadBalancerHostUsage> list = map.get(123);
            assertTrue(list.size() == entryCount);
        }

        @Test
        public void shouldCombineAllDataIntoEventSeparatedEntries() {
            assertTrue(usageRefactorService.getRecentHostUsageRecord(123).getPollTime() != null);
        }

        @Test
        public void shouldAddRecordForLoadBalancerWithEvent() {
        }
    }
}