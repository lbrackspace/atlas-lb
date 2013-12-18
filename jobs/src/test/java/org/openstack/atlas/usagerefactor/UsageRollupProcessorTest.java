package org.openstack.atlas.usagerefactor;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.LbIdAccountId;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.generator.GeneratorPojo;
import org.openstack.atlas.usagerefactor.generator.PolledUsageRecordGenerator;

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
        private List<LoadBalancerMergedHostUsage> LoadBalancerMergedHosts;
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
            LoadBalancerMergedHosts = PolledUsageRecordGenerator.generate(generatorPojoList, initialPollTime);
            when(polledUsageRepository.getAllRecords(loadbalancerIds)).thenReturn(LoadBalancerMergedHosts);
        }

        @Test
        public void shouldNotCreateAnHourlyRecordWhenNoFiveMinuteRecords() {
            LoadBalancerMergedHosts.clear();
            List<LoadBalancerMergedHostUsage> alls = polledUsageRepository.getAllRecords(loadbalancerIds);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(alls, hourToProcess, new HashSet<LbIdAccountId>());
            Assert.assertTrue(processedUsages.isEmpty());
        }

        @Test
        public void shouldCreateOneHourlyRecord() {
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 11));
            LoadBalancerMergedHosts = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(LoadBalancerMergedHosts, hourToProcess,  new HashSet<LbIdAccountId>());
            Assert.assertEquals(1, processedUsages.size());
        }

        @Test
        public void shouldCreateOneHourlyRecordPerLB() {
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            int randomLBCount = new Random().nextInt(100) + 1;
            for (int lbId = 0; lbId < randomLBCount; lbId++) {
                generatorPojos.add(new GeneratorPojo(5806065, lbId, 1, 30));
            }
            LoadBalancerMergedHosts = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(LoadBalancerMergedHosts, hourToProcess,  new HashSet<LbIdAccountId>());
            Assert.assertEquals(randomLBCount, processedUsages.size());
            for (int lbId = 0; lbId < randomLBCount; lbId++) {
                List<Usage> lbUsageList = new ArrayList<Usage>();
                for (Usage processedUsage : processedUsages) {
                    if (processedUsage.getLoadbalancer().getId() == lbId) {
                        lbUsageList.add(processedUsage);
                    }
                }
                Assert.assertEquals(1, lbUsageList.size());
            }
        }

        @Test
        public void shouldCreateOneRecordWithStartTimeOnTheHourAndEndTimeOnTheNextHour() {
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 11));
            LoadBalancerMergedHosts = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(LoadBalancerMergedHosts, hourToProcess,  new HashSet<LbIdAccountId>());
            Calendar compTime = Calendar.getInstance();
            compTime.setTime(hourToProcess.getTime());
            Assert.assertEquals(1, processedUsages.size());
            Assert.assertEquals(compTime, processedUsages.get(0).getStartTime());
            compTime.add(Calendar.HOUR, 1);
            Assert.assertEquals(compTime, processedUsages.get(0).getEndTime());
        }

        @Test
        public void shouldSumAllBandwidthIntoOneRecord() {
            int numLBPolls = 11;
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, numLBPolls));
            LoadBalancerMergedHosts = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime);
            long increment = 20530932;
            long outgoingTransfer = 123021;
            long incomingTransfer = 1001421;
            long outgoingTransferSsl = 23242;
            long incomingTransferSsl = 928340;
            long totBandwidthOut = 0;
            long totBandwidthIn = 0;
            long totBandwidthOutSsl = 0;
            long totBandwidthInSsl = 0;
            for (LoadBalancerMergedHostUsage LoadBalancerMergedHost : LoadBalancerMergedHosts) {
                LoadBalancerMergedHost.setOutgoingTransfer(outgoingTransfer);
                LoadBalancerMergedHost.setIncomingTransfer(incomingTransfer);
                LoadBalancerMergedHost.setOutgoingTransferSsl(outgoingTransferSsl);
                LoadBalancerMergedHost.setIncomingTransferSsl(incomingTransferSsl);
                totBandwidthOut += outgoingTransfer;
                totBandwidthIn += incomingTransfer;
                totBandwidthOutSsl += outgoingTransferSsl;
                totBandwidthInSsl += incomingTransferSsl;
                outgoingTransfer += increment;
                incomingTransfer += increment;
                outgoingTransferSsl += increment;
                incomingTransferSsl += increment;
            }
            List<Usage> processedUsages = usageRollupProcessor.processRecords(LoadBalancerMergedHosts, hourToProcess,  new HashSet<LbIdAccountId>());
            Assert.assertEquals(1, processedUsages.size());
            Assert.assertEquals(totBandwidthOut - 123021, processedUsages.get(0).getOutgoingTransfer().longValue());
            Assert.assertEquals(totBandwidthIn - 1001421, processedUsages.get(0).getIncomingTransfer().longValue());
            Assert.assertEquals(totBandwidthOutSsl - 23242, processedUsages.get(0).getOutgoingTransferSsl().longValue());
            Assert.assertEquals(totBandwidthInSsl - 928340, processedUsages.get(0).getIncomingTransferSsl().longValue());
        }

        @Test
        public void shouldAverageAllConcurrentConnectionsForOneRecord() {
            int numLBPolls = 11;
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, numLBPolls));
            LoadBalancerMergedHosts = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime);

            int ccs = 4;
            int ccsIncrement = 10;
            int ccsSsl = 8;
            int ccsSslIncrement = 15;
            int totalCcs = 0;
            int totalCcsSsl = 0;
            for(LoadBalancerMergedHostUsage LoadBalancerMergedHost : LoadBalancerMergedHosts){
                LoadBalancerMergedHost.setConcurrentConnections(ccs);
                LoadBalancerMergedHost.setConcurrentConnectionsSsl(ccsSsl);
                totalCcs += ccs;
                totalCcsSsl += ccsSsl;
                ccs += ccsIncrement;
                ccsSsl += ccsSslIncrement;
            }
            double expectedACC = (totalCcs - LoadBalancerMergedHosts.get(0).getConcurrentConnections()) / (numLBPolls - 1.0);
            double expectedACCSsl = (totalCcsSsl - LoadBalancerMergedHosts.get(0).getConcurrentConnectionsSsl()) / (numLBPolls - 1.0);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(LoadBalancerMergedHosts, hourToProcess,  new HashSet<LbIdAccountId>());
            Assert.assertEquals(1, processedUsages.size());
            Assert.assertEquals(expectedACC, processedUsages.get(0).getAverageConcurrentConnections(), 0);
            Assert.assertEquals(expectedACCSsl, processedUsages.get(0).getAverageConcurrentConnectionsSsl(), 0);
        }

        @Test
        public void shouldMaintainTagsBitmask() {
            int numLBPolls = 11;
            int tagsBitmask = 1;
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, numLBPolls));
            LoadBalancerMergedHosts = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime, tagsBitmask);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(LoadBalancerMergedHosts, hourToProcess,  new HashSet<LbIdAccountId>());
            Assert.assertEquals(1, processedUsages.size());
            Assert.assertEquals(tagsBitmask, (int) processedUsages.get(0).getTags());
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class OneHourOfPolledUsageWithEvents {

        private int accountId = 5806065;
        private int lbId = 1234;

        private List<Integer> loadbalancerIds;
        private List<LoadBalancerMergedHostUsage> loadBalancerMergedHosts;
        @Mock
        private UsageRepository usageRepository;
        @InjectMocks
        private UsageRollupProcessor usageRollupProcessor = new UsageRollupProcessorImpl();
        private Calendar initialPollTime;
        private Calendar hourToProcess;
        private Usage mostRecentUsage;

        @Before
        public void standUp() throws EntityNotFoundException {
            mostRecentUsage = new Usage();
            when(usageRepository.getMostRecentUsageForLoadBalancer(Matchers.<Integer>any())).thenReturn(mostRecentUsage);

            loadbalancerIds = new ArrayList<Integer>();
            loadbalancerIds.add(lbId);
            initialPollTime = new GregorianCalendar(2013, Calendar.MARCH, 20, 10, 0, 0);
            hourToProcess = new GregorianCalendar(2013, Calendar.MARCH, 20, 10, 0, 0);

            List<GeneratorPojo> generatorPojoList = new ArrayList<GeneratorPojo>();
            generatorPojoList.add(new GeneratorPojo(accountId, lbId, 24));
            loadBalancerMergedHosts = PolledUsageRecordGenerator.generate(generatorPojoList, initialPollTime);
        }

        @Test
        public void shouldCreateTwoRecordsIfOnlyOneEventWithFewPolls() {
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 2));
            List<UsageEvent> eventTypes = new ArrayList<UsageEvent>();
            eventTypes.add(null);
            eventTypes.add(UsageEvent.SSL_ONLY_ON);
            loadBalancerMergedHosts = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime, eventTypes);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(loadBalancerMergedHosts, hourToProcess,  new HashSet<LbIdAccountId>());

            Assert.assertEquals(2, processedUsages.size());
            Assert.assertNull(processedUsages.get(0).getEventType());
            Assert.assertEquals(UsageEvent.SSL_ONLY_ON.name(), processedUsages.get(1).getEventType());
        }

        @Test
        public void shouldCreateTwoRecordsIfOnlyOneEventWithManyPolls() {
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 6));
            List<UsageEvent> eventTypes = new ArrayList<UsageEvent>();
            eventTypes.add(null);
            eventTypes.add(null);
            eventTypes.add(UsageEvent.SSL_ONLY_ON);
            eventTypes.add(null);
            eventTypes.add(null);
            eventTypes.add(null);
            loadBalancerMergedHosts = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime, eventTypes);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(loadBalancerMergedHosts, hourToProcess,  new HashSet<LbIdAccountId>());
            Assert.assertEquals(2, processedUsages.size());
            Assert.assertNull(processedUsages.get(0).getEventType());
            Assert.assertEquals(UsageEvent.SSL_ONLY_ON.name(), processedUsages.get(1).getEventType());
        }

        @Test
        public void shouldCreateOneMoreRecordThanEvents() {
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 6));
            List<UsageEvent> eventTypes = new ArrayList<UsageEvent>();
            eventTypes.add(null);
            eventTypes.add(UsageEvent.SSL_ONLY_ON);
            eventTypes.add(UsageEvent.SSL_MIXED_ON);
            eventTypes.add(UsageEvent.SSL_OFF);
            eventTypes.add(UsageEvent.SUSPEND_LOADBALANCER);
            eventTypes.add(UsageEvent.UNSUSPEND_LOADBALANCER);
            loadBalancerMergedHosts = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime, eventTypes);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(loadBalancerMergedHosts, hourToProcess,  new HashSet<LbIdAccountId>());
            Assert.assertEquals(6, processedUsages.size());
        }

        @Test
        public void shouldStoreUsageFromEventRecordToPreviousRecord() {
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 2));
            List<UsageEvent> eventTypes = new ArrayList<UsageEvent>();
            eventTypes.add(null);
            eventTypes.add(UsageEvent.SSL_ONLY_ON);
            loadBalancerMergedHosts = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime, eventTypes);
            loadBalancerMergedHosts.get(0).setOutgoingTransfer(100);
            loadBalancerMergedHosts.get(0).setIncomingTransfer(1000);
            loadBalancerMergedHosts.get(1).setOutgoingTransfer(100);
            loadBalancerMergedHosts.get(1).setIncomingTransfer(1000);
            loadBalancerMergedHosts.get(1).setOutgoingTransferSsl(100);
            loadBalancerMergedHosts.get(1).setIncomingTransferSsl(1000);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(loadBalancerMergedHosts, hourToProcess,  new HashSet<LbIdAccountId>());
            Assert.assertEquals(2, processedUsages.size());
            Assert.assertEquals(100, processedUsages.get(0).getOutgoingTransfer().longValue());
            Assert.assertEquals(1000, processedUsages.get(0).getIncomingTransfer().longValue());
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
        public void shouldAppropriateUsageToCorrectRecordsWhenEventOccurs() {
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 5));
            List<UsageEvent> eventTypes = new ArrayList<UsageEvent>();
            eventTypes.add(null);
            eventTypes.add(null);
            eventTypes.add(UsageEvent.SSL_ONLY_ON);
            eventTypes.add(null);
            eventTypes.add(null);
            loadBalancerMergedHosts = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime, eventTypes);
            loadBalancerMergedHosts.get(0).setOutgoingTransfer(100);
            loadBalancerMergedHosts.get(0).setIncomingTransfer(1000);
            loadBalancerMergedHosts.get(0).setOutgoingTransferSsl(100);
            loadBalancerMergedHosts.get(0).setIncomingTransferSsl(1000);

            loadBalancerMergedHosts.get(1).setOutgoingTransfer(100);
            loadBalancerMergedHosts.get(1).setIncomingTransfer(1000);
            loadBalancerMergedHosts.get(1).setOutgoingTransferSsl(100);
            loadBalancerMergedHosts.get(1).setIncomingTransferSsl(1000);

            loadBalancerMergedHosts.get(2).setOutgoingTransfer(100);
            loadBalancerMergedHosts.get(2).setIncomingTransfer(1000);
            loadBalancerMergedHosts.get(2).setOutgoingTransferSsl(100);
            loadBalancerMergedHosts.get(2).setIncomingTransferSsl(1000);

            loadBalancerMergedHosts.get(3).setOutgoingTransferSsl(100);
            loadBalancerMergedHosts.get(3).setIncomingTransferSsl(1000);

            loadBalancerMergedHosts.get(4).setOutgoingTransferSsl(100);
            loadBalancerMergedHosts.get(4).setIncomingTransferSsl(1000);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(loadBalancerMergedHosts, hourToProcess,  new HashSet<LbIdAccountId>());
            Assert.assertEquals(2, processedUsages.size());
            Assert.assertEquals(200, processedUsages.get(0).getOutgoingTransfer().longValue());
            Assert.assertEquals(2000, processedUsages.get(0).getIncomingTransfer().longValue());
            Assert.assertEquals(200, processedUsages.get(0).getOutgoingTransferSsl().longValue());
            Assert.assertEquals(2000, processedUsages.get(0).getIncomingTransferSsl().longValue());
            Assert.assertEquals(0, processedUsages.get(1).getOutgoingTransfer().longValue());
            Assert.assertEquals(0, processedUsages.get(1).getIncomingTransfer().longValue());
            Assert.assertEquals(200, processedUsages.get(1).getOutgoingTransferSsl().longValue());
            Assert.assertEquals(2000, processedUsages.get(1).getIncomingTransferSsl().longValue());
        }

        @Test
        public void recordStartTimeShouldEqualToTimeOfCreateLBEvent() {
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 11));
            loadBalancerMergedHosts = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime);
            loadBalancerMergedHosts.get(0).setEventType(UsageEvent.CREATE_LOADBALANCER);
            loadBalancerMergedHosts.get(0).getPollTime().add(Calendar.MINUTE, 2);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(loadBalancerMergedHosts, hourToProcess,  new HashSet<LbIdAccountId>());
            Calendar compTime = Calendar.getInstance();
            compTime.setTime(loadBalancerMergedHosts.get(0).getPollTime().getTime());
            Assert.assertEquals(1, processedUsages.size());
            Assert.assertEquals(compTime, processedUsages.get(0).getStartTime());
            compTime.setTime(hourToProcess.getTime());
            compTime.add(Calendar.HOUR, 1);
            Assert.assertEquals(compTime, processedUsages.get(0).getEndTime());
        }

        @Test
        public void recordEndTimeShouldEqualToTimeOfDeleteLBEvent() {
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 8));
            loadBalancerMergedHosts = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime);
            loadBalancerMergedHosts.get(0).setEventType(UsageEvent.CREATE_LOADBALANCER);
            loadBalancerMergedHosts.get(0).getPollTime().add(Calendar.MINUTE, 2);
            loadBalancerMergedHosts.get(7).getPollTime().add(Calendar.MINUTE, -2);
            loadBalancerMergedHosts.get(7).setEventType(UsageEvent.DELETE_LOADBALANCER);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(loadBalancerMergedHosts, hourToProcess,  new HashSet<LbIdAccountId>());
            Calendar compTime = Calendar.getInstance();
            compTime.setTime(loadBalancerMergedHosts.get(0).getPollTime().getTime());
            Assert.assertEquals(2, processedUsages.size());
            Assert.assertEquals(compTime, processedUsages.get(0).getStartTime());
            compTime = Calendar.getInstance();
            compTime.setTime(loadBalancerMergedHosts.get(7).getPollTime().getTime());
            Assert.assertEquals(compTime, processedUsages.get(1).getStartTime());
            Assert.assertEquals(compTime, processedUsages.get(1).getEndTime());
        }

        @Test
        public void shouldHaveBandwidthOnRecordBeforeDeleteEvent() {
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 2));
            loadBalancerMergedHosts = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime);
            loadBalancerMergedHosts.get(1).setOutgoingTransfer(12345);
            loadBalancerMergedHosts.get(1).setIncomingTransfer(54321);
            loadBalancerMergedHosts.get(1).setEventType(UsageEvent.DELETE_LOADBALANCER);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(loadBalancerMergedHosts, hourToProcess,  new HashSet<LbIdAccountId>());
            Assert.assertEquals(2, processedUsages.size());
            Assert.assertEquals(loadBalancerMergedHosts.get(1).getIncomingTransfer(), processedUsages.get(0).getIncomingTransfer().longValue());
            Assert.assertEquals(loadBalancerMergedHosts.get(1).getOutgoingTransfer(), processedUsages.get(0).getOutgoingTransfer().longValue());
            Assert.assertEquals(0, processedUsages.get(1).getIncomingTransfer().longValue());
            Assert.assertEquals(0, processedUsages.get(1).getOutgoingTransfer().longValue());
        }

        @Test
        public void shouldCreateTwoRecordsIfEventIsFirstRecordOfHour() {
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 1));
            loadBalancerMergedHosts = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime);
            loadBalancerMergedHosts.get(0).setOutgoingTransfer(12345);
            loadBalancerMergedHosts.get(0).setIncomingTransfer(54321);
            loadBalancerMergedHosts.get(0).getPollTime().add(Calendar.MINUTE, 1);
            loadBalancerMergedHosts.get(0).setEventType(UsageEvent.DELETE_LOADBALANCER);
            Calendar compTime = Calendar.getInstance();
            compTime.setTime(initialPollTime.getTime());
            List<Usage> processedUsages = usageRollupProcessor.processRecords(loadBalancerMergedHosts, hourToProcess,  new HashSet<LbIdAccountId>());
            Assert.assertEquals(2, processedUsages.size());
            Assert.assertEquals(0, processedUsages.get(0).getIncomingTransfer().longValue());
            Assert.assertEquals(0, processedUsages.get(0).getOutgoingTransfer().longValue());
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
        public void shouldIncreaseNumVipsWhenCreateVIPEventEncountered() {
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 3));
            List<UsageEvent> eventTypes = new ArrayList<UsageEvent>();
            eventTypes.add(null);
            eventTypes.add(UsageEvent.CREATE_VIRTUAL_IP);
            eventTypes.add(null);
            loadBalancerMergedHosts = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime, eventTypes);
            loadBalancerMergedHosts.get(1).setNumVips(2);
            loadBalancerMergedHosts.get(2).setNumVips(2);
            Calendar compTime = Calendar.getInstance();
            compTime.setTime(initialPollTime.getTime());
            List<Usage> processedUsages = usageRollupProcessor.processRecords(loadBalancerMergedHosts, hourToProcess,  new HashSet<LbIdAccountId>());
            Assert.assertEquals(2, processedUsages.size());
            Assert.assertEquals(1, (int) processedUsages.get(0).getNumVips());
            Assert.assertEquals(2, (int) processedUsages.get(1).getNumVips());
        }

        @Test
        public void shouldCalculateAverageConcurrentConnectionsWithEvents() {
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 8));
            List<UsageEvent> eventTypes = new ArrayList<UsageEvent>();
            eventTypes.add(null);
            eventTypes.add(UsageEvent.SSL_ONLY_ON);
            eventTypes.add(null);
            eventTypes.add(UsageEvent.SSL_MIXED_ON);
            eventTypes.add(null);
            eventTypes.add(null);
            eventTypes.add(UsageEvent.SSL_OFF);
            eventTypes.add(null);
            loadBalancerMergedHosts = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime, eventTypes);
            loadBalancerMergedHosts.get(0).setConcurrentConnections(20);
            loadBalancerMergedHosts.get(1).setConcurrentConnections(30);
            loadBalancerMergedHosts.get(2).setConcurrentConnectionsSsl(12);
            loadBalancerMergedHosts.get(3).setConcurrentConnectionsSsl(36);
            loadBalancerMergedHosts.get(4).setConcurrentConnections(52);
            loadBalancerMergedHosts.get(4).setConcurrentConnectionsSsl(43);
            loadBalancerMergedHosts.get(5).setConcurrentConnections(145);
            loadBalancerMergedHosts.get(5).setConcurrentConnectionsSsl(1);
            loadBalancerMergedHosts.get(6).setConcurrentConnections(123);
            loadBalancerMergedHosts.get(6).setConcurrentConnectionsSsl(92);
            loadBalancerMergedHosts.get(7).setConcurrentConnections(21);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(loadBalancerMergedHosts, hourToProcess,  new HashSet<LbIdAccountId>());
            Assert.assertEquals(4, processedUsages.size());
            double expectedACC = 30;
            double expectedACCSsl = 0;
            Assert.assertEquals(expectedACC, processedUsages.get(0).getAverageConcurrentConnections(), 0);
            Assert.assertEquals(expectedACCSsl, processedUsages.get(0).getAverageConcurrentConnectionsSsl(), 0);
            expectedACC = 0;
            expectedACCSsl = (12 + 36) / 2.0;
            Assert.assertEquals(expectedACC, processedUsages.get(1).getAverageConcurrentConnections(), 0);
            Assert.assertEquals(expectedACCSsl, processedUsages.get(1).getAverageConcurrentConnectionsSsl(), 0);
            expectedACC = (52 + 145 + 123) / 3.0;
            expectedACCSsl = (43 + 1 + 92) / 3.0;
            Assert.assertEquals(expectedACC, processedUsages.get(2).getAverageConcurrentConnections(), 0);
            Assert.assertEquals(expectedACCSsl, processedUsages.get(2).getAverageConcurrentConnectionsSsl(), 0);
            expectedACC = 21;
            expectedACCSsl = 0;
            Assert.assertEquals(expectedACC, processedUsages.get(3).getAverageConcurrentConnections(), 0);
            Assert.assertEquals(expectedACCSsl, processedUsages.get(3).getAverageConcurrentConnectionsSsl(), 0);
        }

        @Test
        public void shouldProcessCorrectTagsBitmaskForAllEvents() {
            BitTags tags = new BitTags();
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            generatorPojos.add(new GeneratorPojo(5806065, 1234, 16));
            List<UsageEvent> eventTypes = new ArrayList<UsageEvent>();
            eventTypes.add(UsageEvent.CREATE_LOADBALANCER);
            eventTypes.add(null);
            eventTypes.add(UsageEvent.SSL_ONLY_ON);
            eventTypes.add(null);
            eventTypes.add(UsageEvent.SSL_MIXED_ON);
            eventTypes.add(null);
            eventTypes.add(UsageEvent.SSL_OFF);
            eventTypes.add(null);
            eventTypes.add(UsageEvent.SUSPEND_LOADBALANCER);
            eventTypes.add(null);
            eventTypes.add(UsageEvent.UNSUSPEND_LOADBALANCER);
            eventTypes.add(null);
            loadBalancerMergedHosts = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime, eventTypes);
            tags.flipTagOn(BitTag.SERVICENET_LB);
            loadBalancerMergedHosts.get(0).setTagsBitmask(tags.toInt());
            loadBalancerMergedHosts.get(1).setTagsBitmask(tags.toInt());
            tags.flipTagOn(BitTag.SSL);
            loadBalancerMergedHosts.get(2).setTagsBitmask(tags.toInt());
            loadBalancerMergedHosts.get(3).setTagsBitmask(tags.toInt());
            tags.flipTagOn(BitTag.SSL);
            tags.flipTagOn(BitTag.SSL_MIXED_MODE);
            loadBalancerMergedHosts.get(4).setTagsBitmask(tags.toInt());
            loadBalancerMergedHosts.get(5).setTagsBitmask(tags.toInt());
            tags.flipTagOff(BitTag.SSL);
            tags.flipTagOff(BitTag.SSL_MIXED_MODE);
            loadBalancerMergedHosts.get(6).setTagsBitmask(tags.toInt());
            loadBalancerMergedHosts.get(7).setTagsBitmask(tags.toInt());
            loadBalancerMergedHosts.get(8).setTagsBitmask(tags.toInt());
            loadBalancerMergedHosts.get(9).setTagsBitmask(tags.toInt());
            loadBalancerMergedHosts.get(10).setTagsBitmask(tags.toInt());
            loadBalancerMergedHosts.get(11).setTagsBitmask(tags.toInt());
            loadBalancerMergedHosts.get(12).setTagsBitmask(tags.toInt());
            loadBalancerMergedHosts.get(13).setTagsBitmask(tags.toInt());
            loadBalancerMergedHosts.get(14).setTagsBitmask(tags.toInt());
            loadBalancerMergedHosts.get(15).setTagsBitmask(tags.toInt());
            List<Usage> processedUsages = usageRollupProcessor.processRecords(loadBalancerMergedHosts, hourToProcess,  new HashSet<LbIdAccountId>());
            tags.flipAllTagsOff();
            tags.flipTagOn(BitTag.SERVICENET_LB);
            Assert.assertEquals(6, processedUsages.size());
            Assert.assertEquals(tags.toInt(), (int) processedUsages.get(0).getTags());
            tags.flipTagOn(BitTag.SSL);
            Assert.assertEquals(tags.toInt(), (int) processedUsages.get(1).getTags());
            tags.flipTagOn(BitTag.SSL);
            tags.flipTagOn(BitTag.SSL_MIXED_MODE);
            Assert.assertEquals(tags.toInt(), (int) processedUsages.get(2).getTags());
            tags.flipTagOff(BitTag.SSL);
            tags.flipTagOff(BitTag.SSL_MIXED_MODE);
            Assert.assertEquals(tags.toInt(), (int) processedUsages.get(3).getTags());
            Assert.assertEquals(tags.toInt(), (int) processedUsages.get(4).getTags());
            Assert.assertEquals(tags.toInt(), (int) processedUsages.get(5).getTags());
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenBreakingPolledRecordsDownByLbId {
        private Calendar initialPollTime;
        private UsageRollupProcessor usageRollupProcessor;
        private List<LoadBalancerMergedHostUsage> LoadBalancerMergedHostUsages;

        @Before
        public void standUp() {
            usageRollupProcessor = new UsageRollupProcessorImpl();
            LoadBalancerMergedHostUsages = new ArrayList<LoadBalancerMergedHostUsage>();
            initialPollTime = new GregorianCalendar(2013, Calendar.MARCH, 20, 10, 0, 0);
        }

        @Test
        public void shouldReturnEmptyMapWhenNoPolledRecords() {
            Map<LbIdAccountId, List<LoadBalancerMergedHostUsage>> usagesByLbId = usageRollupProcessor.groupUsagesByLbIdAccountId(LoadBalancerMergedHostUsages);

            Assert.assertTrue(usagesByLbId.isEmpty());
        }

        @Test
        public void shouldReturnARecordWhenOnePolledRecordExists() {
            List<GeneratorPojo> usagePojoList = new ArrayList<GeneratorPojo>();
            LbIdAccountId lbIdAccountId = new LbIdAccountId(1, 5806065);
            usagePojoList.add(new GeneratorPojo(lbIdAccountId.getAccountId(), lbIdAccountId.getLbId(), 1));
            LoadBalancerMergedHostUsages = PolledUsageRecordGenerator.generate(usagePojoList, initialPollTime);
            Map<LbIdAccountId, List<LoadBalancerMergedHostUsage>> usagesByLbId = usageRollupProcessor.groupUsagesByLbIdAccountId(LoadBalancerMergedHostUsages);

            Assert.assertEquals(usagePojoList.size(), usagesByLbId.size());
            Assert.assertEquals(usagePojoList.get(0).getNumRecords(), usagesByLbId.get(lbIdAccountId).size());
        }

        @Test
        public void shouldReturnManyRecordsWhenManyPolledRecordsExistForALoadBalancer() {
            List<GeneratorPojo> usagePojoList = new ArrayList<GeneratorPojo>();
            LbIdAccountId lbIdAccountId = new LbIdAccountId(1, 5806065);
            usagePojoList.add(new GeneratorPojo(lbIdAccountId.getAccountId(), lbIdAccountId.getLbId(), 1, 30));
            LoadBalancerMergedHostUsages = PolledUsageRecordGenerator.generate(usagePojoList, initialPollTime);
            Map<LbIdAccountId, List<LoadBalancerMergedHostUsage>> usagesByLbId = usageRollupProcessor.groupUsagesByLbIdAccountId(LoadBalancerMergedHostUsages);

            Assert.assertEquals(usagePojoList.size(), usagesByLbId.size());
            Assert.assertEquals(usagePojoList.get(0).getNumRecords(), usagesByLbId.get(lbIdAccountId).size());
        }

        @Test
        public void shouldReturnManyRecordsWhenManyPolledRecordsExistForManyLoadBalancers() {
            List<GeneratorPojo> generatorPojos = new ArrayList<GeneratorPojo>();
            LbIdAccountId lbIdAccountId = new LbIdAccountId(1, 5806065);
            int randomLBCount = new Random().nextInt(100) + 1;
            for (int lbId = 0; lbId < randomLBCount; lbId++) {
                generatorPojos.add(new GeneratorPojo(5806065, lbId, 1, 30));
            }

            LoadBalancerMergedHostUsages = PolledUsageRecordGenerator.generate(generatorPojos, initialPollTime);
            Map<LbIdAccountId, List<LoadBalancerMergedHostUsage>> usagesByLbId = usageRollupProcessor.groupUsagesByLbIdAccountId(LoadBalancerMergedHostUsages);

            Assert.assertEquals(generatorPojos.size(), usagesByLbId.size());
            for (int i = 0; i < randomLBCount; i++) {
                lbIdAccountId = new LbIdAccountId(i, 5806065);
                Assert.assertEquals(generatorPojos.get(i).getNumRecords(), usagesByLbId.get(lbIdAccountId).size());
            }
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenMultipleHoursOfPolledUsagesWithNoEvents {
        private int accountId = 5806065;
        private int lbId = 1234;

        private Calendar initialPollTime;
        private Calendar hourToProcess;
        private UsageRollupProcessor usageRollupProcessor;
        private List<LoadBalancerMergedHostUsage> LoadBalancerMergedHosts;

        @Before
        public void standUp() {
            usageRollupProcessor = new UsageRollupProcessorImpl();
            LoadBalancerMergedHosts = new ArrayList<LoadBalancerMergedHostUsage>();
            initialPollTime = new GregorianCalendar(2013, Calendar.MARCH, 20, 10, 4, 0);
            hourToProcess = new GregorianCalendar(2013, Calendar.MARCH, 20, 10, 0, 0);
        }

        @Test
        public void shouldStopProcessingRecordsBeforeTheNextHour() {
            List<GeneratorPojo> usagePojoList = new ArrayList<GeneratorPojo>();
            usagePojoList.add(new GeneratorPojo(accountId, lbId, 36));
            LoadBalancerMergedHosts = PolledUsageRecordGenerator.generate(usagePojoList, initialPollTime);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(LoadBalancerMergedHosts, hourToProcess,  new HashSet<LbIdAccountId>());

            Calendar compTime = Calendar.getInstance();
            compTime.setTime(hourToProcess.getTime());
            Assert.assertEquals(1, processedUsages.size());
            Assert.assertEquals(compTime, processedUsages.get(0).getStartTime());
            compTime.add(Calendar.HOUR, 1);
            Assert.assertEquals(compTime, processedUsages.get(0).getEndTime());
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenMultipleHoursOfPolledUsagesWithEvents {
        private int accountId = 5806065;
        private int lbId = 1234;

        private Calendar initialPollTime;
        private Calendar hourToProcess;
        @Mock
        private UsageRepository usageRepository;
        @InjectMocks
        private UsageRollupProcessor usageRollupProcessor = new UsageRollupProcessorImpl();
        private List<LoadBalancerMergedHostUsage> LoadBalancerMergedHosts;
        private Usage mostRecentUsage;

        @Before
        public void standUp() throws EntityNotFoundException {
            mostRecentUsage = new Usage();
            when(usageRepository.getMostRecentUsageForLoadBalancer(Matchers.<Integer>any())).thenReturn(mostRecentUsage);

            LoadBalancerMergedHosts = new ArrayList<LoadBalancerMergedHostUsage>();
            initialPollTime = new GregorianCalendar(2013, Calendar.MARCH, 20, 10, 4, 0);
            hourToProcess = new GregorianCalendar(2013, Calendar.MARCH, 20, 10, 0, 0);
        }

        @Test
        public void shouldStopProcessingRecordsBeforeTheNextHour() {
            List<GeneratorPojo> usagePojoList = new ArrayList<GeneratorPojo>();
            usagePojoList.add(new GeneratorPojo(accountId, lbId, 36));
            LoadBalancerMergedHosts = PolledUsageRecordGenerator.generate(usagePojoList, initialPollTime);
            LoadBalancerMergedHosts.get(0).setEventType(UsageEvent.SSL_MIXED_ON);
            LoadBalancerMergedHosts.get(11).setEventType(UsageEvent.SSL_ONLY_ON);
            LoadBalancerMergedHosts.get(17).setEventType(UsageEvent.SSL_MIXED_ON);
            LoadBalancerMergedHosts.get(22).setEventType(UsageEvent.SSL_ONLY_ON);
            LoadBalancerMergedHosts.get(25).setEventType(UsageEvent.SSL_MIXED_ON);
            LoadBalancerMergedHosts.get(35).setEventType(UsageEvent.SSL_ONLY_ON);
            List<Usage> processedUsages = usageRollupProcessor.processRecords(LoadBalancerMergedHosts, hourToProcess,  new HashSet<LbIdAccountId>());
            Assert.assertEquals(3, processedUsages.size());
            Calendar compTime = Calendar.getInstance();
            compTime.setTime(hourToProcess.getTime());
            Assert.assertEquals(compTime, processedUsages.get(0).getStartTime());
            compTime.set(Calendar.MINUTE, LoadBalancerMergedHosts.get(0).getPollTime().get(Calendar.MINUTE));
            Assert.assertEquals(compTime, processedUsages.get(0).getEndTime());
            Assert.assertEquals(compTime, processedUsages.get(1).getStartTime());
            compTime.set(Calendar.MINUTE, LoadBalancerMergedHosts.get(11).getPollTime().get(Calendar.MINUTE));
            Assert.assertEquals(compTime, processedUsages.get(1).getEndTime());
            Assert.assertEquals(compTime, processedUsages.get(2).getStartTime());
            compTime.set(Calendar.HOUR, hourToProcess.get(Calendar.HOUR) + 1);
            compTime.set(Calendar.MINUTE, 0);
            Assert.assertEquals(compTime, processedUsages.get(2).getEndTime());
            Assert.assertNull(processedUsages.get(0).getEventType());
            Assert.assertEquals(UsageEvent.SSL_MIXED_ON.name(), processedUsages.get(1).getEventType());
            Assert.assertEquals(UsageEvent.SSL_ONLY_ON.name(), processedUsages.get(2).getEventType());
        }
    }
}
