package org.openstack.atlas.api.mapper.dozer.converter;

import org.dozer.CustomConverter;
import org.openstack.atlas.common.ip.exception.IPStringConversionException1;
import org.openstack.atlas.core.api.v1.IpVersion;
import org.openstack.atlas.core.api.v1.VipType;
import org.openstack.atlas.service.domain.entity.VirtualIpv6;
import org.openstack.atlas.service.domain.exception.NoMappableConstantException;

public class Vip6Converter implements CustomConverter {

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class destinationClass, Class sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        if (sourceFieldValue instanceof VirtualIpv6 && destinationClass == java.lang.String.class) {
            try {
                return ((VirtualIpv6) sourceFieldValue).getDerivedIpString();
            } catch (IPStringConversionException1 ipStringConversionException1) {
                // TODO: Handle properly
                ipStringConversionException1.printStackTrace();
            }
        }

        if (sourceFieldValue instanceof VirtualIpv6 && destinationClass == IpVersion.class) {
            return IpVersion.IPV6;
        }

        if (sourceFieldValue instanceof VirtualIpv6 && destinationClass == VipType.class) {
            return VipType.PUBLIC;
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}
