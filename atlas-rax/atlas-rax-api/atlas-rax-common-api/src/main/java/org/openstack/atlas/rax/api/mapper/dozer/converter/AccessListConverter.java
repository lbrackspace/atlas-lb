package org.openstack.atlas.rax.api.mapper.dozer.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dozer.CustomConverter;
import org.openstack.atlas.api.v1.extensions.rax.NetworkItem;
import org.openstack.atlas.api.v1.extensions.rax.NetworkItemType;
import org.openstack.atlas.rax.domain.entity.AccessList;
import org.openstack.atlas.rax.domain.entity.AccessListType;
import org.openstack.atlas.service.domain.entity.IpVersion;
import org.openstack.atlas.service.domain.exception.NoMappableConstantException;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccessListConverter implements CustomConverter {
    private static Log LOG = LogFactory.getLog(AccessListConverter.class.getName());

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class<?> destinationClass, Class<?> sourceClass) {

        if (sourceFieldValue == null) {
            return null;
        }

        if (destinationClass == List.class && sourceFieldValue instanceof java.util.Set) {
            final Set<AccessList> accessListSet = (Set<AccessList>) sourceFieldValue;
            List<Object> anies = (List<Object>) existingDestinationFieldValue;
            if (anies == null) anies = new ArrayList<Object>();

            try {
                org.openstack.atlas.api.v1.extensions.rax.AccessList dataModelAccessList = convertAccessList(accessListSet);
                Node objectNode = XmlHelper.marshall(dataModelAccessList);
                anies.add(objectNode);
            } catch (Exception e) {
                LOG.error("Error converting accessList from domain to data model", e);
            }

            return anies;
        }

        if (destinationClass == Set.class) {
            Set<org.openstack.atlas.rax.domain.entity.AccessList> accessLists = new HashSet<AccessList>();
            org.openstack.atlas.api.v1.extensions.rax.AccessList _accessList = ExtensionObjectMapper.getAnyElement((List<Object>) sourceFieldValue, org.openstack.atlas.api.v1.extensions.rax.AccessList.class);

            if (_accessList == null) return null;

            for (NetworkItem networkItem : _accessList.getNetworkItems()) {
                AccessList accessList = new AccessList();
                accessList.setId(networkItem.getId());
                accessList.setIpAddress(networkItem.getAddress());
                accessList.setIpVersion(IpVersion.valueOf(networkItem.getIpVersion().name()));
                accessList.setType(AccessListType.valueOf(networkItem.getType().name()));
                accessLists.add(accessList);
            }

            return accessLists;
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }


    private org.openstack.atlas.api.v1.extensions.rax.AccessList convertAccessList(Set<AccessList> accessListSet) {
        org.openstack.atlas.api.v1.extensions.rax.AccessList dataModelAccessList = new org.openstack.atlas.api.v1.extensions.rax.AccessList();
        for (AccessList accessList : accessListSet) {
            NetworkItem networkItem = new NetworkItem();
            networkItem.setId(accessList.getId());
            networkItem.setAddress(accessList.getIpAddress());
            networkItem.setIpVersion(org.openstack.atlas.api.v1.extensions.rax.IpVersion.fromValue(accessList.getIpVersion().name()));
            networkItem.setType(NetworkItemType.fromValue(accessList.getType().name()));
            dataModelAccessList.getNetworkItems().add(networkItem);
        }
        return dataModelAccessList;
    }


}
