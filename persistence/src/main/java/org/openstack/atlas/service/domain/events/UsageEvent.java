package org.openstack.atlas.service.domain.events;

public enum UsageEvent {
    SSL_OFF,
    @Deprecated SSL_ON, // Deprecated. Do not use! Production still references it. Will delete in few months once SSL_ON events get purged
    SSL_ONLY_ON,
    SSL_MIXED_ON,
    CREATE_LOADBALANCER,
    DELETE_LOADBALANCER,
    CREATE_VIRTUAL_IP,
    DELETE_VIRTUAL_IP,
    SUSPEND_LOADBALANCER,
    SUSPENDED_LOADBALANCER,
    UNSUSPEND_LOADBALANCER;
}
