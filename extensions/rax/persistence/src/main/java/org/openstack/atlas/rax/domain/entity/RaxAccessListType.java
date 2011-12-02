package org.openstack.atlas.rax.domain.entity;

import org.openstack.atlas.api.v1.extensions.rax.NetworkItemType;
import org.openstack.atlas.service.domain.exception.NoMappableConstantException;

import java.io.Serializable;

public enum RaxAccessListType implements Serializable {
    ALLOW(NetworkItemType.ALLOW),
    DENY(NetworkItemType.DENY);

    private final static long serialVersionUID = 532512316L;

    private final NetworkItemType myType;

    RaxAccessListType(NetworkItemType myType) {
        this.myType = myType;
    }

    public NetworkItemType getDataType() {
        return myType;
    }

    public static RaxAccessListType fromDataType(NetworkItemType type) {
        for (RaxAccessListType value : values()) {
            if (type == value.getDataType()) {
                return value;
            }
        }

        throw new NoMappableConstantException("Could not map constant: " + type.value() + " for type: " + type.name());
    }
}
