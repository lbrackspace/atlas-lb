package org.openstack.atlas.service.domain.entity;

import java.io.Serializable;

public enum AccountLimitType implements Serializable {
    ACCESS_LIST_LIMIT,
    BATCH_DELETE_LIMIT,
    IPV6_LIMIT,
    LOADBALANCER_LIMIT,
    NODE_LIMIT;
    private final static long serialVersionUID = 532512316L;
}
