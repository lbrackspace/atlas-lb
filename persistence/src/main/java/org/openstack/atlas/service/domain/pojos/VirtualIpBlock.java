package org.openstack.atlas.service.domain.pojos;

import java.io.Serializable;

public class VirtualIpBlock implements Serializable {
    private final static long serialVersionUID = 532512316L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
    protected String firstIp;
    protected String lastIp;

    public String getFirstIp() {
        return firstIp;
    }

    public void setFirstIp(String firstIp) {
        this.firstIp = firstIp;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

}
