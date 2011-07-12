package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.lang.reflect.ParameterizedType;
import java.util.List;

public class JpaRepository<K, E> implements Repository<K, E> {
    final Log LOG = LogFactory.getLog(JpaRepository.class);

    protected Class<E> entityClass;

    protected EntityManager entityManager;

    public JpaRepository() {
        ParameterizedType genericSuperClass = (ParameterizedType) getClass().getGenericSuperclass();
        this.entityClass = (Class<E>) genericSuperClass.getActualTypeArguments()[1];
    }

    @Override
    public void save(E entity) {
        entityManager.persist(entity);
    }

    @Override
    public void remove(E entity) {
        entityManager.remove(entity);
    }

    @Override
    public List<E> getAll(Integer... p) {
        Query query = entityManager.createQuery("from " + entityClass.getName());
        if (p.length >= 2) {
            Integer offset = p[0];
            Integer limit = p[1];
            if (offset == null) {
                offset = 0;
            }
            if (limit == null || limit > 100) {
                limit = 100;
            }
            query = query.setFirstResult(offset).setMaxResults(limit);
        }
        return (List<E>) query.getResultList();
    }

    @Override
    public E getById(K id) throws EntityNotFoundException {
        E entity = entityManager.find(entityClass, id);
        if (entity == null) {
            String errMsg = String.format("Cannot access " + entityClass.getName() + " {id=%d}", id);
            LOG.warn(errMsg);
            throw new EntityNotFoundException(errMsg);
        }
        return entity;
    }
}
