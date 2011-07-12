package org.openstack.atlas.service.domain.entities;

import java.io.Serializable;

public enum LoadBalancerAlgorithm implements Serializable {
    WEIGHTED_LEAST_CONNECTIONS, LEAST_CONNECTIONS, RANDOM, ROUND_ROBIN, WEIGHTED_ROUND_ROBIN;
    private final static long serialVersionUID = 532512316L;
}
