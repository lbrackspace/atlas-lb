package org.openstack.atlas.api.mapper.dozer.converter;

import org.dozer.CustomConverter;
import org.openstack.atlas.core.api.v1.IpVersion;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.exception.NoMappableConstantException;

public class Vip4Converter implements CustomConverter {

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class destinationClass, Class sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        if (sourceFieldValue instanceof VirtualIp && destinationClass == IpVersion.class) {
            return IpVersion.IPV4;
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}
