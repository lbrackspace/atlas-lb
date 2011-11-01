package org.openstack.atlas.service.domain.event;

public enum UsageEvent {
    SSL_OFF,
    SSL_ON,
    CREATE_LOADBALANCER,
    DELETE_LOADBALANCER,
    CREATE_VIRTUAL_IP,
    DELETE_VIRTUAL_IP,
    SUSPEND_LOADBALANCER,
    UNSUSPEND_LOADBALANCER;
}
