package org.openstack.atlas.api.async;

import org.junit.Ignore;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@Ignore
@RunWith(Enclosed.class)
public class UpdateSslTerminationListenerTest {

//    @RunWith(SpringJUnit4ClassRunner.class)
//    @ContextConfiguration(locations = {"classpath:context.xml"})
//    @Transactional
//    public static class WhenTestingUpdateSslTerminationListener {
//
//        @Autowired
//        public UpdateSslTerminationListener updateSslTerminationListener;
//
//        @Autowired
//        @Qualifier("usageRefactorService")
//        public UsageRefactorService usageRefactorService;
//
//        @Autowired
//        @Qualifier("usageEventProcessorImpl")
//        public UsageEventProcessorImpl usageEventProcessor;
//
//        @Autowired
//        @Qualifier("loadBalancerRepository")
//        public LoadBalancerRepository loadBalancerRepository;
//
//        @Autowired
//        public LoadBalancerService loadBalancerService;
//
//        @Autowired
//        public ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;
//
//        @Autowired
//        public NotificationService notificationService;
//
//        public SnmpUsage snmpUsage;
//        public SnmpUsage snmpUsage2;
//        public List<SnmpUsage> snmpUsages;
//        public LoadBalancer lb;
//
//        @Before
//        public void standUp() throws Exception {
//            loadBalancerRepository = mock(LoadBalancerRepository.class);
//
//            lb = new LoadBalancer();
//            lb.setId(543221);
//            lb.setAccountId(55555);
//
//            snmpUsages = new ArrayList<SnmpUsage>();
//            snmpUsage = new SnmpUsage();
//            snmpUsage.setHostId(1);
//            snmpUsage.setLoadbalancerId(lb.getId());
//            snmpUsage.setBytesIn(1234455);
//            snmpUsage.setBytesInSsl(4321);
//            snmpUsage.setBytesOut(987);
//            snmpUsage.setBytesOutSsl(986);
//            snmpUsage.setConcurrentConnections(1);
//            snmpUsage.setConcurrentConnectionsSsl(3);
//            snmpUsages.add(snmpUsage);
//
//            loadBalancerService = mock(LoadBalancerService.class);
//            notificationService = mock(NotificationService.class);
//            doNothing().when(loadBalancerService).update(Matchers.<LoadBalancer>any());
//            doNothing().when(loadBalancerService).setStatus(Matchers.<LoadBalancer>any(), Matchers.<LoadBalancerStatus>any());
//            doNothing().when(notificationService).saveAlert(Matchers.<Integer>any(), Matchers.<Integer>any(), Matchers.<Exception>any(), Matchers.<String>any(), Matchers.<String>any());
//            doNothing().when(notificationService).saveAlert(Matchers.<Exception>any(), Matchers.<String>any(), Matchers.<String>any());
//            doNothing().when(notificationService).saveSslTerminationEvent(Matchers.<String>any(), Matchers.<Integer>any(), Matchers.<Integer>any(),
//                    Matchers.<Integer>any(), Matchers.<String>any(), Matchers.<String>any(), Matchers.<EventType>any(), Matchers.<CategoryType>any(), Matchers.<EventSeverity>any());
//            doNothing().when(notificationService).saveLoadBalancerEvent(Matchers.<String>any(), Matchers.<Integer>any(), Matchers.<Integer>any(), Matchers.<String>any(),
//                    Matchers.<String>any(), Matchers.<EventType>any(), Matchers.<CategoryType>any(), Matchers.<EventSeverity>any());
//        }
//
//        @Test
//        public void shouldHaveNoPreviousUsagesForTestDB() throws Exception {
//            Calendar starttime = Calendar.getInstance();
//            starttime.roll(Calendar.MONTH, false);
//
//            ObjectMessage m = mock(ObjectMessage.class);
//            MessageDataContainer md = new MessageDataContainer();
//            md.setAccountId(lb.getAccountId());
//            md.setLoadBalancerId(lb.getId());
//            md.setUserName("me");
//
//            ZeusSslTermination zm = new ZeusSslTermination();
//            md.setZeusSslTermination(zm);
//
//            when(loadBalancerService.get(Matchers.anyInt(), Matchers.anyInt())).thenReturn(lb);
//            reverseProxyLoadBalancerService = mock(ReverseProxyLoadBalancerService.class);
//            doNothing().when(reverseProxyLoadBalancerService).updateSslTermination(Matchers.<LoadBalancer>any(), Matchers.<ZeusSslTermination>any());
//
//            updateSslTerminationListener.doOnMessage(m);
//
////            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_ON);
//            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> oUsages = usageRefactorService.getAllLoadBalancerHostUsages();
//            Assert.assertNotNull(oUsages);
//            Assert.assertEquals(1, oUsages.size());
//            Assert.assertEquals(true, oUsages.containsKey(543221));
//        }
//    }
}