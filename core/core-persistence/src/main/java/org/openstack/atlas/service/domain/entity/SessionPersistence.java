package org.openstack.atlas.service.domain.entity;

import java.io.Serializable;

public enum SessionPersistence implements Serializable {
    NONE,
    HTTP_COOKIE;

    private final static long serialVersionUID = 532512316L;
}
