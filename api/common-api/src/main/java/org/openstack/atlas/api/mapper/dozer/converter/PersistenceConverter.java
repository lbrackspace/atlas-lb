package org.openstack.atlas.api.mapper.dozer.converter;

import org.openstack.atlas.service.domain.entities.SessionPersistence;
import org.openstack.atlas.service.domain.exceptions.NoMappableConstantException;
import org.dozer.CustomConverter;

public class PersistenceConverter implements CustomConverter {
    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class destinationClass, Class sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        if (sourceFieldValue instanceof SessionPersistence) {
            org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence sesionPersistence = ((SessionPersistence) sourceFieldValue).getSesionPersistence();
            return sesionPersistence.getPersistenceType() == null ? null : sesionPersistence;
        }

        if (sourceFieldValue instanceof org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence) {
            return SessionPersistence.fromDataType(((org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence) sourceFieldValue).getPersistenceType());
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}
