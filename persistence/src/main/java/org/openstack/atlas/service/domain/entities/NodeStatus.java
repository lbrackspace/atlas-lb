package org.openstack.atlas.service.domain.entities;

import java.io.Serializable;

public enum NodeStatus implements Serializable {
    ONLINE, OFFLINE;
    private final static long serialVersionUID = 532512316L;
}
