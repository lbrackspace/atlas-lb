package org.openstack.atlas.service.domain.deadlock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.springframework.transaction.TransactionSystemException;

import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class DeadLockRetryAspectTest {

    public static class WhenRetryAttemptEncountered{

        @Mock
        ProceedingJoinPoint pjp;
        @Mock
        DeadLockRetry deadLockRetry;

        @InjectMocks
        private DeadLockRetryAspect deadLockRetryAspect;

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);
            deadLockRetryAspect = new DeadLockRetryAspect();
        }

        @After
        public void tearDown() throws Exception {
        }

        @Test
        public void shouldTriggerRollbackRetry() throws Throwable {
            doReturn(1).when(deadLockRetry).retryCount();
            doReturn(new Object[1]).when(pjp).getArgs();
            doThrow(new TransactionSystemException("TSXRollback",
                    new RollbackException("Rollback"))).when(pjp).proceed(anyObject());
            try {
                deadLockRetryAspect.concurrencyRetry(pjp, deadLockRetry);
            } catch (RollbackException ex) {
                // ignore
            }
            Assert.assertEquals(1, deadLockRetryAspect.getConncurrencyRetryCalls());
            verify(pjp, times(1)).getArgs();
        }

        @Test
        public void shouldTriggerRollbackRetryTwice() throws Throwable {
            doReturn(2).when(deadLockRetry).retryCount();
            doReturn(new Object[1]).when(pjp).getArgs();
            doThrow(new TransactionSystemException("TSXRollback",
                    new RollbackException("Rollback"))).when(pjp).proceed(anyObject());
            try {
                deadLockRetryAspect.concurrencyRetry(pjp, deadLockRetry);
            } catch (RollbackException ex) {
                // ignore
            }
            Assert.assertEquals(1, deadLockRetryAspect.getConncurrencyRetryCalls());
            verify(pjp, times(2)).getArgs();

        }

        @Test()
        public void shouldThrowRollbackExceptionRetriesExceeded() throws Throwable {
            doReturn(1).when(deadLockRetry).retryCount();
            doReturn(new Object[1]).when(pjp).getArgs();
            doThrow(new TransactionSystemException("TSXRollback",
                    new RollbackException("Rollback"))).when(pjp).proceed(anyObject());
            try {
                deadLockRetryAspect.concurrencyRetry(pjp, deadLockRetry);
            } catch (RollbackException ex) {
                verify(pjp, times(1)).getArgs();
            }

        }

        @Test(expected = TransactionSystemException.class)
        public void shouldThrowTransactionSystemExceptionNotRollbackException() throws Throwable {
            doReturn(1).when(deadLockRetry).retryCount();
            doReturn(new Object[1]).when(pjp).getArgs();
            // Known error but not caused by expected error...
            doThrow(TransactionSystemException.class).when(pjp).proceed(anyObject());
            deadLockRetryAspect.concurrencyRetry(pjp, deadLockRetry);
        }

        @Test(expected = BadRequestException.class)
        public void shouldThrowExceptionIfRetriesExceeded() throws Throwable {
            doReturn(1).when(deadLockRetry).retryCount();
            doReturn(new Object[1]).when(pjp).getArgs();
            // Unknown error...
            doThrow(BadRequestException.class).when(pjp).proceed(anyObject());
            deadLockRetryAspect.concurrencyRetry(pjp, deadLockRetry);

        }
    }
}
