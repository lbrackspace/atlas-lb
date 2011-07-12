package org.openstack.atlas.api.mapper.dozer.converter;

import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionLogging;
import org.openstack.atlas.service.domain.exceptions.NoMappableConstantException;
import org.dozer.CustomConverter;

public class ConnectionLoggingConverter implements CustomConverter {

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class destinationClass, Class sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        if (sourceFieldValue instanceof ConnectionLogging && destinationClass.equals(Boolean.class)) {
            return ((ConnectionLogging)sourceFieldValue).isEnabled();
        }

        if (sourceFieldValue instanceof Boolean) {
            ConnectionLogging conLog = new ConnectionLogging();
            conLog.setEnabled((Boolean) sourceFieldValue);
            return conLog;
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}