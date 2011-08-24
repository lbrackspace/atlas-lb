package org.openstack.atlas.api.mapper.dozer.converter;

import org.openstack.atlas.service.domain.exceptions.NoMappableConstantException;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.dozer.CustomConverter;

public class SslTagConverter implements CustomConverter {

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class destinationClass, Class sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        if(sourceFieldValue instanceof Integer) {
            return ((Integer) sourceFieldValue & BitTag.SSL.tagValue()) == 1;
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}
