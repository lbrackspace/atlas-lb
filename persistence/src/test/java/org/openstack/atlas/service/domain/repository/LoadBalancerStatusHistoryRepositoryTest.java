package org.openstack.atlas.service.domain.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatusHistory;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@RunWith(Enclosed.class)
public class LoadBalancerStatusHistoryRepositoryTest {

    public static class whenDeletingLBStatusHistory {

        @Mock
        EntityManager entityManager;
        @Mock
        Query qry;
        @InjectMocks
        LoadBalancerStatusHistoryRepository loadBalancerStatusHistoryRepository;
        Calendar cal;
        List<LoadBalancerStatusHistory> lbshList;
        LoadBalancerStatusHistory lbsh;

        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -180);
            lbshList = new ArrayList<>();
            lbsh = new LoadBalancerStatusHistory();
            lbsh.setId(1);
            lbshList.add(lbsh);
            when(entityManager.createQuery(anyString())).thenReturn(qry);
            when(qry.setParameter(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(qry);
            when(qry.getResultList()).thenReturn(lbshList);
        }

        @Test
        public void shouldSendQueryToDeleteStatusHist() {
            loadBalancerStatusHistoryRepository.batchDeleteStatusHistoryForLBOlderThanSixMonths(cal);
            verify(qry, times(1)).executeUpdate();
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowEntityNotFound() {
            when(qry.executeUpdate()).thenThrow(EntityNotFoundException.class);
            loadBalancerStatusHistoryRepository.batchDeleteStatusHistoryForLBOlderThanSixMonths(cal);
        }

        @Test
        public void shouldSendZeroQueriesToDeleteStatusHist() {
            lbshList.clear();
            loadBalancerStatusHistoryRepository.batchDeleteStatusHistoryForLBOlderThanSixMonths(cal);
            verify(qry, times(0)).executeUpdate();
        }

    }
}
