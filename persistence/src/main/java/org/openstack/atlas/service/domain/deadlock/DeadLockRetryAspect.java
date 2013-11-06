package org.openstack.atlas.service.domain.deadlock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.JDBCException;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.engine.SessionFactoryImplementor;
import org.openstack.atlas.service.domain.util.DeepCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

/**
 * This Aspect will cause methods to retry if there is a notion of a deadlock.
 * <p/>
 * <emf>Note that the aspect implements the Ordered interface so we can set the
 * precedence of the aspect higher than the transaction advice (we want a fresh
 * transaction each time we retry). Also note that all aspects are singletons.
 * </emf>
 */
@Aspect
@Component
public class DeadLockRetryAspect implements Ordered {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeadLockRetryAspect.class);
    private int order = 99; // Transaction manager order should be set to 100
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    private int conncurrencyRetryCalls = 0;

    public int getConncurrencyRetryCalls() {
        return conncurrencyRetryCalls;
    }

    /**
     * Deadlock retry. The aspect applies to every service method with the
     * annotation {@link DeadLockRetry}
     *
     * @param pjp           the joinpoint
     * @param deadLockRetry the concurrency retry
     * @return
     * @throws Throwable the throwable
     */
    @Around(value = "@annotation(deadLockRetry)", argNames = "pjp,deadLockRetry")
    public Object concurrencyRetry(final ProceedingJoinPoint pjp, final DeadLockRetry deadLockRetry) throws Throwable {
        conncurrencyRetryCalls++;

        final Integer retryCount = deadLockRetry.retryCount();
        Integer deadlockCounter = 0;
        Object result = null;

        while (deadlockCounter < retryCount) {
            try {
                Object[] argsCopy = copyArgs(pjp.getArgs()); // copy original args so side effects aren't introduced
                result = pjp.proceed(argsCopy);
                break;
            } catch (final JpaSystemException exception) {
                if (exception.getCause() instanceof PersistenceException) {
                    deadlockCounter = handleException((PersistenceException) exception.getCause(), deadlockCounter, retryCount);
                } else {
                    throw exception;
                }
            } catch (final PersistenceException pe) {
                deadlockCounter = handleException(pe, deadlockCounter, retryCount);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw e;
            }
        }

        return result;
    }

    private Object[] copyArgs(Object[] args) {
        Object[] copy = new Object[args.length];

        for (int i = 0; i < args.length; i++) {
            copy[i] = DeepCopy.copy(args[i]);
        }

        return copy;
    }

    /**
     * handles the persistence exception. Performs checks to see if the
     * exception is a deadlock and check the retry count.
     *
     * @param exception       the persistence exception that could be a deadlock
     * @param deadlockCounter the counter of occured deadlocks
     * @param retryCount      the max retry count
     * @return the deadlockCounter that is incremented
     */
    private Integer handleException(final PersistenceException exception, Integer deadlockCounter, final Integer retryCount) {
        if (isDeadlock(exception)) {
            deadlockCounter++;
            LOGGER.error("Deadlocked ", exception.getMessage());
            if (deadlockCounter == (retryCount - 1)) {
                throw exception;
            }
        } else {
            throw exception;
        }
        return deadlockCounter;
    }

    /**
     * check if the exception is a deadlock error.
     *
     * @param exception the persitence error
     * @return is a deadlock error
     */
    private Boolean isDeadlock(final PersistenceException exception) {
        Boolean isDeadlock = Boolean.FALSE;
        final Dialect dialect = getDialect();
        if (dialect instanceof ErrorCodeAware && exception.getCause() instanceof JDBCException) {
            if (((ErrorCodeAware) dialect).getDeadlockErrorCodes().contains(getSQLErrorCode(exception))) {
                isDeadlock = Boolean.TRUE;
            }
        }
        return isDeadlock;
    }

    /**
     * Returns the currently used dialect
     *
     * @return the dialect
     */
    private Dialect getDialect() {
        final SessionFactory sessionFactory = ((HibernateEntityManagerFactory) entityManager.getEntityManagerFactory()).getSessionFactory();
        Dialect dialect = ((SessionFactoryImplementor) sessionFactory).getDialect();

        if (dialect instanceof org.hibernate.dialect.MySQL5InnoDBDialect) return new MySQL5InnoDBDialect();
        // Add custom dialect conditionals here.
        return dialect;
    }

    /**
     * extracts the low level sql error code from the
     * {@link PersistenceException}
     *
     * @param exception the persistence exception
     * @return the low level sql error code
     */
    private int getSQLErrorCode(final PersistenceException exception) {
        return ((JDBCException) exception.getCause()).getSQLException().getErrorCode();
    }

    /**
     * {@inheritDoc}
     */
    public int getOrder() {
        return order;
    }

    /**
     * Sets the order.
     *
     * @param order the order to set
     */
    public void setOrder(final int order) {
        this.order = order;
    }
}