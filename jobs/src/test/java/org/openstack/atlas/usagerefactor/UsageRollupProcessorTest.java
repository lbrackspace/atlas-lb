package org.openstack.atlas.usagerefactor;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@Ignore
@RunWith(Enclosed.class)
public class UsageRollupProcessorTest {

    public static class OneHourOfPolledUsageWithNoEvents {

        @Mock
        private PolledUsageRepository polledUsageRepository;
        @InjectMocks
        private UsageRollupProcessor usageRollupProcessor;

        @Before
        public void standUp() {

        }

        @Test
        public void shouldCreateOneHourlyRecord() {

        }
    }
}
