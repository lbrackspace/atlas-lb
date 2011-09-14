package org.openstack.atlas.rax.api.mapper.dozer.converter;

import org.dozer.CustomConverter;
import org.openstack.atlas.api.v1.extensions.rax.NetworkItem;
import org.openstack.atlas.rax.domain.entity.AccessList;
import org.openstack.atlas.rax.domain.entity.AccessListType;
import org.openstack.atlas.service.domain.entity.IpVersion;
import org.openstack.atlas.service.domain.exception.NoMappableConstantException;

import java.util.*;

public class AccessListConverter implements CustomConverter {
    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class<?> destinationClass, Class<?> sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        if (destinationClass == List.class) {
            List<Object> anies = (List<Object>) existingDestinationFieldValue;
            if (anies == null) anies = new ArrayList<Object>();

            // TODO: Figure this part out lol

            return anies;
        }

        if (destinationClass == Set.class) {
            Set<org.openstack.atlas.rax.domain.entity.AccessList> accessLists = new HashSet<AccessList>();
            org.openstack.atlas.api.v1.extensions.rax.AccessList _accessList = AnyObjectMapper.getAnyElement((List<Object>) sourceFieldValue, org.openstack.atlas.api.v1.extensions.rax.AccessList.class);

            if(_accessList == null) return null;

            for (NetworkItem networkItem : _accessList.getNetworkItems()) {
                AccessList accessList = new AccessList();
                accessList.setIpAddress(networkItem.getAddress());
                accessList.setIpVersion(IpVersion.valueOf(networkItem.getIpVersion().name()));
                accessList.setType(AccessListType.valueOf(networkItem.getType().name()));
                accessLists.add(accessList);
            }

            return accessLists;
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}
