package org.openstack.atlas.api.mapper.dozer.converter;

import org.openstack.atlas.service.domain.events.entities.SslMode;
import org.openstack.atlas.service.domain.exceptions.NoMappableConstantException;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.dozer.CustomConverter;

public class SslModeConverter implements CustomConverter {

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class destinationClass, Class sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        if(sourceFieldValue instanceof Integer) {
            Integer tagsBitMask = (Integer) sourceFieldValue;
            BitTags bitTags = new BitTags(tagsBitMask);
            SslMode.getMode(bitTags).name();
            return SslMode.getMode(bitTags).name();
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}
