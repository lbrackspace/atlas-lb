package org.openstack.atlas.service.domain.entities;

import java.io.Serializable;

public enum LoadBalancerStatus implements Serializable {
    ACTIVE,
    BUILD,
    ERROR,
    PENDING_UPDATE,
    PENDING_DELETE,
    SUSPENDED,
    DELETED;
    private final static long serialVersionUID = 532512316L;
}
