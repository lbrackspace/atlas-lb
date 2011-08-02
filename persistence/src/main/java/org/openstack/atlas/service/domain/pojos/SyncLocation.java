package org.openstack.atlas.service.domain.pojos;

public enum SyncLocation {
    DATABASE,
    LBDEVICE;

    public String value() {
        return name();
    }

    public static SyncLocation fromValue(String v) {
        return valueOf(v);
    }

}
