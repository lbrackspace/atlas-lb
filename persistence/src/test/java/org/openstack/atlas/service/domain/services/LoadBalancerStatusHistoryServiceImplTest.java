package org.openstack.atlas.service.domain.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerStatusHistoryRepository;
import org.openstack.atlas.service.domain.services.impl.LoadBalancerStatusHistoryServiceImpl;
import java.util.Calendar;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;

@RunWith(Enclosed.class)
public class LoadBalancerStatusHistoryServiceImplTest {

    public static class whenDeletingOldLBStatusHistory {

        LoadBalancerStatusHistoryServiceImpl loadBalancerStatusHistoryService;
        @Mock
        LoadBalancerStatusHistoryRepository loadBalancerStatusHistoryRepository;
        Calendar cal;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            loadBalancerStatusHistoryService = new LoadBalancerStatusHistoryServiceImpl();
            cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -180);
            loadBalancerStatusHistoryService.setLoadBalancerStatusHistoryRepository(loadBalancerStatusHistoryRepository);
        }

        @Test
        public void shouldSendCalendarOlderThan180DaysToLbStatusHistRepository() throws Exception {
            loadBalancerStatusHistoryService.deleteLBStatusHistoryOlderThanSixMonths();
            verify(loadBalancerStatusHistoryRepository, times(1)).deleteStatusHistoryForLBOlderThanSixMonths(cal);
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowEntityNotFound() throws EntityNotFoundException {
            doThrow(EntityNotFoundException.class).when(loadBalancerStatusHistoryRepository).deleteStatusHistoryForLBOlderThanSixMonths(any());
            loadBalancerStatusHistoryService.deleteLBStatusHistoryOlderThanSixMonths();
        }
    }
}
