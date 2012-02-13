package org.openstack.atlas.api.mapper.dozer.converter;

import org.dozer.CustomConverter;
import org.openstack.atlas.service.domain.exceptions.NoMappableConstantException;

import java.util.Collection;

public class CollectionCountConverter implements CustomConverter {

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class destinationClass, Class sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        if (sourceFieldValue instanceof Collection && destinationClass == Integer.class) {
            return ((Collection) sourceFieldValue).size();
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}
