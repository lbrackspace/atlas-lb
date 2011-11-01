package org.openstack.atlas.service.domain.entity;

import java.io.Serializable;

public enum BlacklistType implements Serializable {
    NODE, ACCESSLIST;
    private final static long serialVersionUID = 532512316L;
}
