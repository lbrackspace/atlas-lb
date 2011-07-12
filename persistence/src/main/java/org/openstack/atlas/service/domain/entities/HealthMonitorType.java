package org.openstack.atlas.service.domain.entities;

import java.io.Serializable;

public enum HealthMonitorType implements Serializable {
    CONNECT, HTTP, HTTPS;

    private final static long serialVersionUID = 532512316L;
}
