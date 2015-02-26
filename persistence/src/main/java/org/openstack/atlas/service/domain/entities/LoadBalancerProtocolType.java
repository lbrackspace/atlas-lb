package org.openstack.atlas.service.domain.entities;

import java.io.Serializable;

public enum LoadBalancerProtocolType implements Serializable {
    TCP,
    UDP;

    private final static long serialVersionUID = 532512316L;
}
