package org.openstack.atlas.api.mapper.dozer.converter;

import org.dozer.CustomConverter;
import org.openstack.atlas.service.domain.entity.SessionPersistence;
import org.openstack.atlas.service.domain.exception.NoMappableConstantException;

public class PersistenceConverter implements CustomConverter {
    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class destinationClass, Class sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        if (sourceFieldValue instanceof SessionPersistence) {
            org.openstack.atlas.core.api.v1.SessionPersistence sessionPersistence = new org.openstack.atlas.core.api.v1.SessionPersistence();
            sessionPersistence.setPersistenceType(((SessionPersistence) sourceFieldValue).name());
            return sourceFieldValue == SessionPersistence.NONE ? null : sessionPersistence;
        }

        if (sourceFieldValue instanceof org.openstack.atlas.core.api.v1.SessionPersistence) {
            return SessionPersistence.valueOf(((org.openstack.atlas.core.api.v1.SessionPersistence) sourceFieldValue).getPersistenceType());
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}
