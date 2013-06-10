package org.openstack.atlas.usagerefactor;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.openstack.atlas.dbunit.FlatXmlLoader;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.LbIdAccountId;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerMergedHostUsageRepository;
import org.openstack.atlas.usagerefactor.junit.AssertUsage;
import org.openstack.atlas.util.common.CalendarUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.*;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.openstack.atlas.service.domain.events.UsageEvent.*;

/*
    To see what each case is testing please refer to their respective xml
    file for more information.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:dbunit-context.xml"})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DbUnitConfiguration(dataSetLoader = FlatXmlLoader.class)
public class UsageRollupProcessorTest2 {

    @Autowired
    private LoadBalancerMergedHostUsageRepository loadBalancerMergedHostUsageRepository;
    @Mock
    private UsageRepository usageRepository;
    @InjectMocks
    private UsageRollupProcessor usageRollupProcessor = new UsageRollupProcessorImpl();

    private Calendar hourToRollup;
    private Calendar rollupMarker;
    private List<LoadBalancerMergedHostUsage> allUsageRecordsInOrder;
    private List<Usage> processedUsages;
    private Usage mostRecentUsage;
    private Set<LbIdAccountId> lbsActiveDuringHour;

    @Before
    public void standUp() throws EntityNotFoundException {
        initMocks(this);
        mostRecentUsage = new Usage();
        when(usageRepository.getMostRecentUsageForLoadBalancer(Matchers.<Integer>any())).thenReturn(mostRecentUsage);

        lbsActiveDuringHour = new HashSet<LbIdAccountId>();

        hourToRollup = new GregorianCalendar(2013, Calendar.APRIL, 10, 20, 0, 0);
        hourToRollup = CalendarUtils.stripOutMinsAndSecs(hourToRollup);
        rollupMarker = CalendarUtils.copy(hourToRollup);
        rollupMarker.add(Calendar.HOUR, 1);
        allUsageRecordsInOrder = loadBalancerMergedHostUsageRepository.getAllUsageRecordsInOrderBeforeOrEqualToTime(rollupMarker);
    }

    @After
    public void tearDown() {
        lbsActiveDuringHour.clear();
        allUsageRecordsInOrder.clear();
        processedUsages.clear();
        mostRecentUsage = null;
    }

    private void getRecordsForNextHour() {
        hourToRollup.add(Calendar.HOUR, 1);
        rollupMarker.add(Calendar.HOUR, 1);
        allUsageRecordsInOrder = loadBalancerMergedHostUsageRepository.getAllUsageRecordsInOrderBeforeOrEqualToTime(rollupMarker);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/edgecases/case001.xml")
    public void edgeCasesCase001() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertTrue(processedUsages.isEmpty());
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/edgecases/case002.xml")
    public void edgeCasesCase002() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:58:59", "2013-04-10 21:00:00",
                1, 1, 0, CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/edgecases/case003.xml")
    public void edgeCasesCase003() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(2, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 50l, 100l, 100l, 1.0, 1.0, "2013-04-10 20:00:00", "2013-04-10 20:00:01",
                1, 1, 5, null, 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 600l, 0l, 1200l, 0l, 1.0, 0.0, "2013-04-10 20:00:01", "2013-04-10 21:00:00",
                12, 1, 0, SSL_OFF.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/edgecases/case004.xml")
    public void edgeCasesCase004() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(2, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 0l, 100l, 0l, 0.667, 0.0, "2013-04-10 20:23:59", "2013-04-10 20:25:00",
                3, 1, 0, CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 50l, 100l, 100l, 1.0, 1.0, "2013-04-10 20:25:00", "2013-04-10 21:00:00",
                1, 1, 5, SSL_MIXED_ON.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/edgecases/case005.xml")
    public void edgeCasesCase005() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(2, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 0l, 100l, 0l, 0.5, 0.0, "2013-04-10 20:23:59", "2013-04-10 20:25:00",
                2, 1, 0, CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 100l, 50l, 200l, 100l, 1.0, 0.5, "2013-04-10 20:25:00", "2013-04-10 21:00:00",
                2, 1, 5, SSL_MIXED_ON.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/edgecases/case006.xml")
    public void edgeCasesCase006() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(3, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 50l, 100l, 100l, 1.0, 1.0, "2013-04-10 20:00:00", "2013-04-10 20:03:00",
                1, 1, 5, null, 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:03:00", "2013-04-10 20:33:00",
                0, 1, 5, SUSPEND_LOADBALANCER.name(), 0, true, null, actualUsage);
        actualUsage = processedUsages.get(2);
        AssertUsage.hasValues(null, 1234, 1234, 250l, 250l, 500l, 500l, 1.0, 1.0, "2013-04-10 20:33:00", "2013-04-10 21:00:00",
                5, 1, 5, UNSUSPEND_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Ignore // We decided that this extreme case is not worth the code complexity. Leaving here just in case.
    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/edgecases/case007.xml")
    public void edgeCasesCase007() throws Exception {
        mostRecentUsage.setEventType(SUSPENDED_LOADBALANCER.name());
        mostRecentUsage.setStartTime(CalendarUtils.stringToCalendar("2013-04-10 18:00:00"));
        mostRecentUsage.setEndTime(CalendarUtils.stringToCalendar("2013-04-10 19:00:00"));

        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(2, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 19:00:00", "2013-04-10 20:00:00",
                0, 1, 5, SUSPENDED_LOADBALANCER.name(), 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:00:00", "2013-04-10 21:00:00",
                0, 1, 5, SUSPENDED_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/edgecases/case008.xml")
    public void edgeCasesCase008() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 350l, 0l, 700l, 0l, 0.875, 0.0, "2013-04-10 20:23:59", "2013-04-10 21:00:00",
                8, 1, 0, CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/edgecases/case009.xml")
    public void edgeCasesCase009() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(3, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);

        AssertUsage.hasValues(null, 1234, 1234, 51L, 0L, 101L, 0L, 0.666, 0.0, "2013-04-10 20:25:00", "2013-04-10 20:30:00",
                3, 1, 0, CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);

        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 102L, 102L, 202L, 202L, 1.0, 1.0, "2013-04-10 20:30:00", "2013-04-10 20:40:00",
                4, 1, 5, SSL_MIXED_ON.name(), 0, true, null, actualUsage);

        actualUsage = processedUsages.get(2);
        AssertUsage.hasValues(null, 1234, 1234, 150L, 0L, 300L, 0L, 1.0, 0.0, "2013-04-10 20:40:00", "2013-04-10 21:00:00",
                3, 1, 0, SSL_OFF.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/missingprevioususage/case001.xml")
    public void missingPreviousUsageCase001() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 550l, 550l, 1100l, 1100l, 1.0, 1.0, "2013-04-10 20:00:00", "2013-04-10 21:00:00",
                11, 1, 5, null, 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/missingprevioususage/case002.xml")
    public void missingPreviousUsageCase002() throws Exception {
        mostRecentUsage.setTags(5);
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(2, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 1.0, 1.0, "2013-04-10 20:00:00", "2013-04-10 20:00:01",
                1, 1, 5, null, 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 600l, 0l, 1200l, 0l, 1.0, 0.0, "2013-04-10 20:00:01", "2013-04-10 21:00:00",
                12, 1, 0, SSL_OFF.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/missingprevioususage/case003.xml")
    public void missingPreviousUsageCase003() throws Exception {
        mostRecentUsage.setTags(5);
        mostRecentUsage.setNumVips(2);
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(2, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:00:00", "2013-04-10 20:01:00",
                1, 2, 5, null, 0, true, null, actualUsage);

        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:01:00", "2013-04-10 20:01:00",
                0, 0, 0, DELETE_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/multipleevents/case001.xml")
    public void multipleEventsCase001() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(5, processedUsages.size());

        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 0l, 100l, 0l, 0.5, 0.0, "2013-04-10 20:23:59", "2013-04-10 20:24:21",
                2, 1, 0, CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);

        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 50l, 100l, 100l, 1.0, 1.0, "2013-04-10 20:24:21", "2013-04-10 20:24:59",
                1, 1, 5, SSL_MIXED_ON.name(), 0, true, null, actualUsage);

        actualUsage = processedUsages.get(2);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 50l, 0l, 100l, 0.0, 1.0, "2013-04-10 20:24:59", "2013-04-10 20:25:21",
                1, 1, 1, SSL_ONLY_ON.name(), 0, true, null, actualUsage);

        actualUsage = processedUsages.get(3);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 0l, 100l, 0l, 1.0, 0.0, "2013-04-10 20:25:21", "2013-04-10 20:25:59",
                1, 1, 0, SSL_OFF.name(), 0, true, null, actualUsage);

        actualUsage = processedUsages.get(4);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:25:59", "2013-04-10 20:25:59",
                0, 0, 0, DELETE_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/normalusage/case001.xml")
    public void normalUsageCase001() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 350l, 0l, 700l, 0l, 0.875, 0.0, "2013-04-10 20:23:59", "2013-04-10 21:00:00",
                8, 1, 0, CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/normalusage/case002.xml")
    public void normalUsageCase002() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 400l, 0l, 800l, 0l, 0.889, 0.0, "2013-04-10 20:23:59", "2013-04-10 21:00:00",
                9, 1, 0, CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);

        getRecordsForNextHour();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(1, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 0l, 100l, 0l, 1.0, 0.0, "2013-04-10 21:00:00", "2013-04-10 22:00:00",
                1, 1, 0, null, 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/normalusage/case003.xml")
    public void normalUsageCase003() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(2, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 0l, 100l, 0l, 0.5, 0.0, "2013-04-10 20:23:59", "2013-04-10 20:24:59",
                2, 1, 0, CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 350l, 350l, 700l, 700l, 1.0, 1.0, "2013-04-10 20:24:59", "2013-04-10 21:00:00",
                7, 1, 5, SSL_MIXED_ON.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/normalusage/case004.xml")
    public void normalUsageCase004() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(2, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 200l, 0l, 400l, 0l, 0.8, 0.0, "2013-04-10 20:23:59", "2013-04-10 20:39:59",
                5, 1, 0, CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 200l, 200l, 400l, 400l, 1.0, 1.0, "2013-04-10 20:39:59", "2013-04-10 21:00:00",
                4, 1, 5, SSL_MIXED_ON.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/normalusage/case005.xml")
    public void normalUsageCase005() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 600l, 600l, 1200l, 1200l, 1.0, 1.0, "2013-04-10 20:00:00", "2013-04-10 21:00:00",
                12, 1, 5, null, 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/normalusage/case006.xml")
    public void normalUsageCase006() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(2, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 50l, 100l, 100l, 0.0, 0.0, "2013-04-10 20:00:00", "2013-04-10 20:01:00",
                1, 1, 5, null, 0, true, null, actualUsage);

        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:01:00", "2013-04-10 20:01:00",
                0, 0, 0, DELETE_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/normalusage/case007.xml")
    public void normalUsageCase007() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(2, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 400l, 400l, 800l, 800l, 1.0, 1.0, "2013-04-10 20:00:00", "2013-04-10 20:33:00",
                8, 1, 5, null, 0, true, null, actualUsage);

        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:33:00", "2013-04-10 21:00:00",
                0, 1, 5, SUSPEND_LOADBALANCER.name(), 0, true, null, actualUsage);

        getRecordsForNextHour();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(1, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 21:00:00", "2013-04-10 22:00:00",
                0, 1, 5, SUSPENDED_LOADBALANCER.name(), 0, true, null, actualUsage);

        getRecordsForNextHour();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(2, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 22:00:00", "2013-04-10 22:33:00",
                0, 1, 5, SUSPENDED_LOADBALANCER.name(), 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 250l, 250l, 500l, 500l, 1.0, 1.0, "2013-04-10 22:33:00", "2013-04-10 23:00:00",
                5, 1, 5, UNSUSPEND_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/normalusage/case008.xml")
    public void normalUsageCase008() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:00:00", "2013-04-10 21:00:00",
                1, 1, 0, null, 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/normalusage/case009.xml")
    public void normalUsageCase009() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(0, processedUsages.size());
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/pollergoesdown/case001.xml")
    public void pollerGoesDownCase001() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 350l, 0l, 700l, 0l, 1.0, 0.0, "2013-04-10 20:00:00", "2013-04-10 21:00:00",
                7, 1, 5, null, 0, true, null, actualUsage);

        getRecordsForNextHour();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(1, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 21:00:00", "2013-04-10 22:00:00",
                0, 1, 5, null, 0, true, null, actualUsage);

        getRecordsForNextHour();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(1, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 200l, 0l, 400l, 0l, 1.0, 0.0, "2013-04-10 22:00:00", "2013-04-10 23:00:00",
                5, 1, 5, null, 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/pollergoesdown/case002.xml")
    public void pollerGoesDownCase002() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 350l, 0l, 700l, 0l, 1.0, 0.0, "2013-04-10 20:00:00", "2013-04-10 21:00:00",
                7, 1, 5, null, 0, true, null, actualUsage);

        getRecordsForNextHour();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(1, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 21:00:00", "2013-04-10 22:00:00",
                0, 1, 5, null, 0, true, null, actualUsage);

        getRecordsForNextHour();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(1, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 22:00:00", "2013-04-10 23:00:00",
                0, 1, 5, null, 0, true, null, actualUsage);

        getRecordsForNextHour();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(1, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 23:00:00", "2013-04-11 00:00:00",
                0, 1, 5, null, 0, true, null, actualUsage);

        getRecordsForNextHour();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(1, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 200l, 0l, 400l, 0l, 1.0, 0.0, "2013-04-11 00:00:00", "2013-04-11 01:00:00",
                5, 1, 5, null, 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/pollergoesdown/case003.xml")
    public void pollerGoesDownCase003() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 0l, 100l, 0l, 0.5, 0.0, "2013-04-10 20:23:59", "2013-04-10 21:00:00",
                2, 1, 0, CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/pollergoesdown/case004.xml")
    public void pollerGoesDownCase004() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(2, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 0l, 100l, 0l, 0.5, 0.0, "2013-04-10 20:23:59", "2013-04-10 20:24:59",
                2, 1, 0, CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:24:59", "2013-04-10 21:00:00",
                0, 1, 5, SSL_MIXED_ON.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/pollergoesdown/case005.xml")
    public void pollerGoesDownCase005() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 350l, 350l, 700l, 700l, 1.0, 1.0, "2013-04-10 20:00:00", "2013-04-10 21:00:00",
                7, 1, 5, null, 0, true, null, actualUsage);

        getRecordsForNextHour();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(2, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 50l, 100l, 100l, 1.0, 1.0, "2013-04-10 21:00:00", "2013-04-10 21:03:35",
                1, 1, 5, null, 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 21:03:35", "2013-04-10 22:00:00",
                1, 1, 5, SUSPEND_LOADBALANCER.name(), 0, true, null, actualUsage);

        getRecordsForNextHour();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(1, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 22:00:00", "2013-04-10 23:00:00",
                0, 1, 5, SUSPENDED_LOADBALANCER.name(), 0, true, null, actualUsage);

        getRecordsForNextHour();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(2, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 23:00:00", "2013-04-10 23:35:05",
                0, 1, 5, SUSPENDED_LOADBALANCER.name(), 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 23:35:05", "2013-04-11 00:00:00",
                0, 1, 5, UNSUSPEND_LOADBALANCER.name(), 0, true, null, actualUsage);

        getRecordsForNextHour();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(1, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-11 00:00:00", "2013-04-11 01:00:00",
                0, 1, 5, null, 0, true, null, actualUsage);

        getRecordsForNextHour();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(1, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 150l, 150l, 300l, 300l, 1.0, 1.0, "2013-04-11 01:00:00", "2013-04-11 02:00:00",
                4, 1, 5, null, 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/pollergoesdown/case006.xml")
    public void pollerGoesDownCase006() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 0l, 100l, 0l, 1.0, 0.0, "2013-04-10 20:00:00", "2013-04-10 21:00:00",
                1, 1, 0, CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);

        getRecordsForNextHour();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(1, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 50l, 100l, 100l, 1.0, 1.0, "2013-04-10 21:00:00", "2013-04-10 22:00:00",
                1, 1, 5, SSL_MIXED_ON.name(), 0, true, null, actualUsage);

        getRecordsForNextHour();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(1, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 0l, 100l, 0l, 0.0, 0.0, "2013-04-10 22:00:00", "2013-04-10 23:00:00",
                1, 1, 0, SSL_OFF.name(), 0, true, null, actualUsage);

        getRecordsForNextHour();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(1, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 23:00:00", "2013-04-10 23:00:00",
                0, 0, 0, DELETE_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/pollergoesdown/case007.xml")
    public void pollerGoesDownCase007() throws Exception {
        LbIdAccountId lbActiveDuringHour = new LbIdAccountId(1234, 1234);
        lbsActiveDuringHour.add(lbActiveDuringHour);
        allUsageRecordsInOrder.clear(); // For some reason dbunit isn't returning an empty table.
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:00:00", "2013-04-10 21:00:00",
                0, 1, 0, null, 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/pollergoesdown/case008.xml")
    public void pollerGoesDownCase008() throws Exception {
        LbIdAccountId lbActiveDuringHour = new LbIdAccountId(1234, 1234);
        lbsActiveDuringHour.add(lbActiveDuringHour);
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:00:00", "2013-04-10 21:00:00",
                0, 1, 0, SUSPENDED_LOADBALANCER.name(), 0, true, null, actualUsage);

        getRecordsForNextHour();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(1, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 21:00:00", "2013-04-10 22:00:00",
                0, 1, 0, SUSPENDED_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/pollergoesdown/case009.xml")
    public void pollerGoesDownCase009() throws Exception {
        LbIdAccountId lbActiveDuringHour = new LbIdAccountId(1234, 1234);
        lbsActiveDuringHour.add(lbActiveDuringHour);
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(2, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:52:59", "2013-04-10 20:53:07",
                2, 1, 0, CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:53:07", "2013-04-10 21:00:00",
                0, 1, 0, SUSPEND_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/vips/case001.xml")
    public void vipsCase001() throws Exception {
        LbIdAccountId lbActiveDuringHour = new LbIdAccountId(1234, 1234);
        lbsActiveDuringHour.add(lbActiveDuringHour);
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(2, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:55:05", "2013-04-10 20:56:05",
                2, 1, 0, CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:56:05", "2013-04-10 21:00:00",
                0, 2, 0, CREATE_VIRTUAL_IP.name(), 0, true, null, actualUsage);

        getRecordsForNextHour();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(1, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 21:00:00", "2013-04-10 22:00:00",
                0, 2, 0, null, 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/rollupjob/vips/case002.xml")
    public void vipsCase002() throws Exception {
        LbIdAccountId lbActiveDuringHour = new LbIdAccountId(1234, 1234);
        lbsActiveDuringHour.add(lbActiveDuringHour);
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);

        Assert.assertEquals(4, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:55:05", "2013-04-10 20:56:05",
                2, 1, 0, CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:56:05", "2013-04-10 20:57:05",
                1, 2, 0, CREATE_VIRTUAL_IP.name(), 0, true, null, actualUsage);
        actualUsage = processedUsages.get(2);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:57:05", "2013-04-10 20:58:05",
                1, 3, 0, CREATE_VIRTUAL_IP.name(), 0, true, null, actualUsage);
        actualUsage = processedUsages.get(3);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:58:05", "2013-04-10 21:00:00",
                0, 2, 0, DELETE_VIRTUAL_IP.name(), 0, true, null, actualUsage);

        getRecordsForNextHour();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToRollup, lbsActiveDuringHour);
        Assert.assertEquals(1, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 21:00:00", "2013-04-10 22:00:00",
                0, 2, 0, null, 0, true, null, actualUsage);
    }
}