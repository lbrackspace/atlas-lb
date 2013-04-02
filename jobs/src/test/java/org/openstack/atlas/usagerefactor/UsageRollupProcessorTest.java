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
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.usagerefactor.generator.PolledUsageRecordGenerator;
import org.openstack.atlas.usagerefactor.generator.GeneratorPojo;

import java.util.*;

import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class UsageRollupProcessorTest {

    @RunWith(MockitoJUnitRunner.class)
    public static class OneHourOfPolledUsageWithNoEvents {
        private int accountId = 5806065;
        private int lbId = 1234;

        private List<Integer> loadbalancerIds;
        @Mock
        private PolledUsageRepository polledUsageRepository;
        private List<PolledUsageRecord> polledRecords;
        private UsageRollupProcessor usageRollupProcessor;
        private Calendar initialPollTime;
        private Calendar hourToProcess;

        @Before
        public void standUp() {
            loadbalancerIds = new ArrayList<Integer>();
            loadbalancerIds.add(lbId);
            usageRollupProcessor = new UsageRollupProcessorImpl();
            initialPollTime = new GregorianCalendar(2013, Calendar.MARCH, 20, 10, 0, 0);
            hourToProcess = new GregorianCalendar(2013, Calendar.MARCH, 20, 10, 0, 0);

            List<GeneratorPojo> generatorPojoList = new ArrayList<GeneratorPojo>();
            generatorPojoList.add(new GeneratorPojo(accountId, lbId, 24));
            polledRecords = PolledUsageRecordGenerator.generate(generatorPojoList, initialPollTime);
            when(polledUsageRepository.getAllRecords(loadbalancerIds)).thenReturn(polledRecords);
        }

        @Test
        public void shouldNotCreateARecord() {
            polledRecords.clear();
            List<PolledUsageRecord> allRecords = polledUsageRepository.getAllRecords(loadbalancerIds);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(allRecords, hourToProcess);
            Assert.assertTrue(processedUsages.isEmpty());
        }

        @Test
        public void shouldCreateOneHourlyRecord() {
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 11));
            polledRecords = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(polledRecords, hourToProcess);
            Assert.assertEquals(1, processedUsages.size());
        }

        @Test
        public void shouldCreateOneHourlyRecordPerLB(){
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            int randomLBCount = new Random().nextInt(100) + 1;
            for(int lbId = 0; lbId < randomLBCount; lbId++){
                generatorPojos.add(new GeneratorPojo(5806065, lbId, 1, 30));
            }
            polledRecords = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(polledRecords, hourToProcess);
            Assert.assertEquals(randomLBCount, processedUsages.size());
            for(int lbId = 0; lbId < randomLBCount; lbId++){
                List<Usage> lbUsageList = new ArrayList<Usage>();
                for(Usage processedUsage : processedUsages){
                    if (processedUsage.getLoadbalancer().getId() == lbId){
                        lbUsageList.add(processedUsage);
                    }
                }
                Assert.assertEquals(1, lbUsageList.size());
            }
        }

        @Test
        public void shouldCreateOneRecordWithStartTimeOnTheHourAndEndTimeOnTheNextHour(){
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 11));
            polledRecords = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(polledRecords, hourToProcess);
            Calendar compTime = Calendar.getInstance();
            compTime.setTime(hourToProcess.getTime());
            Assert.assertEquals(1, processedUsages.size());
            Assert.assertEquals(compTime, processedUsages.get(0).getStartTime());
            compTime.add(Calendar.HOUR,  1);
            Assert.assertEquals(compTime, processedUsages.get(0).getEndTime());
        }

        @Test
        public void shouldSumAllBandwidthIntoOneRecord(){
            int numLBPolls = 11;
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, numLBPolls));
            polledRecords = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime);
            long increment = 20530932;
            long outgoingTransfer = 123021;
            long incomingTransfer = 1001421;
            long outgoingTransferSsl = 23242;
            long incomingTransferSsl = 928340;
            long totBandwidthOut = 0;
            long totBandwidthIn = 0;
            long totBandwidthOutSsl = 0;
            long totBandwidthInSsl = 0;
            for(PolledUsageRecord polledRecord : polledRecords){
                polledRecord.setOutgoingTransfer(outgoingTransfer);
                polledRecord.setIncomingTransfer(incomingTransfer);
                polledRecord.setOutgoingTransferSsl(outgoingTransferSsl);
                polledRecord.setIncomingTransferSsl(incomingTransferSsl);
                totBandwidthOut += outgoingTransfer;
                totBandwidthIn += incomingTransfer;
                totBandwidthOutSsl += outgoingTransferSsl;
                totBandwidthInSsl += incomingTransferSsl;
                outgoingTransfer += increment;
                incomingTransfer += increment;
                outgoingTransferSsl += increment;
                incomingTransferSsl += increment;
            }
            List<Usage> processedUsages = usageRollupProcessor.processRecords(polledRecords, hourToProcess);
            Assert.assertEquals(1, processedUsages.size());
            Assert.assertEquals(totBandwidthOut, processedUsages.get(0).getOutgoingTransfer().longValue());
            Assert.assertEquals(totBandwidthIn, processedUsages.get(0).getIncomingTransfer().longValue());
            Assert.assertEquals(totBandwidthOutSsl, processedUsages.get(0).getOutgoingTransferSsl().longValue());
            Assert.assertEquals(totBandwidthInSsl, processedUsages.get(0).getIncomingTransferSsl().longValue());
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class OneHourOfPolledUsageWithEvents {

        private int accountId = 5806065;
        private int lbId = 1234;

        private List<Integer> loadbalancerIds;
        @Mock
        private PolledUsageRepository polledUsageRepository;
        private List<PolledUsageRecord> polledRecords;
        private UsageRollupProcessor usageRollupProcessor;
        private Calendar initialPollTime;
        private Calendar hourToProcess;

        @Before
        public void standUp() {
            loadbalancerIds = new ArrayList<Integer>();
            loadbalancerIds.add(lbId);
            usageRollupProcessor = new UsageRollupProcessorImpl();
            initialPollTime = new GregorianCalendar(2013, Calendar.MARCH, 20, 10, 0, 0);
            hourToProcess = new GregorianCalendar(2013, Calendar.MARCH, 20, 10, 0, 0);

            List<GeneratorPojo> generatorPojoList = new ArrayList<GeneratorPojo>();
            generatorPojoList.add(new GeneratorPojo(accountId, lbId, 24));
            polledRecords = PolledUsageRecordGenerator.generate(generatorPojoList, initialPollTime);
            when(polledUsageRepository.getAllRecords(loadbalancerIds)).thenReturn(polledRecords);
        }

        @Test
        public void shouldCreateTwoRecordsIfOnlyOneEventWithFewPolls(){
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 2));
            List<String> eventTypes = new ArrayList<String>();
            eventTypes.add(null);
            eventTypes.add(UsageEvent.SSL_ONLY_ON.name());
            polledRecords = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime, eventTypes);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(polledRecords, hourToProcess);
            Assert.assertEquals(2, processedUsages.size());
        }

        @Test
        public void shouldCreateTwoRecordsIfOnlyOneEventWithManyPolls(){
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 6));
            List<String> eventTypes = new ArrayList<String>();
            eventTypes.add(null);
            eventTypes.add(null);
            eventTypes.add(UsageEvent.SSL_ONLY_ON.name());
            eventTypes.add(null);
            eventTypes.add(null);
            eventTypes.add(null);
            polledRecords = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime, eventTypes);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(polledRecords, hourToProcess);
            Assert.assertEquals(2, processedUsages.size());
        }

        @Test
        public void shouldCreateOneMoreRecordThanEvents(){
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 6));
            List<String> eventTypes = new ArrayList<String>();
            eventTypes.add(null);
            eventTypes.add(UsageEvent.SSL_ONLY_ON.name());
            eventTypes.add(UsageEvent.SSL_MIXED_ON.name());
            eventTypes.add(UsageEvent.SSL_OFF.name());
            eventTypes.add(UsageEvent.SUSPEND_LOADBALANCER.name());
            eventTypes.add(UsageEvent.UNSUSPEND_LOADBALANCER.name());
            polledRecords = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime, eventTypes);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(polledRecords, hourToProcess);
            Assert.assertEquals(6, processedUsages.size());
        }

        @Test
        public void shouldCreateTwoUsageRecordsFromTwoPolledRecordsAndBandwidthOnlyOnFirstUsageRecord(){
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 2));
            List<String> eventTypes = new ArrayList<String>();
            eventTypes.add(null);
            eventTypes.add(UsageEvent.SSL_ONLY_ON.name());
            polledRecords = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime, eventTypes);
            polledRecords.get(0).setOutgoingTransfer(100);
            polledRecords.get(0).setIncomingTransfer(1000);
            polledRecords.get(1).setOutgoingTransfer(100);
            polledRecords.get(1).setIncomingTransfer(1000);
            polledRecords.get(1).setOutgoingTransferSsl(100);
            polledRecords.get(1).setIncomingTransferSsl(1000);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(polledRecords, hourToProcess);
            Assert.assertEquals(2, processedUsages.size());
            Assert.assertEquals(200, processedUsages.get(0).getOutgoingTransfer().longValue());
            Assert.assertEquals(2000, processedUsages.get(0).getIncomingTransfer().longValue());
            Assert.assertEquals(100, processedUsages.get(0).getOutgoingTransferSsl().longValue());
            Assert.assertEquals(1000, processedUsages.get(0).getIncomingTransferSsl().longValue());
            Assert.assertNull(processedUsages.get(0).getEventType());
            Assert.assertEquals(0, processedUsages.get(1).getOutgoingTransfer().longValue());
            Assert.assertEquals(0, processedUsages.get(1).getIncomingTransfer().longValue());
            Assert.assertEquals(0, processedUsages.get(1).getOutgoingTransferSsl().longValue());
            Assert.assertEquals(0, processedUsages.get(1).getIncomingTransferSsl().longValue());
            Assert.assertEquals(UsageEvent.SSL_ONLY_ON.name(), processedUsages.get(1).getEventType());
        }

        @Test
        public void shouldCreateTwoUsageRecordsFromManyPolledRecordsAndBandwidthSplitBetweenFirstAndSecondUsageRecord(){
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 5));
            List<String> eventTypes = new ArrayList<String>();
            eventTypes.add(null);
            eventTypes.add(null);
            eventTypes.add(UsageEvent.SSL_ONLY_ON.name());
            eventTypes.add(null);
            eventTypes.add(null);
            polledRecords = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime, eventTypes);
            polledRecords.get(0).setOutgoingTransfer(100);
            polledRecords.get(0).setIncomingTransfer(1000);
            polledRecords.get(0).setOutgoingTransferSsl(100);
            polledRecords.get(0).setIncomingTransferSsl(1000);

            polledRecords.get(1).setOutgoingTransfer(100);
            polledRecords.get(1).setIncomingTransfer(1000);
            polledRecords.get(1).setOutgoingTransferSsl(100);
            polledRecords.get(1).setIncomingTransferSsl(1000);

            polledRecords.get(2).setOutgoingTransfer(100);
            polledRecords.get(2).setIncomingTransfer(1000);
            polledRecords.get(2).setOutgoingTransferSsl(100);
            polledRecords.get(2).setIncomingTransferSsl(1000);

            polledRecords.get(3).setOutgoingTransferSsl(100);
            polledRecords.get(3).setIncomingTransferSsl(1000);

            polledRecords.get(4).setOutgoingTransferSsl(100);
            polledRecords.get(4).setIncomingTransferSsl(1000);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(polledRecords, hourToProcess);
            Assert.assertEquals(2, processedUsages.size());
            Assert.assertEquals(300, processedUsages.get(0).getOutgoingTransfer().longValue());
            Assert.assertEquals(3000, processedUsages.get(0).getIncomingTransfer().longValue());
            Assert.assertEquals(300, processedUsages.get(0).getOutgoingTransferSsl().longValue());
            Assert.assertEquals(3000, processedUsages.get(0).getIncomingTransferSsl().longValue());
            Assert.assertEquals(0, processedUsages.get(1).getOutgoingTransfer().longValue());
            Assert.assertEquals(0, processedUsages.get(1).getIncomingTransfer().longValue());
            Assert.assertEquals(200, processedUsages.get(1).getOutgoingTransferSsl().longValue());
            Assert.assertEquals(2000, processedUsages.get(1).getIncomingTransferSsl().longValue());
        }

        @Test
        public void shouldStartRecordOnStartTimeOfCreateLBEvent(){
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 11));
            polledRecords = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime);
            polledRecords.get(0).setEventType(UsageEvent.CREATE_LOADBALANCER.name());
            polledRecords.get(0).getPollTime().add(Calendar.MINUTE, 2);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(polledRecords, hourToProcess);
            Calendar compTime = Calendar.getInstance();
            compTime.setTime(polledRecords.get(0).getPollTime().getTime());
            Assert.assertEquals(1, processedUsages.size());
            Assert.assertEquals(compTime, processedUsages.get(0).getStartTime());
            compTime.setTime(hourToProcess.getTime());
            compTime.add(Calendar.HOUR,  1);
            Assert.assertEquals(compTime, processedUsages.get(0).getEndTime());
        }

        @Test
        public void shouldEndRecordOnEndTimeOfDeleteLBEvent(){
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 8));
            polledRecords = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime);
            polledRecords.get(0).setEventType(UsageEvent.CREATE_LOADBALANCER.name());
            polledRecords.get(0).getPollTime().add(Calendar.MINUTE, 2);
            polledRecords.get(7).getPollTime().add(Calendar.MINUTE, -2);
            polledRecords.get(7).setEventType(UsageEvent.DELETE_LOADBALANCER.name());
            List<Usage> processedUsages = usageRollupProcessor.processRecords(polledRecords, hourToProcess);
            Calendar compTime = Calendar.getInstance();
            compTime.setTime(polledRecords.get(0).getPollTime().getTime());
            Assert.assertEquals(2, processedUsages.size());
            Assert.assertEquals(compTime, processedUsages.get(0).getStartTime());
            compTime = Calendar.getInstance();
            compTime.setTime(polledRecords.get(7).getPollTime().getTime());
            Assert.assertEquals(compTime, processedUsages.get(1).getStartTime());
            Assert.assertEquals(compTime, processedUsages.get(1).getEndTime());
        }

        @Test
        public void shouldHaveBandwidthOnRecordBeforeEvent(){
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 2));
            polledRecords = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime);
            polledRecords.get(1).setOutgoingTransfer(12345);
            polledRecords.get(1).setIncomingTransfer(54321);
            polledRecords.get(1).setEventType(UsageEvent.DELETE_LOADBALANCER.name());
            List<Usage> processedUsages = usageRollupProcessor.processRecords(polledRecords, hourToProcess);
            Assert.assertEquals(2, processedUsages.size());
            Assert.assertEquals(polledRecords.get(1).getIncomingTransfer(), processedUsages.get(0).getIncomingTransfer().longValue());
            Assert.assertEquals(polledRecords.get(1).getOutgoingTransfer(), processedUsages.get(0).getOutgoingTransfer().longValue());
            Assert.assertEquals(0, processedUsages.get(1).getIncomingTransfer().longValue());
            Assert.assertEquals(0, processedUsages.get(1).getOutgoingTransfer().longValue());
        }

        @Test
        public void shouldCreateCreateTwoRecordsIfEventIsFirstPolledRecordOfHour(){
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 1));
            polledRecords = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime);
            polledRecords.get(0).setOutgoingTransfer(12345);
            polledRecords.get(0).setIncomingTransfer(54321);
            polledRecords.get(0).getPollTime().add(Calendar.MINUTE, 1);
            polledRecords.get(0).setEventType(UsageEvent.DELETE_LOADBALANCER.name());
            Calendar compTime = Calendar.getInstance();
            compTime.setTime(initialPollTime.getTime());
            List<Usage> processedUsages = usageRollupProcessor.processRecords(polledRecords, hourToProcess);
            Assert.assertEquals(2, processedUsages.size());
            Assert.assertEquals(polledRecords.get(0).getIncomingTransfer(), processedUsages.get(0).getIncomingTransfer().longValue());
            Assert.assertEquals(polledRecords.get(0).getOutgoingTransfer(), processedUsages.get(0).getOutgoingTransfer().longValue());
            Assert.assertNull(processedUsages.get(0).getEventType());
            Assert.assertEquals(compTime.get(Calendar.HOUR), processedUsages.get(0).getStartTime().get(Calendar.HOUR));
            Assert.assertEquals(0, processedUsages.get(0).getStartTime().get(Calendar.MINUTE));
            Assert.assertEquals(0, processedUsages.get(0).getStartTime().get(Calendar.SECOND));
            Assert.assertEquals(0, processedUsages.get(0).getStartTime().get(Calendar.MILLISECOND));
            Assert.assertEquals(compTime, processedUsages.get(0).getEndTime());
            Assert.assertEquals(0, processedUsages.get(1).getIncomingTransfer().longValue());
            Assert.assertEquals(0, processedUsages.get(1).getOutgoingTransfer().longValue());
            Assert.assertEquals(UsageEvent.DELETE_LOADBALANCER.name(), processedUsages.get(1).getEventType());
            Assert.assertEquals(compTime, processedUsages.get(1).getStartTime());
            Assert.assertEquals(compTime, processedUsages.get(1).getEndTime());
        }

        @Test
        public void shouldIncreaseNumVipsWhenCreateVIPEventEncountered(){
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 2));
            List<String> eventTypes = new ArrayList<String>();
            eventTypes.add(null);
            eventTypes.add(UsageEvent.CREATE_VIRTUAL_IP.name());
            polledRecords = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime, eventTypes);
            polledRecords.get(1).setNumVips(2);
            Calendar compTime = Calendar.getInstance();
            compTime.setTime(initialPollTime.getTime());
            List<Usage> processedUsages = usageRollupProcessor.processRecords(polledRecords, hourToProcess);
            Assert.assertEquals(2, processedUsages.size());
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenMultipleHoursOfPolledUsagesWithNoEvents{
        @Ignore
        @Test
        public void placeholder(){}
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenMultipleHoursOfPolledUsagesWithEvents{
        @Ignore
        @Test
        public void placeholder(){}
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenBreakingPolledRecordsDownByLbId {
        private Calendar initialPollTime;
        private UsageRollupProcessor usageRollupProcessor;
        private List<PolledUsageRecord> polledUsageRecords;

        @Before
        public void standUp() {
            usageRollupProcessor = new UsageRollupProcessorImpl();
            polledUsageRecords = new ArrayList<PolledUsageRecord>();
            initialPollTime = new GregorianCalendar(2013, Calendar.MARCH, 20, 10, 0, 0);
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
            polledUsageRecords = PolledUsageRecordGenerator.generate(usagePojoList, initialPollTime);
            Map<Integer, List<PolledUsageRecord>> usagesByLbId = usageRollupProcessor.breakDownUsagesByLbId(polledUsageRecords);

            Assert.assertEquals(usagePojoList.size(), usagesByLbId.size());
            Assert.assertEquals(usagePojoList.get(0).getNumRecords(), usagesByLbId.get(1).size());
        }

        @Test
        public void shouldReturnManyRecordsWhenManyPolledRecordsExistForALoadBalancer(){
            List<GeneratorPojo> usagePojoList = new ArrayList<GeneratorPojo>();
            usagePojoList.add(new GeneratorPojo(5806065, 1, 1, 30));
            polledUsageRecords = PolledUsageRecordGenerator.generate(usagePojoList, initialPollTime);
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

            polledUsageRecords = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime);
            Map<Integer, List<PolledUsageRecord>> usagesByLbId = usageRollupProcessor.breakDownUsagesByLbId(polledUsageRecords);

            Assert.assertEquals(generatorPojos.size(), usagesByLbId.size());
            for(int i = 0; i < randomLBCount; i++){
                Assert.assertEquals(generatorPojos.get(i).getNumRecords(), usagesByLbId.get(i).size());
            }
        }
    }
}
