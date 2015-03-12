package org.openstack.atlas.service.domain.repository;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.openstack.atlas.service.domain.entities.CloudFilesLzo;
import org.openstack.atlas.service.domain.entities.HdfsLzo;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.util.crypto.HashUtil;

@Repository
@Transactional
public class LzoRepository {

    private final Log LOG = LogFactory.getLog(LzoRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public List<HdfsLzo> getAllHdfsLzo() {
        String qStr = "select h from HdfsLzo h";
        List<HdfsLzo> respList = entityManager.createQuery(qStr).getResultList();
        return respList;
    }

    public List<CloudFilesLzo> getAllCloudFilesLzo() {
        String qStr = "select c from CloudFilesLzo c";
        List<CloudFilesLzo> respList = entityManager.createQuery(qStr).getResultList();
        return respList;

    }

    public HdfsLzo getHdfsLzo(int hourKey) throws EntityNotFoundException {
        String qStr = "select h from HdfsLzo h where hourKey = :hourKey";
        List<HdfsLzo> respList = entityManager.createQuery(qStr).setParameter("hourKey", hourKey).getResultList();
        if (respList.size() <= 0) {
            throw new EntityNotFoundException(String.format("no HdfsFile for hour %d found", hourKey));
        }
        return respList.get(0);
    }

    public <T extends Object> T merge(T t) {
        return entityManager.merge(t);
    }

    public void refresh(Object o) {
        entityManager.refresh(o);
    }

    public void remove(Object obj) {
        entityManager.remove(obj);
    }

    public void persist(Object o) {
        entityManager.persist(o);
    }

    public void detach(Object entity) {
        entityManager.detach(entity);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public CloudFilesLzo getCloudFilesLzo(int hourKey, int frag) throws EntityNotFoundException {
        String qStr = "select c from CloudFilesLzo c where c.hourKey = :hourKey and frag = :frag";
        List<CloudFilesLzo> respList = entityManager.createQuery(qStr).setParameter("hourKey", hourKey).setParameter("frag", frag).getResultList();
        if (respList.size() <= 0) {
            throw new EntityNotFoundException(String.format("Could not find cloudFilesLzo entry for hour %d frag %d", hourKey, frag));
        }
        return respList.get(0);
    }

    public List<CloudFilesLzo> getCloudFilesLzo(int hourKey) {
        String qStr = "select c from CloudFilesLzo c where c.hourKey = :hourKey";
        List<CloudFilesLzo> respList = entityManager.createQuery(qStr).setParameter("hourKey", hourKey).getResultList();
        return respList;
    }
}
