package org.openstack.atlas.usagerefactor;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;
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

/*
    To see what each case is testing please refer to their respective xml
    file for more information.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:context.xml"})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
public class UsageRollupProcessorTest2 {

    @Autowired
    private LoadBalancerMergedHostUsageRepository loadBalancerMergedHostUsageRepository;
    private UsageRollupProcessor usageRollupProcessor;
    private Calendar hourToProcess;
    private List<LoadBalancerMergedHostUsage> allUsageRecordsInOrder;
    private List<Usage> processedUsages;

    @Before
    public void standUp() {
        usageRollupProcessor = new UsageRollupProcessorImpl();
        hourToProcess = new GregorianCalendar(2013, Calendar.APRIL, 10, 20, 0, 0);
        allUsageRecordsInOrder = loadBalancerMergedHostUsageRepository.getAllUsageRecordsInOrder();
        processedUsages = usageRollupProcessor.processRecords(allUsageRecordsInOrder, hourToProcess);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/case1.xml")
    public void case1() throws Exception {
        Assert.assertTrue(processedUsages.isEmpty());
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/case2.xml")
    public void case2() throws Exception {
        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:58:59", "2013-04-10 21:00:00",
                1, 1, 0, UsageEvent.CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/case3.xml")
    public void case3() throws Exception {
        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 0l, 100l, 0l, 0.5, 0.0, "2013-04-10 20:23:59", "2013-04-10 21:00:00",
                2, 1, 0, UsageEvent.CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/case4.xml")
    public void case4() throws Exception {
        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 350l, 0l, 700l, 0l, 0.875, 0.0, "2013-04-10 20:23:59", "2013-04-10 21:00:00",
                8, 1, 0, UsageEvent.CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/case5.xml")
    public void case5() throws Exception {
        Assert.assertEquals(1, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 350l, 0l, 700l, 0l, 0.875, 0.0, "2013-04-10 20:23:59", "2013-04-10 21:00:00",
                8, 1, 0, UsageEvent.CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/case6.xml")
    public void case6() throws Exception {
        Assert.assertEquals(2, processedUsages.size());
        Usage actualUsage = processedUsages.get(0);
        AssertUsage.hasValues(null, 1234, 1234, 50l, 0l, 100l, 0l, 0.5, 0.0, "2013-04-10 20:23:59", "2013-04-10 20:24:59",
                2, 1, 0, UsageEvent.CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
        actualUsage = processedUsages.get(1);
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:24:59", "2013-04-10 21:00:00",
                0, 1, 5, UsageEvent.SSL_MIXED_ON.name(), 0, true, null, actualUsage);
    }

}