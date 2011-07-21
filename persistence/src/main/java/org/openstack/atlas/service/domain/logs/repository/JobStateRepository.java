package org.openstack.atlas.service.domain.logs.repository;

import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.service.domain.logs.entities.DateTime;
import org.openstack.atlas.service.domain.logs.entities.JobName;
import org.openstack.atlas.service.domain.logs.entities.JobStateVal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Calendar;
import java.util.List;

@Transactional
public class JobStateRepository {

    final Log LOG = LogFactory.getLog(JobStateRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public JobState addEntry(JobName jobName) {
        return addEntry(jobName, null);
    }

    public JobState addEntry(JobName jobName, String inputPath) {
        JobState s = new JobState();
        s.setState(JobStateVal.CREATED);
        s.setJobName(jobName);
        s.setInputPath(inputPath);
        s.setStartTime(new DateTime().getCalendar());
        s.setEndTime(null);
        entityManager.persist(s);
        return s;
    }

    public List getEntry(JobName jobName, String inputPath) {
        return entityManager.createQuery("SELECT s from JobState s where s.inputPath = :inputPath and s.jobName = :jobName order by s.startTime")
                .setParameter("inputPath", inputPath)
                .setParameter("jobName", jobName).getResultList();
    }

    public List getEntry(JobName jobName) {
        return entityManager.createQuery("SELECT s from JobState s where s.jobName = :jobName order by s.startTime")
                .setParameter("jobName", jobName).getResultList();
    }

    public List getEntryLike(JobName jobName, String inputPath) {
        return entityManager.createQuery("Select s from JobState s where s.inputPath like :inputPath and s.jobName = :jobName order by s.startTime")
                .setParameter("inputPath", inputPath)
                .setParameter("jobName", jobName).getResultList();
    }

    public List getStates(String inputPath) {
        return null;
    }

    public List<JobState> getStatesByStatus(JobStateVal state) {
        return null;
    }

    public List<JobState> getStateBeforeEndTime(JobName name, Calendar endTime) {
        return null;
    }

    public void update(JobState state) {
        entityManager.merge(state);
    }

    public JobState getLastState(JobName name) {
        return null;
    }

    public List<JobState> getStatesByStartTime(JobName name, Calendar startTime) {
        return null;
    }

    public List<JobState> getStatesBeforeStartTime(JobName name, Calendar startTime) {
        return null;
    }

    public List<JobState> getStatesByStartAndEndTime(JobName name, Calendar startTime, Calendar endTime) {
        return null;
    }

    public List<JobState> getStatesBetweenTimes(JobName name, Calendar today, Calendar yesterday, Calendar twoDaysAgo) {
        return null;
    }
}
