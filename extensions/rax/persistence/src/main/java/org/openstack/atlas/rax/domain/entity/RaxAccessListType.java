package org.openstack.atlas.rax.domain.entity;

import java.io.Serializable;

public enum RaxAccessListType implements Serializable {
    ALLOW,
    DENY;

    private final static long serialVersionUID = 532512316L;
}
