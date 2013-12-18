package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.util.Constants;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@org.springframework.stereotype.Repository
@Transactional
public class JobStateRepository {

    final Log LOG = LogFactory.getLog(JobStateRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public JobState getById(Integer id) throws EntityNotFoundException {
        JobState jobState = entityManager.find(JobState.class, id);
        if (jobState == null) logAndThrowException();
        return jobState;
    }

    public List<JobState> getAll(Integer offset, Integer limit, Integer marker) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JobState> criteria = builder.createQuery(JobState.class);
        Root<JobState> jobStateRoot = criteria.from(JobState.class);
        int markerToOffset = 0;

        if (marker != null) {
            CriteriaQuery<Integer> criteriaQuery = builder.createQuery(Integer.class);
            jobStateRoot = criteriaQuery.from(JobState.class);
            criteriaQuery.select(jobStateRoot.get(JobState_.id));
            List<Integer> ids = entityManager.createQuery(criteriaQuery).getResultList();
            for (Integer id : ids) {
                if (id.equals(marker)) {
                    break;
                }
                markerToOffset++;
            }
            offset = markerToOffset;
        }

        if (offset == null) {
            offset = 0;
        }

        if (limit == null || limit > 100 || limit == 0) {
            limit = 100;
        }

        criteria.select(jobStateRoot);
        TypedQuery<JobState> query = entityManager.createQuery(criteria);
        query = query.setFirstResult(offset).setMaxResults(limit);

        return query.getResultList();
    }

    public List<JobState> getByState(String state, Integer... p) {
        final JobStateVal jobStateVal;
        try {
             jobStateVal = JobStateVal.valueOf(state);
        } catch(IllegalArgumentException e) {
            return new ArrayList<JobState>();
        }

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JobState> criteria = builder.createQuery(JobState.class);
        Root<JobState> jobStateRoot = criteria.from(JobState.class);

        Predicate hasState = builder.equal(jobStateRoot.get(JobState_.state), jobStateVal);
        criteria.select(jobStateRoot);
        criteria.where(hasState);
        TypedQuery<JobState> query = entityManager.createQuery(criteria);

        if (p.length >= 2) {
            Integer offset = p[0];
            Integer limit = p[1];
            if (offset == null) offset = 0;
            if (limit == null || limit > 100) limit = 100;
            query = query.setFirstResult(offset).setMaxResults(limit);
        }

        return query.getResultList();
    }

    // returns true if a row has THE_ONE_TO_RULE_THEM_ALL on GO
    // also returns true if THE_ONE_TO_RULE_THEM_ALL doesn't exist (Legacy support)
    // In case of multiple rows just return true if any are set to GO.
    public boolean isJobReadyToGo() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JobState> criteria = builder.createQuery(JobState.class);
        Root<JobState> jobStateRoot = criteria.from(JobState.class);

        Predicate hasName = builder.equal(jobStateRoot.get(JobState_.jobName), JobName.THE_ONE_TO_RULE_THEM_ALL);

        criteria.select(jobStateRoot);
        criteria.where(hasName);
        List<JobState> masterJobs = entityManager.createQuery(criteria).getResultList();
        for (JobState masterJobState : masterJobs) { // serieously though there should only be one.
            if (masterJobState.getState().equals(JobStateVal.GO)) {
                return true;
            }
        }
        return false;
    }

    public JobState getByName(JobName jobName) throws EntityNotFoundException {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JobState> criteria = builder.createQuery(JobState.class);
        Root<JobState> jobStateRoot = criteria.from(JobState.class);

        Predicate hasName = builder.equal(jobStateRoot.get(JobState_.jobName), jobName);

        criteria.select(jobStateRoot);
        criteria.where(hasName);

        try {
            return entityManager.createQuery(criteria).getResultList().get(0);
        } catch (NoResultException e) {
            logAndThrowException();
        }

        return null;
    }

    public List<JobState> getEntriesLike(JobName jobName, String inputPath) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JobState> criteria = builder.createQuery(JobState.class);
        Root<JobState> jobStateRoot = criteria.from(JobState.class);

        Predicate hasName = builder.equal(jobStateRoot.get(JobState_.jobName), jobName);
        Predicate likeInputPath = builder.like(jobStateRoot.get(JobState_.inputPath), inputPath);
        Order startTimeOrder = builder.asc(jobStateRoot.get(JobState_.startTime));

        criteria.select(jobStateRoot);
        criteria.where(builder.and(hasName, likeInputPath));
        criteria.orderBy(startTimeOrder);

        return entityManager.createQuery(criteria).getResultList();
    }

    public JobState create(JobName jobName) {
        return create(jobName, null);
    }

    public JobState create(JobName jobName, String inputPath) {
        JobState s = new JobState();
        s.setState(JobStateVal.CREATED);
        s.setJobName(jobName);
        s.setInputPath(inputPath);
        s.setStartTime(Calendar.getInstance());
        entityManager.persist(s);
        return s;
    }

    public void update(JobState jobState) {
        entityManager.merge(jobState);
    }

    public void delete(JobState jobState) {
        entityManager.remove(jobState);
    }

    public void deleteByNamesOlderThanNDays(List<JobName> jobNames, int days) {
        Calendar timestamp = Calendar.getInstance();
        timestamp.add(Calendar.DATE, -days);

        Query query = entityManager.createQuery("DELETE JobState s WHERE s.endTime < :timestamp AND s.jobName IN (:jobNames)")
                .setParameter("timestamp", timestamp, TemporalType.TIMESTAMP)
                .setParameter("jobNames", jobNames);
        int numRowsDeleted = query.executeUpdate();
        LOG.info(String.format("Deleted %d rows with endTime before %s", numRowsDeleted, timestamp.getTime()));
    }

    private JobState logAndThrowException() throws EntityNotFoundException {
        String message = Constants.JobNotFound;
        LOG.debug(message);
        throw new EntityNotFoundException(message);
    }
}
