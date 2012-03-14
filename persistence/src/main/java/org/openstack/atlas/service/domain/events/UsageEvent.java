package org.openstack.atlas.service.domain.events;

public enum UsageEvent {
    SSL_OFF,
    SSL_ONLY_ON,
    SSL_MIXED_ON,
    CREATE_LOADBALANCER,
    DELETE_LOADBALANCER,
    CREATE_VIRTUAL_IP,
    DELETE_VIRTUAL_IP,
    SUSPEND_LOADBALANCER,
    UNSUSPEND_LOADBALANCER;
}
