package org.openstack.atlas.api.mapper.dozer.converter;

import org.dozer.CustomConverter;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.service.domain.exceptions.NoMappableConstantException;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;

public class VipTypeTagConverter implements CustomConverter {

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class destinationClass, Class sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        if (sourceFieldValue instanceof Integer) {
            if (BitTags.isTagOn((Integer) sourceFieldValue, BitTag.SERVICENET_LB)) return VipType.SERVICENET;
            else return VipType.PUBLIC;
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}