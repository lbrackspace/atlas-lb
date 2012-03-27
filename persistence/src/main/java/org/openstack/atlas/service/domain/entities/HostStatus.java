package org.openstack.atlas.service.domain.entities;

import java.io.Serializable;

public enum HostStatus implements Serializable {
    ACTIVE_TARGET, ACTIVE, FAILOVER, OFFLINE, BURN_IN,SOAP_API_ENDPOINT;
    private final static long serialVersionUID = 532512316L;
}