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

        if (sourceFieldValue instanceof VirtualIp) {
            return IpVersion.IPV4;
        }

        if (sourceFieldValue instanceof IpVersion) {
            return org.openstack.atlas.service.domain.entity.IpVersion.fromDataType((IpVersion) sourceFieldValue);
        }

        // HealthMonitorType -> String
        if (sourceClass == HealthMonitorType.class && destinationClass == String.class) {
            return ((HealthMonitorType) sourceFieldValue).name();
        }

        // String -> HealthMonitorType
        if (sourceClass == String.class && destinationClass ==  HealthMonitorType.class) {
            return HealthMonitorType.valueOf((String)sourceFieldValue);
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}
