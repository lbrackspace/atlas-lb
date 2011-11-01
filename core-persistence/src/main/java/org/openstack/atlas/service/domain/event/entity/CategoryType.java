package org.openstack.atlas.service.domain.event.entity;

import java.io.Serializable;

public enum CategoryType implements Serializable {
    CREATE, DELETE, UPDATE;
    private final static long serialVersionUID = 532512316L;
}