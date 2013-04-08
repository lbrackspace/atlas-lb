package org.openstack.atlas.usagerefactor;

import org.junit.Before;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.generator.GeneratorPojo;
import org.openstack.atlas.usagerefactor.generator.PolledUsageRecordGenerator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class UsagePollerTest {

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenLBHostUsageTableIsEmpty {

        private int accountId = 5806065;
        private int lbId = 1234;

        private List<LoadBalancerHostUsage> lbHostUsages;
        private UsagePoller usagePoller;
        private Calendar initialPollTime;
        private Calendar hourToProcess;

        @Before
        public void standUp() {
            usagePoller = new UsagePollerImpl();
            initialPollTime = new GregorianCalendar(2013, Calendar.MARCH, 20, 10, 0, 0);
            //when(usagePoller.getLoadBalancerHostUsageRecords()).thenReturn(new Map<Integer, LoadBalancerHostUsage>());
        }

    }
}
