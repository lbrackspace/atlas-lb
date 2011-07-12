package org.openstack.atlas.service.domain.entities;

import java.io.Serializable;

public enum NodeCondition implements Serializable {
    ENABLED, DISABLED, DRAINING;
    private final static long serialVersionUID = 532512316L;
}
