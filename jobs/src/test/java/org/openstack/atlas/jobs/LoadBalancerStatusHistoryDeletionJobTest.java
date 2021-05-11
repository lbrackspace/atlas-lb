package org.openstack.atlas.jobs;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.service.domain.services.impl.LoadBalancerStatusHistoryServiceImpl;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;


@RunWith(Enclosed.class)
public class LoadBalancerStatusHistoryDeletionJobTest {

    public static class whenDeletingStatusHistory {

        @Mock
        LoadBalancerStatusHistoryServiceImpl loadBalancerStatusHistoryService;
        @InjectMocks
        LoadBalancerStatusHistoryDeletionJob loadBalancerStatusHistoryDeletionJob;

        @Before
        public void standUp() {
            MockitoAnnotations.initMocks(this);
        }

        @Test
        public void shouldCallLBStatusHistoryService() throws Exception {
            loadBalancerStatusHistoryDeletionJob.run();
            verify(loadBalancerStatusHistoryService, times(1)).deleteLBStatusHistoryOlderThanSixMonths();
        }
        @Test(expected = NullPointerException.class)
        public void shouldThrowEntityNotFoundException() throws Exception {
            doThrow(new NullPointerException()).when(loadBalancerStatusHistoryService).deleteLBStatusHistoryOlderThanSixMonths();
            loadBalancerStatusHistoryDeletionJob.run();
        }
    }
}
