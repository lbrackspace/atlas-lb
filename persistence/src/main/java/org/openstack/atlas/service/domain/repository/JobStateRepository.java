package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.util.Constants;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.List;

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

    public List<JobState> getAll(Integer... p) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JobState> criteria = builder.createQuery(JobState.class);
        Root<JobState> jobStateRoot = criteria.from(JobState.class);

        criteria.select(jobStateRoot);
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

    public JobState getByName(JobName jobName) throws EntityNotFoundException {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JobState> criteria = builder.createQuery(JobState.class);
        Root<JobState> jobStateRoot = criteria.from(JobState.class);

        Predicate hasName = builder.equal(jobStateRoot.get(JobState_.jobName), jobName);

        criteria.select(jobStateRoot);
        criteria.where(hasName);

        try {
            return entityManager.createQuery(criteria).getSingleResult();
        } catch (NoResultException e) {
            logAndThrowException();
        }

        return null;
    }

    public List getEntriesLike(JobName jobName, String inputPath) {
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
        entityManager.persist(s);
        return s;
    }

    public void update(JobState jobState) {
        entityManager.merge(jobState);
    }

    public void delete(JobState jobState) {
        entityManager.remove(jobState);
    }

    private JobState logAndThrowException() throws EntityNotFoundException {
        String message = Constants.JobNotFound;
        LOG.debug(message);
        throw new EntityNotFoundException(message);
    }
}
