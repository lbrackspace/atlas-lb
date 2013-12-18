package org.openstack.atlas.service.domain.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.repository.HostUsageRefactorRepository;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerMergedHostUsageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(Enclosed.class)
public class UsageRefactorServiceIntegrationTest {

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:context.xml"})
    @Transactional
    public static class WhenTestingProcessRecordsNoEvents {

        int hostId;
        int accountId;
        int loadBalancerId;
        int itemId1;
        int itemId2;
        Calendar firstTime;
        Calendar secondTime;
        LoadBalancerHostUsage usage1;
        LoadBalancerHostUsage usage2;
        List<LoadBalancerHostUsage> usageList;
        LoadBalancerMergedHostUsage mergedUsage1;
        LoadBalancerMergedHostUsage mergedUsage2;
        List<LoadBalancerMergedHostUsage> mergedUsageList;

        @Autowired
        public UsageRefactorService usageRefactorService;

        @Autowired
        public HostUsageRefactorRepository usageRefactorRepository;

        @Autowired
        public LoadBalancerMergedHostUsageRepository mergedUsageRepository;

        @Before
        public void standUp() {
            usageRefactorRepository = mock(HostUsageRefactorRepository.class);
            itemId1 = 1;
            itemId2 = 2;
            hostId = 1;
            loadBalancerId = 121;
            accountId = 12321;
            firstTime = Calendar.getInstance();
            firstTime.add(Calendar.HOUR, -1);
            firstTime.add(Calendar.MINUTE, 10);
            secondTime = Calendar.getInstance();
            secondTime.add(Calendar.HOUR, -1);
            secondTime.add(Calendar.MINUTE, 15);

            usageList = new ArrayList<LoadBalancerHostUsage>();
            usage1 = new LoadBalancerHostUsage();
            usage1.setId(1L);
            usage1.setHostId(hostId);
            usage1.setPollTime(firstTime);
            usage1.setNumVips(2);
            usage1.setTagsBitmask(0);
            usage1.setAccountId(accountId);
            usage1.setConcurrentConnections(10);
            usage1.setConcurrentConnectionsSsl(0);
            usage1.setIncomingTransfer(100);
            usage1.setIncomingTransferSsl(0);
            usage1.setOutgoingTransfer(100);
            usage1.setOutgoingTransferSsl(0);
            usage1.setLoadbalancerId(loadBalancerId);
            usageList.add(usage1);
            usage2 = new LoadBalancerHostUsage();
            usage2.setId(2L);
            usage2.setHostId(hostId);
            usage2.setPollTime(secondTime);
            usage2.setNumVips(2);
            usage2.setTagsBitmask(0);
            usage2.setAccountId(accountId);
            usage2.setConcurrentConnections(10);
            usage2.setConcurrentConnectionsSsl(0);
            usage2.setIncomingTransfer(100);
            usage2.setIncomingTransferSsl(0);
            usage2.setOutgoingTransfer(100);
            usage2.setOutgoingTransferSsl(0);
            usage2.setLoadbalancerId(loadBalancerId);
            usageList.add(usage2);

            mergedUsageList = new ArrayList<LoadBalancerMergedHostUsage>();
            mergedUsage1 = new LoadBalancerMergedHostUsage();
            mergedUsage1.setPollTime(firstTime);
            mergedUsage1.setNumVips(2);
            mergedUsage1.setTagsBitmask(0);
            mergedUsage1.setAccountId(accountId);
            mergedUsage1.setConcurrentConnections(10);
            mergedUsage1.setConcurrentConnectionsSsl(0);
            mergedUsage1.setIncomingTransfer(100);
            mergedUsage1.setIncomingTransferSsl(0);
            mergedUsage1.setOutgoingTransfer(100);
            mergedUsage1.setOutgoingTransferSsl(0);
            mergedUsage1.setLoadbalancerId(loadBalancerId);
            mergedUsageList.add(mergedUsage1);
            mergedUsage2 = new LoadBalancerMergedHostUsage();
            mergedUsage2.setPollTime(secondTime);
            mergedUsage2.setNumVips(2);
            mergedUsage2.setTagsBitmask(0);
            mergedUsage2.setAccountId(accountId);
            mergedUsage2.setConcurrentConnections(10);
            mergedUsage2.setConcurrentConnectionsSsl(0);
            mergedUsage2.setIncomingTransfer(100);
            mergedUsage2.setIncomingTransferSsl(0);
            mergedUsage2.setOutgoingTransfer(100);
            mergedUsage2.setOutgoingTransferSsl(0);
            mergedUsage2.setLoadbalancerId(loadBalancerId);
            mergedUsageList.add(mergedUsage2);
        }

        @Test
        public void shouldSuccessfullyAddOneRecordForAnEvent() {
            usage1.setId(null);
            usageRefactorService.createUsageEvent(usage1);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> map = usageRefactorService.getAllLoadBalancerHostUsages();
            assertTrue(map.size() == 1);
        }

        @Test
        public void shouldAddMultipleRecordsForHostUsages() {
            usageRefactorService.batchCreateLoadBalancerHostUsages(usageList);
            Map<Integer,Map<Integer, List<LoadBalancerHostUsage>>> map = usageRefactorService.getAllLoadBalancerHostUsages();
            assertTrue(map.get(loadBalancerId).get(hostId).size() == 2);
        }

        @Test
        public void shouldAddMultipleRecordsForMergedHostUsages() {
            usageRefactorService.batchCreateLoadBalancerMergedHostUsages(mergedUsageList);
            List<LoadBalancerMergedHostUsage> map = mergedUsageRepository.getAllUsageRecordsInOrder();
            assertTrue(map.size() == 2);
        }

        @Test
        public void shouldDeleteOldMergedHostUsages() {
            usageRefactorService.batchCreateLoadBalancerMergedHostUsages(mergedUsageList);
            List<LoadBalancerMergedHostUsage> addMap = mergedUsageRepository.getAllUsageRecordsInOrder();
            assertTrue(addMap.size() == 2);
            usageRefactorService.batchDeleteLoadBalancerMergedHostUsages(addMap);
            List<LoadBalancerMergedHostUsage> deleteMap = mergedUsageRepository.getAllUsageRecordsInOrder();
            assertTrue(deleteMap.size() == 0);
        }

        @Test
        public void shouldDeleteOldHostUsagesLessThanMaxId() {
            usageRefactorService.batchCreateLoadBalancerHostUsages(usageList);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> addMap = usageRefactorService.getAllLoadBalancerHostUsages();
            assertTrue(addMap.get(loadBalancerId).get(hostId).size() == 2);
            usageRefactorService.deleteOldLoadBalancerHostUsages(Calendar.getInstance(), null, 1L);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> deleteMap = usageRefactorService.getAllLoadBalancerHostUsages();
            assertTrue(deleteMap.size() == 1);
        }

        @Test
        public void shouldDeleteOldHostUsagesButNotLoadBalancerIdsSpecified() {
            ArrayList<Integer> loadbalancerIdsToKeep = new ArrayList<Integer>();
            loadbalancerIdsToKeep.add(121);
            usageRefactorService.batchCreateLoadBalancerHostUsages(usageList);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> addMap = usageRefactorService.getAllLoadBalancerHostUsages();
            assertTrue(addMap.get(loadBalancerId).get(hostId).size() == 2);
            usageRefactorService.deleteOldLoadBalancerHostUsages(Calendar.getInstance(), loadbalancerIdsToKeep, 2L);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> deleteMap = usageRefactorService.getAllLoadBalancerHostUsages();
            assertTrue(deleteMap.get(loadBalancerId).get(hostId).size() == 2);
        }
    }
}