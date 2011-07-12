package org.openstack.atlas.service.domain.logs.repository;

import org.openstack.atlas.service.domain.logs.entities.DateTime;
import org.openstack.atlas.service.domain.logs.entities.NameVal;
import org.openstack.atlas.service.domain.logs.entities.State;
import org.openstack.atlas.service.domain.logs.entities.StateVal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Calendar;
import java.util.List;

@Transactional
public class StateRepository {

    final Log LOG = LogFactory.getLog(StateRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;
    private final Integer NUM_DAYS_RETENTION = 120;

    public State addState(NameVal jobName, String inputPath) {
        State s = new State();
        s.setState(StateVal.CREATED);
        s.setJobName(jobName);
        s.setInputPath(inputPath);
        s.setStartTime(new DateTime().getCalendar());
        entityManager.persist(s);
        return s;
    }

    public List<State> getSlowRuns(Integer threshold) {
        return null;
    }

    public List getState(NameVal jobName, String inputPath) {
        return entityManager.createQuery("SELECT s from State s where s.inputPath = :inputPath and s.jobName = :jobName order by s.startTime")
                .setParameter("inputPath", inputPath)
                .setParameter("jobName", jobName).getResultList();
    }

    public List getState(NameVal jobName) {
        return entityManager.createQuery("SELECT s from State s where s.jobName = :jobName order by s.startTime")
                .setParameter("jobName", jobName).getResultList();
    }

    public List getStateLike(NameVal jobName, String inputPath) {
        return entityManager.createQuery("Select s from State s where s.inputPath like :inputPath and s.jobName = :jobName order by s.startTime")
                .setParameter("inputPath", inputPath)
                .setParameter("jobName", jobName).getResultList();
    }

    public List getStates(String inputPath) {
        return null;
    }

    public List<State> getStatesByStatus(StateVal state) {
        return null;
    }

    public List<State> getStateBeforeEndTime(NameVal name, Calendar endTime) {
        return null;
    }

    public void update(State state) {
        entityManager.merge(state);
    }

    public State getLastState(NameVal name) {
        return null;
    }

    public List<State> getStatesByStartTime(NameVal name, Calendar startTime) {
        return null;
    }

    public List<State> getStatesBeforeStartTime(NameVal name, Calendar startTime) {
        return null;
    }

    public List<State> getStatesByStartAndEndTime(NameVal name, Calendar startTime, Calendar endTime) {
        return null;
    }

    public List<State> getStatesBetweenTimes(NameVal name, Calendar today, Calendar yesterday, Calendar twoDaysAgo) {
        return null;
    }
}
