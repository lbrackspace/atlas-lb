package org.openstack.atlas.service.domain.entities;

import java.io.Serializable;

public enum ClusterType implements Serializable {

    STANDARD, INTERNAL, SMOKE;
    private static final long serialVersionUID = 532512316L;
}
