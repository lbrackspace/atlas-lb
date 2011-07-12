package org.openstack.atlas.service.domain.repository;

public interface IdEntity<T> {
    T getId();

    void setId(T entity);
}
