package org.openstack.atlas.usagerefactor;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.openstack.atlas.dbunit.FlatXmlLoader;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerMergedHostUsageRepository;
import org.openstack.atlas.usagerefactor.junit.AssertUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/*
    To see what each case is testing please refer to their respective xml
    file for more information.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:context.xml"})
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

    private Calendar hourToProcess;
    private List<LoadBalancerMergedHostUsage> allUsageRecordsInOrder;
    private List<Usage> processedUsages;
    private Usage mostRecentUsage;

    @Before
    public void standUp() throws EntityNotFoundException {
        initMocks(this);
        mostRecentUsage = new Usage();
        when(usageRepository.getMostRecentUsageForLoadBalancer(Matchers.<Integer>any())).thenReturn(mostRecentUsage);

        hourToProcess = new GregorianCalendar(2013, Calendar.APRIL, 10, 20, 0, 0);
        allUsageRecordsInOrder = loadBalancerMergedHostUsageRepository.getAllUsageRecordsInOrder();
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/case001.xml")
    public void case1() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToProcess);

        Assert.assertTrue(processedUsages.isEmpty());
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/case002.xml")
    public void case2() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToProcess);

        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:58:59", "2013-04-10 21:00:00",
                1, 1, 0, UsageEvent.CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/case003.xml")
    public void case3() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToProcess);

        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 0l, 100l, 0l, 0.5, 0.0, "2013-04-10 20:23:59", "2013-04-10 21:00:00",
                2, 1, 0, UsageEvent.CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/case004.xml")
    public void case4() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToProcess);

        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 350l, 0l, 700l, 0l, 0.875, 0.0, "2013-04-10 20:23:59", "2013-04-10 21:00:00",
                8, 1, 0, UsageEvent.CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/case005.xml")
    public void case5() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToProcess);

        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 350l, 0l, 700l, 0l, 0.875, 0.0, "2013-04-10 20:23:59", "2013-04-10 21:00:00",
                8, 1, 0, UsageEvent.CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/case006.xml")
    public void case6() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToProcess);

        Assert.assertEquals(2, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 0l, 100l, 0l, 0.5, 0.0, "2013-04-10 20:23:59", "2013-04-10 20:24:59",
                2, 1, 0, UsageEvent.CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:24:59", "2013-04-10 21:00:00",
                0, 1, 5, UsageEvent.SSL_MIXED_ON.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/case007.xml")
    public void case7() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToProcess);

        Assert.assertEquals(2, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 0l, 100l, 0l, 0.5, 0.0, "2013-04-10 20:23:59", "2013-04-10 20:24:59",
                2, 1, 0, UsageEvent.CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 350l, 350l, 700l, 700l, 1.0, 1.0, "2013-04-10 20:24:59", "2013-04-10 21:00:00",
                7, 1, 5, UsageEvent.SSL_MIXED_ON.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/case008.xml")
    public void case8() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToProcess);

        Assert.assertEquals(2, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 200l, 0l, 400l, 0l, 0.8, 0.0, "2013-04-10 20:23:59", "2013-04-10 20:39:59",
                5, 1, 0, UsageEvent.CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 200l, 200l, 400l, 400l, 1.0, 1.0, "2013-04-10 20:39:59", "2013-04-10 21:00:00",
                4, 1, 5, UsageEvent.SSL_MIXED_ON.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/case009.xml")
    public void case9() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToProcess);

        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 600l, 600l, 1200l, 1200l, 1.0, 1.0, "2013-04-10 20:00:00", "2013-04-10 21:00:00",
                12, 1, 5, null, 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/case010.xml")
    public void case10() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToProcess);

        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 550l, 550l, 1100l, 1100l, 1.0, 1.0, "2013-04-10 20:00:00", "2013-04-10 21:00:00",
                12, 1, 5, null, 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/case011.xml")
    public void case11() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToProcess);

        Assert.assertEquals(2, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 50l, 100l, 100l, 1.0, 1.0, "2013-04-10 20:00:00", "2013-04-10 20:00:01",
                1, 1, 5, null, 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 600l, 0l, 1200l, 0l, 1.0, 0.0, "2013-04-10 20:00:01", "2013-04-10 21:00:00",
                12, 1, 0, UsageEvent.SSL_OFF.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/case012.xml")
    public void case12() throws Exception {
        mostRecentUsage.setTags(5);
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToProcess);

        Assert.assertEquals(2, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 1.0, 1.0, "2013-04-10 20:00:00", "2013-04-10 20:00:01",
                1, 1, 5, null, 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 600l, 0l, 1200l, 0l, 1.0, 0.0, "2013-04-10 20:00:01", "2013-04-10 21:00:00",
                12, 1, 0, UsageEvent.SSL_OFF.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/case013.xml")
    public void case13() throws Exception {
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToProcess);
        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 350l, 0l, 700l, 0l, 1.0, 0.0, "2013-04-10 20:00:00", "2013-04-10 21:00:00",
                7, 1, 5, null, 0, true, null, actualUsage);

        hourToProcess.add(Calendar.HOUR, 1);
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToProcess);
        Assert.assertEquals(1, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 21:00:00", "2013-04-10 22:00:00",
                0, 1, 5, null, 0, true, null, actualUsage);

        hourToProcess.add(Calendar.HOUR, 1);
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToProcess);
        Assert.assertEquals(1, processedUsages.size());
        actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 200l, 0l, 400l, 0l, 1.0, 0.0, "2013-04-10 22:00:00", "2013-04-10 23:00:00",
                5, 1, 5, null, 0, true, null, actualUsage);
    }
}