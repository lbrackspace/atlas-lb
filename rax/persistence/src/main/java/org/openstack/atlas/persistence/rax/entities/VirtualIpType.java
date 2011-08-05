package org.openstack.atlas.persistence.rax.entities;

import java.io.Serializable;

public enum VirtualIpType implements Serializable {
    PUBLIC, SERVICENET;
    private final static long serialVersionUID = 532512316L;
}
