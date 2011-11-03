package org.openstack.atlas.service.domain.entities;

import java.io.Serializable;

public enum ClusterStatus implements Serializable {
    ACTIVE, INACTIVE;
    private final static long serialVersionUID = 532512316L;
}