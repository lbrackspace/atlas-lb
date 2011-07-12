package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

import java.util.List;

public interface Repository<K, E> {
    void save(E entity);
    void remove(E entity);
    E getById(K id) throws EntityNotFoundException;
    List<E> getAll(Integer... p);
}
