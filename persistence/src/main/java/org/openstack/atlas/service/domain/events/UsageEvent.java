package org.openstack.atlas.service.domain.events;

import java.io.Serializable;

public enum UsageEvent implements Serializable{
    SSL_OFF,
    SSL_ONLY_ON,
    SSL_MIXED_ON,
    CREATE_LOADBALANCER,
    DELETE_LOADBALANCER,
    CREATE_VIRTUAL_IP,
    DELETE_VIRTUAL_IP,
    SUSPEND_LOADBALANCER,
    SUSPENDED_LOADBALANCER,
    UNSUSPEND_LOADBALANCER;

    private final static long serialVersionUID = 532512316L;
}
