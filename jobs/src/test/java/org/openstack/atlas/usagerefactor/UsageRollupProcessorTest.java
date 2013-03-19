package org.openstack.atlas.usagerefactor;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.usagerefactor.generator.PolledUsageRecordGenerator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import org.openstack.atlas.usagerefactor.generator.PolledUsageRecordGenerator;

//@Ignore
@RunWith(Enclosed.class)
public class UsageRollupProcessorTest {

    @RunWith(MockitoJUnitRunner.class)
    public static class OneHourOfPolledUsageWithNoEvents {
        private int lbId1 = 1234;
        private List<Integer> loadbalancerIds;

        @Mock
        private PolledUsageRepository polledUsageRepository;
        private List<PolledUsageRecord> polledRecords;
        private UsageRollupProcessor usageRollupProcessor;

        @Before
        public void standUp() {
            loadbalancerIds = new ArrayList<Integer>();
            loadbalancerIds.add(lbId1);
            usageRollupProcessor = new UsageRollupProcessorImpl();

            polledRecords = new ArrayList<PolledUsageRecord>();
            when(polledUsageRepository.getAllRecords(loadbalancerIds)).thenReturn(polledRecords);
        }

        @Test
        public void shouldNotCreateARecord() {
            List<PolledUsageRecord> allRecords = polledUsageRepository.getAllRecords(loadbalancerIds);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(allRecords);
            Assert.assertTrue(processedUsages.isEmpty());
        }

        @Test
        public void shouldCreateOneHourlyRecord() {
            List<PolledUsageRecord> allRecords = polledUsageRepository.getAllRecords(loadbalancerIds);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(allRecords);
            Assert.assertEquals(1, processedUsages.size());
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenBreakingPolledRecordsDownByLbId {
        private UsageRollupProcessor usageRollupProcessor;
        private List<PolledUsageRecord> polledUsageRecords;

        @Before
        public void standUp() {
            usageRollupProcessor = new UsageRollupProcessorImpl();
            polledUsageRecords = new ArrayList<PolledUsageRecord>();
        }

        @Test
        public void shouldReturnEmptyMapWhenNoPolledRecords() {
            Map<Integer, List<PolledUsageRecord>> usagesByLbId = usageRollupProcessor.breakDownUsagesByLbId(polledUsageRecords);

            Assert.assertTrue(usagesByLbId.isEmpty());
        }

        @Test
        public void shouldReturnARecordWhenOnePolledRecordExists() {
            List<PolledUsageRecordGenerator.GeneratorPojo> usagePojoList = new ArrayList<PolledUsageRecordGenerator.GeneratorPojo>();
            PolledUsageRecordGenerator.GeneratorPojo usagePojo = new PolledUsageRecordGenerator.GeneratorPojo(5806065, 1, 1);
            usagePojoList.add(usagePojo);
            polledUsageRecords = PolledUsageRecordGenerator.generate(usagePojoList, Calendar.getInstance());
            Map<Integer, List<PolledUsageRecord>> usagesByLbId = usageRollupProcessor.breakDownUsagesByLbId(polledUsageRecords);
            Assert.assertEquals(1, usagesByLbId.size());
        }
    }
}
