package org.openstack.atlas.service.domain.events.entities;

import java.io.Serializable;

public enum EventSeverity implements Serializable {
    CRITICAL, INFO, WARNING;
    private final static long serialVersionUID = 532512316L;
}