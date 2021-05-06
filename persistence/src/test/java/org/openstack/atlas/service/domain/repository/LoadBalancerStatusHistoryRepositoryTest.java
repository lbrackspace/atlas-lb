package org.openstack.atlas.service.domain.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Calendar;

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


        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -180);

        }

        @Test
        public void shouldSendQueryToDeleteStatusHist() throws EntityNotFoundException {
            when(entityManager.createQuery(anyString())).thenReturn(qry);
            when(qry.setParameter(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(qry);
            loadBalancerStatusHistoryRepository.deleteStatusHistoryForLBOlderThanSixMonths(cal);
            verify(qry, times(1)).executeUpdate();
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowEntityNotFound() throws EntityNotFoundException {
            loadBalancerStatusHistoryRepository.deleteStatusHistoryForLBOlderThanSixMonths(cal);
        }

    }




}
