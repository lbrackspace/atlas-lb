package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional
public class GroupRepository {

    final Log LOG = LogFactory.getLog(GroupRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public List<GroupRateLimit> getByAccountId(Integer accountId) throws EntityNotFoundException {

        String sql = "select ag.groupRateLimit from AccountGroup ag where ag.accountId = :accountId";
        Query query = entityManager.createQuery(sql).setParameter("accountId", accountId);
        List<GroupRateLimit> limits = query.getResultList();
        if (limits != null && limits.size() > 0) {
            return limits;
        }
        return getDefaultGroup();
    }

    public List<AccountGroup> getAllAccounts() throws EntityNotFoundException {

        String sql = "from AccountGroup ag ";
        Query query = entityManager.createQuery(sql);
        List<AccountGroup> limits = query.getResultList();
        return limits;
    }

public List<GroupRateLimit> getAssignedGroupsForAccount(Integer accountId) throws EntityNotFoundException {

        String sql = "select ag.groupRateLimit from AccountGroup ag where ag.accountId = :accountId";
        Query query = entityManager.createQuery(sql).setParameter("accountId", accountId);
        List<GroupRateLimit> limits = query.getResultList();
        return limits;
}


        public List<GroupRateLimit> groupHasAssociatedAccounts(Integer groupId) throws EntityNotFoundException {

        String sql = "select ag.groupRateLimit from AccountGroup ag where ag.groupRateLimit.id = :groupId";
        Query query = entityManager.createQuery(sql).setParameter("groupId", groupId);
        List<GroupRateLimit> limits = query.getResultList();
        return limits;
        }



    public List<GroupRateLimit> getDefaultGroup() throws EntityNotFoundException {
        String sql = "from GroupRateLimit l where is_default = 1";
        Query query = entityManager.createQuery(sql);
        List<GroupRateLimit> limits = query.getResultList();
        return limits;
    }

    public GroupRateLimit getByGroupName(String groupName) throws EntityNotFoundException {
        String sql = "from GroupRateLimit l where name = :groupName";
        Query query = entityManager.createQuery(sql).setParameter("groupName", groupName);
        List<GroupRateLimit> limits = query.getResultList();
        if (limits != null && limits.size() > 0) {
            return limits.get(0);
        }
        return null;
    }

        public void delete(GroupRateLimit limit) {
        limit = entityManager.merge(limit);
        entityManager.remove(limit);
    }

     public List<GroupRateLimit> getAll( ) {
        List<GroupRateLimit> groupRateLimits = new ArrayList<GroupRateLimit>();
        Query query = entityManager.createQuery("SELECT h FROM GroupRateLimit h");
        groupRateLimits = query.getResultList();
        return groupRateLimits;
    }

    public GroupRateLimit update(GroupRateLimit limit) {
       LOG.info("Updating GroupRateLimit " + limit.getId() + "...");
       limit = entityManager.merge(limit);
       entityManager.flush();
       return limit;
    }


    public GroupRateLimit getByGroupId(Integer id) throws EntityNotFoundException {
        GroupRateLimit groupRateLimit = entityManager.find(GroupRateLimit.class, id);
        if (groupRateLimit == null) {
            throw new EntityNotFoundException("Object not found");
        }
        return groupRateLimit;
    }


    public void deleteAllForAccount(Integer accountId) {
        Query query = entityManager.createQuery("DELETE AccountGroup u WHERE u.accountId = :id").setParameter("id",
                accountId);
        int numRowsDeleted = query.executeUpdate();

    }

    public void save(AccountGroup accountGroup) {
        entityManager.persist(accountGroup);
    }

    public void save(GroupRateLimit group) {
        entityManager.persist(group);
    }
}
