package org.openstack.atlas.service.domain.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.services.impl.UsageRefactorServiceImpl;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
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
        List<LoadBalancerHostUsage> firstUsageList;
        List<LoadBalancerHostUsage> secondUsageList;
        List<LoadBalancerHostUsage> thirdUsageList;
        List<LoadBalancerHostUsage> fourthUsageList;
        List<LoadBalancerHostUsage> fifthUsageList;
        int firstLoadBalancerId;
        int secondLoadBalancerId;
        int firstHostId;
        int secondHostId;
        Calendar recentPollTime;

        @Mock
        HostUsageRefactorRepository hostUsageRefactorRepository;

        @Mock
        LoadBalancerMergedHostUsageRepository loadBalancerMergedHostUsageRepository;

        @InjectMocks
        UsageRefactorService usageRefactorService = new UsageRefactorServiceImpl();

        @Before
        public void standUp() {
            firstHostId = 10;
            secondHostId = 11;
            firstLoadBalancerId = 123;
            secondLoadBalancerId = 321;
            firstUsageList = new ArrayList<LoadBalancerHostUsage>();
            secondUsageList = new ArrayList<LoadBalancerHostUsage>();
            thirdUsageList = new ArrayList<LoadBalancerHostUsage>();
            fourthUsageList = new ArrayList<LoadBalancerHostUsage>();
            fifthUsageList = new ArrayList<LoadBalancerHostUsage>();

            LoadBalancerHostUsage hostUsage1 = new LoadBalancerHostUsage();
            hostUsage1.setAccountId(1234321);
            hostUsage1.setConcurrentConnections(2);
            hostUsage1.setConcurrentConnectionsSsl(0);
            hostUsage1.setIncomingTransfer(50);
            hostUsage1.setOutgoingTransfer(25);
            hostUsage1.setIncomingTransferSsl(0);
            hostUsage1.setOutgoingTransferSsl(0);
            hostUsage1.setLoadbalancerId(firstLoadBalancerId);
            hostUsage1.setNumVips(2);
            hostUsage1.setPollTime(Calendar.getInstance());
            hostUsage1.setTagsBitmask(0);
            hostUsage1.setHostId(firstHostId);
            firstUsageList.add(hostUsage1);
            secondUsageList.add(hostUsage1);
            fourthUsageList.add(hostUsage1);
            fifthUsageList.add(hostUsage1);

            LoadBalancerHostUsage hostUsage2 = new LoadBalancerHostUsage();
            hostUsage2.setAccountId(1234321);
            hostUsage2.setConcurrentConnections(2);
            hostUsage2.setConcurrentConnectionsSsl(0);
            hostUsage2.setIncomingTransfer(50);
            hostUsage2.setOutgoingTransfer(25);
            hostUsage2.setIncomingTransferSsl(0);
            hostUsage2.setOutgoingTransferSsl(0);
            hostUsage2.setLoadbalancerId(firstLoadBalancerId);
            hostUsage2.setNumVips(2);
            hostUsage2.setPollTime(Calendar.getInstance());
            hostUsage2.setTagsBitmask(0);
            hostUsage2.setHostId(secondHostId);
            secondUsageList.add(hostUsage2);
            thirdUsageList.add(hostUsage2);
            fifthUsageList.add(hostUsage2);

            LoadBalancerHostUsage hostUsage3 = new LoadBalancerHostUsage();
            hostUsage3.setAccountId(1234321);
            hostUsage3.setConcurrentConnections(2);
            hostUsage3.setConcurrentConnectionsSsl(0);
            hostUsage3.setIncomingTransfer(50);
            hostUsage3.setOutgoingTransfer(25);
            hostUsage3.setIncomingTransferSsl(0);
            hostUsage3.setOutgoingTransferSsl(0);
            hostUsage3.setLoadbalancerId(secondLoadBalancerId);
            hostUsage3.setNumVips(2);
            hostUsage3.setPollTime(Calendar.getInstance());
            hostUsage3.setTagsBitmask(0);
            hostUsage3.setHostId(firstHostId);
            fourthUsageList.add(hostUsage3);
            fifthUsageList.add(hostUsage3);

            LoadBalancerHostUsage hostUsage4 = new LoadBalancerHostUsage();
            hostUsage4.setAccountId(1234321);
            hostUsage4.setConcurrentConnections(2);
            hostUsage4.setConcurrentConnectionsSsl(0);
            hostUsage4.setIncomingTransfer(50);
            hostUsage4.setOutgoingTransfer(25);
            hostUsage4.setIncomingTransferSsl(0);
            hostUsage4.setOutgoingTransferSsl(0);
            hostUsage4.setLoadbalancerId(secondLoadBalancerId);
            hostUsage4.setNumVips(2);
            recentPollTime = Calendar.getInstance();
            hostUsage4.setPollTime(recentPollTime);
            hostUsage4.setTagsBitmask(0);
            hostUsage4.setHostId(secondHostId);
            thirdUsageList.add(hostUsage4);
            fourthUsageList.add(hostUsage4);
            fifthUsageList.add(hostUsage4);

            when(hostUsageRefactorRepository.getMostRecentUsageRecordForLbIdAndHostId(anyInt(), anyInt())).thenReturn(hostUsage4);
        }

        @Test
        public void shouldCollectOneLoadBalancerOnOneHost() {
            when(hostUsageRefactorRepository.getAllLoadBalancerHostUsageRecords(true)).thenReturn(firstUsageList);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> map = usageRefactorService.getAllLoadBalancerHostUsages();
            Map<Integer, List<LoadBalancerHostUsage>> list = map.get(firstLoadBalancerId);
            assertTrue(list.get(firstHostId).size() == 1);
        }

        @Test
        public void shouldCollectForOneLoadBalancerOnMultipleHosts() {
            when(hostUsageRefactorRepository.getAllLoadBalancerHostUsageRecords(true)).thenReturn(secondUsageList);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> map = usageRefactorService.getAllLoadBalancerHostUsages();
            Map<Integer, List<LoadBalancerHostUsage>> list = map.get(firstLoadBalancerId);
            assertTrue(list.get(firstHostId).size() == 1);
            assertTrue(list.get(secondHostId).size() == 1);
        }

        @Test
        public void shouldCollectMultiplLoadBalancersOnOneHost() {
            when(hostUsageRefactorRepository.getAllLoadBalancerHostUsageRecords(true)).thenReturn(thirdUsageList);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> map = usageRefactorService.getAllLoadBalancerHostUsages();
            Map<Integer, List<LoadBalancerHostUsage>> firstList = map.get(firstLoadBalancerId);
            Map<Integer, List<LoadBalancerHostUsage>> secondList = map.get(secondLoadBalancerId);
            assertTrue(firstList.get(secondHostId).size() == 1);
            assertTrue(secondList.get(secondHostId).size() == 1);
        }

        @Test
        public void shouldCollectOneLoadBalancerOnOneHostAndOneLoadBalancerOnMultipleHosts() {
            when(hostUsageRefactorRepository.getAllLoadBalancerHostUsageRecords(true)).thenReturn(fourthUsageList);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> map = usageRefactorService.getAllLoadBalancerHostUsages();
            Map<Integer, List<LoadBalancerHostUsage>> firstList = map.get(firstLoadBalancerId);
            Map<Integer, List<LoadBalancerHostUsage>> secondList = map.get(secondLoadBalancerId);
            assertTrue(!firstList.get(firstHostId).isEmpty());
            assertTrue(!secondList.get(firstHostId).isEmpty());
            assertTrue(!secondList.get(secondHostId).isEmpty());
        }

        @Test
        public void shouldCollectMultipleLoadBalancersOnMultipleHosts() {
            when(hostUsageRefactorRepository.getAllLoadBalancerHostUsageRecords(true)).thenReturn(fifthUsageList);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> map = usageRefactorService.getAllLoadBalancerHostUsages();
            Map<Integer, List<LoadBalancerHostUsage>> firstList = map.get(firstLoadBalancerId);
            Map<Integer, List<LoadBalancerHostUsage>> secondList = map.get(secondLoadBalancerId);
            assertTrue(!firstList.get(firstHostId).isEmpty());
            assertTrue(!firstList.get(secondHostId).isEmpty());
            assertTrue(!secondList.get(firstHostId).isEmpty());
            assertTrue(!secondList.get(secondHostId).isEmpty());
        }

        @Test
        public void shouldRetrieveOneEntryForLoadBalancerId() {
            LoadBalancerHostUsage recentEvent = usageRefactorService.getLastRecordForLbIdAndHostId(secondLoadBalancerId, secondHostId);
            assertTrue(recentEvent.getPollTime().equals(recentPollTime));
        }
    }
}