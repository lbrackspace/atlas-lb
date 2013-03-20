package org.openstack.atlas.usagerefactor;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.usagerefactor.generator.PolledUsageRecordGenerator;
import org.openstack.atlas.usagerefactor.generator.GeneratorPojo;

import java.util.*;

import static org.mockito.Mockito.when;

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
//            List<PolledUsageRecord> allRecords = polledUsageRepository.getAllRecords(loadbalancerIds);
//            List<Usage> processedUsages = usageRollupProcessor.processRecords(allRecords);
//            Assert.assertEquals(1, processedUsages.size());
            List<GeneratorPojo> usagePojoList = new ArrayList<GeneratorPojo>();
            usagePojoList.add(new GeneratorPojo(5806065, 1, 1));
            polledRecords = PolledUsageRecordGenerator.generate(usagePojoList, Calendar.getInstance());
            List<Usage> processedUsages = usageRollupProcessor.processRecords(polledRecords);
            Assert.assertEquals(1, processedUsages.size());
        }

        @Test
        public void shouldCreateOneHourlyRecordPerLB(){
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            int randomLBCount = new Random().nextInt(100) + 1;
            for(int lbId = 0; lbId < randomLBCount; lbId++){
                generatorPojos.add(new GeneratorPojo(5806065, lbId, 1, 30));
            }
            polledRecords = PolledUsageRecordGenerator.generate(generatorPojos, Calendar.getInstance());
            List<Usage> processedUsages = usageRollupProcessor.processRecords(polledRecords);
            Set<Usage> processedUsagesSet = new HashSet<Usage>();
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
            List<GeneratorPojo> usagePojoList = new ArrayList<GeneratorPojo>();
            usagePojoList.add(new GeneratorPojo(5806065, 1, 1));
            polledUsageRecords = PolledUsageRecordGenerator.generate(usagePojoList, Calendar.getInstance());
            Map<Integer, List<PolledUsageRecord>> usagesByLbId = usageRollupProcessor.breakDownUsagesByLbId(polledUsageRecords);

            Assert.assertEquals(usagePojoList.size(), usagesByLbId.size());
            Assert.assertEquals(usagePojoList.get(0).getNumRecords(), usagesByLbId.get(1).size());
        }

        @Test
        public void shouldReturnManyRecordsWhenManyPolledRecordsExistForALoadBalancer(){
            List<GeneratorPojo> usagePojoList = new ArrayList<GeneratorPojo>();
            usagePojoList.add(new GeneratorPojo(5806065, 1, 1, 30));
            polledUsageRecords = PolledUsageRecordGenerator.generate(usagePojoList, Calendar.getInstance());
            Map<Integer, List<PolledUsageRecord>> usagesByLbId = usageRollupProcessor.breakDownUsagesByLbId(polledUsageRecords);

            Assert.assertEquals(usagePojoList.size(), usagesByLbId.size());
            Assert.assertEquals(usagePojoList.get(0).getNumRecords(), usagesByLbId.get(1).size());
        }

        @Test
        public void shouldReturnManyRecordsWhenManyPolledRecordsExistForManyLoadBalancers(){
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            int randomLBCount = new Random().nextInt(100) + 1;
            for(int lbId = 0; lbId < randomLBCount; lbId++){
                generatorPojos.add(new GeneratorPojo(5806065, lbId, 1, 30));
            }

            polledUsageRecords = PolledUsageRecordGenerator.generate(generatorPojos, Calendar.getInstance());
            Map<Integer, List<PolledUsageRecord>> usagesByLbId = usageRollupProcessor.breakDownUsagesByLbId(polledUsageRecords);

            Assert.assertEquals(generatorPojos.size(), usagesByLbId.size());
            for(int i = 0; i < randomLBCount; i++){
                Assert.assertEquals(generatorPojos.get(i).getNumRecords(), usagesByLbId.get(i).size());
            }
        }
    }
}
