package org.openstack.atlas.service.domain.services;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.repository.HostUsageRefactorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.mockito.Mockito.mock;

@RunWith(Enclosed.class)
public class UsageRefactorServiceIntegrationTest {

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:context.xml"})
    @Transactional
    public static class WhenTestingProcessRecordsNoEvents {

        int host1;
        int accountId;
        int loadBalancer1;
        LoadBalancerHostUsage usage1;
        LoadBalancerHostUsage usage2;
        List<LoadBalancerHostUsage> usageList;
        LoadBalancerMergedHostUsage mergedUsage1;
        LoadBalancerMergedHostUsage mergedUsage2;
        List<LoadBalancerMergedHostUsage> mergedUsageList;

        @Autowired
        @Qualifier("usageRefactorService")
        public UsageRefactorService usageRefactorService;

        @Autowired
        public HostUsageRefactorRepository usageRefactorRepository;

        @Before
        public void standUp() {
            usageRefactorRepository = mock(HostUsageRefactorRepository.class);
            host1 = 1;
            loadBalancer1 = 121;
            accountId = 12321;

            usageList = new ArrayList<LoadBalancerHostUsage>();
            usage1 = new LoadBalancerHostUsage();
            usage1.setHostId(host1);
            usage1.setPollTime(Calendar.getInstance());
            usage1.setNumVips(2);
            usage1.setTagsBitmask(0);
            usage1.setAccountId(accountId);
            usage1.setConcurrentConnections(10);
            usage1.setConcurrentConnectionsSsl(0);
            usage1.setIncomingTransfer(100);
            usage1.setIncomingTransferSsl(0);
            usage1.setOutgoingTransfer(100);
            usage1.setOutgoingTransferSsl(0);
            usage1.setLoadbalancerId(loadBalancer1);
            usageList.add(usage1);
            usage2 = new LoadBalancerHostUsage();
            usage2.setHostId(host1);
            usage2.setPollTime(Calendar.getInstance());
            usage2.setNumVips(2);
            usage2.setTagsBitmask(0);
            usage2.setAccountId(accountId);
            usage2.setConcurrentConnections(10);
            usage2.setConcurrentConnectionsSsl(0);
            usage2.setIncomingTransfer(100);
            usage2.setIncomingTransferSsl(0);
            usage2.setOutgoingTransfer(100);
            usage2.setOutgoingTransferSsl(0);
            usage2.setLoadbalancerId(loadBalancer1);
            usageList.add(usage2);

            mergedUsageList = new ArrayList<LoadBalancerMergedHostUsage>();
            mergedUsage1 = new LoadBalancerMergedHostUsage();
            mergedUsage1.setPollTime(Calendar.getInstance());
            mergedUsage1.setNumVips(2);
            mergedUsage1.setTagsBitmask(0);
            mergedUsage1.setAccountId(accountId);
            mergedUsage1.setConcurrentConnections(10);
            mergedUsage1.setConcurrentConnectionsSsl(0);
            mergedUsage1.setIncomingTransfer(100);
            mergedUsage1.setIncomingTransferSsl(0);
            mergedUsage1.setOutgoingTransfer(100);
            mergedUsage1.setOutgoingTransferSsl(0);
            mergedUsage1.setLoadbalancerId(loadBalancer1);
            mergedUsageList.add(mergedUsage1);
            mergedUsage2 = new LoadBalancerMergedHostUsage();
            mergedUsage2.setPollTime(Calendar.getInstance());
            mergedUsage2.setNumVips(2);
            mergedUsage2.setTagsBitmask(0);
            mergedUsage2.setAccountId(accountId);
            mergedUsage2.setConcurrentConnections(10);
            mergedUsage2.setConcurrentConnectionsSsl(0);
            mergedUsage2.setIncomingTransfer(100);
            mergedUsage2.setIncomingTransferSsl(0);
            mergedUsage2.setOutgoingTransfer(100);
            mergedUsage2.setOutgoingTransferSsl(0);
            mergedUsage2.setLoadbalancerId(loadBalancer1);
            mergedUsageList.add(mergedUsage2);
        }

        @Ignore
        @Test
        public void shouldSuccessfullyAddOneRecordForAnEvent() {
            usageRefactorService.createUsageEvent(usage1);
        }

        @Ignore
        @Test
        public void shouldAddMultipleRecordsForHostUsages() {
            usageRefactorService.batchCreateLoadBalancerHostUsages(usageList);
        }

        @Ignore
        @Test
        public void shouldAddMultipleRecordsForMergedHostUsages() {
            usageRefactorService.batchCreateLoadBalancerMergedHostUsages(mergedUsageList);
        }

        @Ignore
        @Test
        public void shouldDeleteOldMergedHostUsages() {
            usageRefactorService.batchDeleteLoadBalancerMergedHostUsages(new ArrayList<LoadBalancerMergedHostUsage>());
        }

        @Ignore
        @Test
        public void shouldDeleteOldHostUsages() {
            Calendar now = Calendar.getInstance();
            now.add(Calendar.HOUR, -1);
            usageRefactorService.deleteOldLoadBalancerHostUsages(now);
        }
    }
}