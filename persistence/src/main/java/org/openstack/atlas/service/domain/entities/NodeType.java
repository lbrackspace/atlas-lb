package org.openstack.atlas.service.domain.entities;

import java.io.Serializable;


public enum NodeType implements Serializable {
    PRIMARY,FAILOVER;
    private final static long serialVersionUID = 532512316L;
}
