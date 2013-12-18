package org.openstack.atlas.service.domain.services.helpers;


import java.io.Serializable;

public enum AlertType implements Serializable {
    ZEUS_FAILURE, API_FAILURE, DATABASE_FAILURE, USAGE_FAILURE;
    private final static long serialVersionUID = 532512316L;
}