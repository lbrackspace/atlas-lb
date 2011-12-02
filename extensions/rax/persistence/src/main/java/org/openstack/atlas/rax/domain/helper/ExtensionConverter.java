package org.openstack.atlas.rax.domain.helper;

import org.openstack.atlas.api.v1.extensions.rax.NetworkItem;
import org.openstack.atlas.api.v1.extensions.rax.NetworkItemType;
import org.openstack.atlas.rax.domain.entity.RaxAccessList;

import java.util.Set;

public final class ExtensionConverter {
    public static org.openstack.atlas.api.v1.extensions.rax.AccessList convertAccessList(Set<RaxAccessList> accessListSet) {
        org.openstack.atlas.api.v1.extensions.rax.AccessList dataModelAccessList = new org.openstack.atlas.api.v1.extensions.rax.AccessList();
        for (RaxAccessList accessList : accessListSet) {
            NetworkItem networkItem = new NetworkItem();
            networkItem.setId(accessList.getId());
            networkItem.setAddress(accessList.getIpAddress());
            //networkItem.setIpVersion(org.openstack.atlas.api.v1.extensions.rax.IpVersion.fromValue(accessList.getIpVersion().name()));
            networkItem.setType(NetworkItemType.fromValue(accessList.getType().name()));
            dataModelAccessList.getNetworkItems().add(networkItem);
        }
        return dataModelAccessList;
    }
}
