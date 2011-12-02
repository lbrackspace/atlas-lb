package org.openstack.atlas.rax.api.mapper.dozer.converter;

import org.dozer.CustomConverter;
import org.openstack.atlas.api.v1.extensions.rax.NetworkItemType;
import org.openstack.atlas.core.api.v1.IpVersion;
import org.openstack.atlas.rax.domain.entity.RaxAccessListType;
import org.openstack.atlas.service.domain.exception.NoMappableConstantException;

public class EnumConverter implements CustomConverter {

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class destinationClass, Class sourceClass) {
        if (sourceFieldValue instanceof RaxAccessListType) {
            return ((RaxAccessListType) sourceFieldValue).getDataType();
        }

        if (sourceFieldValue instanceof NetworkItemType) {
            return RaxAccessListType.fromDataType((NetworkItemType) sourceFieldValue);
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }

}
