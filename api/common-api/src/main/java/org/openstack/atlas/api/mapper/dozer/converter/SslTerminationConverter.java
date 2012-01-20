package org.openstack.atlas.api.mapper.dozer.converter;

import org.dozer.CustomConverter;
import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionLogging;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.service.domain.exceptions.NoMappableConstantException;

public class SslTerminationConverter implements CustomConverter {

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class destinationClass, Class sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        if (sourceFieldValue instanceof SslTermination && destinationClass.equals(Boolean.class)) {
            return ((SslTermination)sourceFieldValue).isEnabled();
        }

        if (sourceFieldValue instanceof SslTermination && destinationClass.equals(Boolean.class)) {
            return ((SslTermination)sourceFieldValue).isSecureTrafficOnly();
        }


        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}