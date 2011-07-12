package org.openstack.atlas.service.domain.entities;

import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItemType;
import org.openstack.atlas.service.domain.exceptions.NoMappableConstantException;

import java.io.Serializable;

public enum AccessListType implements Serializable {
    ALLOW(NetworkItemType.ALLOW),
    DENY(NetworkItemType.DENY);

    private final static long serialVersionUID = 532512316L;
    private final NetworkItemType myType;

    AccessListType(NetworkItemType myType) {
        this.myType = myType;
    }

    public NetworkItemType getDataType() {
        return myType;
    }

    public static AccessListType fromDataType(NetworkItemType type) {
        for (AccessListType value : values()) {
            if (type == value.getDataType()) {
                return value;
            }
        }

        throw new NoMappableConstantException("Could not map constant: " + type.value() + " for type: " + type.name());
    }
}
