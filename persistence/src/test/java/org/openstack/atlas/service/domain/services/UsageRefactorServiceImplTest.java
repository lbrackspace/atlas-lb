package org.openstack.atlas.service.domain.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.services.impl.UsageRefactorServiceImpl;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.repository.HostUsageRefactorRepository;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerMergedHostUsageRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class UsageRefactorServiceImplTest {
    public static class NodeOperations {
        HostUsageRefactorRepository hostUsageRefactorRepository;
        LoadBalancerMergedHostUsageRepository loadBalancerMergedHostUsageRepository;
        UsageRefactorService usageRefactorService;

        List<LoadBalancerHostUsage> hostUsageList;
        List<LoadBalancerMergedHostUsage> mergedUsageList;

        @Before
        public void standUp() {
            hostUsageRefactorRepository = mock(HostUsageRefactorRepository.class);
            loadBalancerMergedHostUsageRepository = mock(LoadBalancerMergedHostUsageRepository.class);
            usageRefactorService = new UsageRefactorServiceImpl();
        }

        @Before
        public void buildMockResponses() {
            hostUsageList = new ArrayList<LoadBalancerHostUsage>();
            mergedUsageList = new ArrayList<LoadBalancerMergedHostUsage>();
            when(hostUsageRefactorRepository.getAllLoadBalancerHostUsageRecords()).thenReturn(hostUsageList);
            when(loadBalancerMergedHostUsageRepository.getAllUsageRecordsInOrder()).thenReturn(mergedUsageList);
        }

        @Test
        public void placeHolder() {
            assertTrue(hostUsageList.size() == 0);
        }
    }
}