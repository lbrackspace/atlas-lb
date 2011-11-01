package org.openstack.atlas.api.mapper.dozer.converter;

import org.dozer.CustomConverter;
import org.openstack.atlas.core.api.v1.IpVersion;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.NoMappableConstantException;

public class EnumConverter implements CustomConverter {

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class destinationClass, Class sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        if (sourceFieldValue instanceof IpVersion && destinationClass == org.openstack.atlas.service.domain.entity.IpVersion.class) {
            return org.openstack.atlas.service.domain.entity.IpVersion.fromDataType((IpVersion) sourceFieldValue);
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}
