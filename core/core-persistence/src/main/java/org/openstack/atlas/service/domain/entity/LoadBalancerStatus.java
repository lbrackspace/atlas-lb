package org.openstack.atlas.service.domain.entity;

import java.io.Serializable;

public enum LoadBalancerStatus implements Serializable {
    ACTIVE,
    QUEUED,
    BUILD,
    PENDING_UPDATE,
    PENDING_DELETE,
    SUSPENDED,
    DELETED,
    ERROR;
    
    private final static long serialVersionUID = 532512316L;
}
