package org.openstack.atlas.usagerefactor;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.dbunit.FlatXmlLoader;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.generator.UsagePollerGenerator;
import org.openstack.atlas.usagerefactor.helpers.UsagePollerHelper;
import org.openstack.atlas.usagerefactor.helpers.UsageProcessorResult;
import org.openstack.atlas.usagerefactor.junit.AssertLoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.junit.AssertLoadBalancerMergedHostUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.text.SimpleDateFormat;
import java.util.*;


/*
    To see what each case is testing please refer to their respective xml
    file for more information.
 */
@RunWith(Enclosed.class)
public class UsageProcessorTest {

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:dbunit-context.xml"})
    public static class WhenTestingProcessExistingEvents {

        @Mock
        private UsagePollerHelper usagePollerHelper;
        @InjectMocks
        private UsageProcessor usageProcessor = new UsageProcessor();

        private List<LoadBalancerMergedHostUsage> mergedExistingUsages = new ArrayList<LoadBalancerMergedHostUsage>();

        private List<LoadBalancerMergedHostUsage> mergedCurrentUsages = new ArrayList<LoadBalancerMergedHostUsage>();
        private List<LoadBalancerHostUsage> newLBHostUsages = new ArrayList<LoadBalancerHostUsage>();
        private UsageProcessorResult processorResult;

        private Calendar pollTime;
        String pollTimeStr;

        @Before
        public void standUp() throws Exception {
            pollTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pollTimeStr = sdf.format(pollTime.getTime());
        }

        @Test
        public void shouldAddExistingUsageMergedRecordsAndCurrentUsageMergedRecordsAndReturnNewHostUsages() {
            LoadBalancerMergedHostUsage merged1 = mock(LoadBalancerMergedHostUsage.class);
            LoadBalancerMergedHostUsage merged2 = mock(LoadBalancerMergedHostUsage.class);
            LoadBalancerMergedHostUsage merged3 = mock(LoadBalancerMergedHostUsage.class);
            LoadBalancerMergedHostUsage merged4 = mock(LoadBalancerMergedHostUsage.class);
            mergedExistingUsages.add(merged1);
            mergedExistingUsages.add(merged2);
            mergedExistingUsages.add(merged3);
            mergedExistingUsages.add(merged4);

            LoadBalancerMergedHostUsage merged5 = mock(LoadBalancerMergedHostUsage.class);
            LoadBalancerMergedHostUsage merged6 = mock(LoadBalancerMergedHostUsage.class);
            LoadBalancerMergedHostUsage merged7 = mock(LoadBalancerMergedHostUsage.class);
            LoadBalancerMergedHostUsage merged8 = mock(LoadBalancerMergedHostUsage.class);
            mergedCurrentUsages.add(merged5);
            mergedCurrentUsages.add(merged6);
            mergedCurrentUsages.add(merged7);
            mergedCurrentUsages.add(merged8);

            LoadBalancerHostUsage hostUsage1 = mock(LoadBalancerHostUsage.class);
            LoadBalancerHostUsage hostUsage2 = mock(LoadBalancerHostUsage.class);
            LoadBalancerHostUsage hostUsage3 = mock(LoadBalancerHostUsage.class);
            LoadBalancerHostUsage hostUsage4 = mock(LoadBalancerHostUsage.class);
            newLBHostUsages.add(hostUsage1);
            newLBHostUsages.add(hostUsage2);
            newLBHostUsages.add(hostUsage3);
            newLBHostUsages.add(hostUsage4);

            initMocks(this);
            when(usagePollerHelper.processExistingEvents(anyMap())).thenReturn(mergedExistingUsages);
            processorResult = new UsageProcessorResult(mergedCurrentUsages, newLBHostUsages);
            when(usagePollerHelper.processCurrentUsage(anyMap(), anyMap(), anyCalendar())).thenReturn(processorResult);

            int mergedExistingUsagesSize = mergedExistingUsages.size();
            int mergedCurrentUsagesSize = mergedCurrentUsages.size();
            UsageProcessorResult result = usageProcessor.mergeRecords(null, null, pollTime);
            Assert.assertEquals(mergedExistingUsagesSize + mergedCurrentUsagesSize, result.getMergedUsages().size());
            Assert.assertEquals(newLBHostUsages.size(), result.getLbHostUsages().size());
            Assert.assertEquals(mergedExistingUsages.get(0), result.getMergedUsages().get(0));
            Assert.assertEquals(mergedExistingUsages.get(1), result.getMergedUsages().get(1));
            Assert.assertEquals(mergedExistingUsages.get(2), result.getMergedUsages().get(2));
            Assert.assertEquals(mergedExistingUsages.get(3), result.getMergedUsages().get(3));
            Assert.assertEquals(mergedCurrentUsages.get(0), result.getMergedUsages().get(4));
            Assert.assertEquals(mergedCurrentUsages.get(1), result.getMergedUsages().get(5));
            Assert.assertEquals(mergedCurrentUsages.get(2), result.getMergedUsages().get(6));
            Assert.assertEquals(mergedCurrentUsages.get(3), result.getMergedUsages().get(7));
            Assert.assertEquals(newLBHostUsages.get(0), result.getLbHostUsages().get(0));
            Assert.assertEquals(newLBHostUsages.get(1), result.getLbHostUsages().get(1));
            Assert.assertEquals(newLBHostUsages.get(2), result.getLbHostUsages().get(2));
            Assert.assertEquals(newLBHostUsages.get(3), result.getLbHostUsages().get(3));
        }

        private Calendar anyCalendar(){
            ArgumentMatcher<Calendar> t = new ArgumentMatcher<Calendar>(){

                @Override
                public boolean matches(Object o) {
                    return o instanceof Calendar;
                }
            };
            return argThat(t);
        }

    }

}