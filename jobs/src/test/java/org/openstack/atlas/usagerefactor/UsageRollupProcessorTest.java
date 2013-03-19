package org.openstack.atlas.usagerefactor;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.entities.Usage;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@Ignore
@RunWith(Enclosed.class)
public class UsageRollupProcessorTest {

    @RunWith(MockitoJUnitRunner.class)
    public static class OneHourOfPolledUsageWithNoEvents {

        @Mock
        private PolledUsageRepository polledUsageRepository;
        private UsageRollupProcessorImpl usageRollupProcessor;
        private List<PolledUsageRecord> polledRecords;

        @Before
        public void standUp() {
            usageRollupProcessor = new UsageRollupProcessorImpl();
            polledRecords = new ArrayList<PolledUsageRecord>();
            when(polledUsageRepository.getAllRecords()).thenReturn(polledRecords);
        }

        @Test
        public void shouldCreateOneHourlyRecord() {
            List<PolledUsageRecord> allRecords = polledUsageRepository.getAllRecords();
            List<Usage> processedUsages = usageRollupProcessor.processRecords(allRecords);
            Assert.assertEquals(1, processedUsages.size());
        }
    }
}
