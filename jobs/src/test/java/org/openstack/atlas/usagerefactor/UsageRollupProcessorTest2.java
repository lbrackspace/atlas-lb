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
        AssertUsage.hasValues(null, 1234, 1234, 0l, 0l, 0l, 0l, 0.0, 0.0, "2013-04-10 20:23:59", "2013-04-10 21:00:00",
                2, 1, 0, UsageEvent.CREATE_LOADBALANCER.name(), 0, true, null, actualUsage);
    }

}