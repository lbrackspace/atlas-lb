package org.openstack.atlas.service.domain.entities;

import org.openstack.atlas.service.domain.exceptions.NoMappableConstantException;
import org.openstack.atlas.docs.loadbalancers.api.v1.PersistenceType;

import java.io.Serializable;

public enum SessionPersistence implements Serializable {
    NONE(null),
    HTTP_COOKIE(PersistenceType.HTTP_COOKIE),
    SOURCE_IP(PersistenceType.SOURCE_IP);

    private final static long serialVersionUID = 532512316L;
    private PersistenceType myType;

    private SessionPersistence(PersistenceType myType) {
        this.myType = myType;
    }

    public PersistenceType getDataType() {
        return myType;
    }

    public org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence getSesionPersistence() {
        org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence persistence = new org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence();
        persistence.setPersistenceType(getDataType());
        return persistence;
    }

    public static SessionPersistence fromDataType(PersistenceType type) {
        for (SessionPersistence value : values()) {
            if (type == value.getDataType()) {
                return value;
            }
        }

        throw new NoMappableConstantException("Could not map constant: " + type.value() + " for type: " + type.name());
    }
}
