package org.openstack.atlas.service.domain.pojos;

import java.io.Serializable;

public class SourceAddresses implements Serializable {
    private final static long serialVersionUID = 532512316L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
    protected String ipv4Servicenet;
    protected String ipv6Servicenet;
    protected String ipv4Public;
    protected String ipv6Public;

    public String getIpv4Servicenet() {
        return ipv4Servicenet;
    }

    public void setIpv4Servicenet(String ipv4Servicenet) {
        this.ipv4Servicenet = ipv4Servicenet;
    }

    public String getIpv6Servicenet() {
        return ipv6Servicenet;
    }

    public void setIpv6Servicenet(String ipv6Servicenet) {
        this.ipv6Servicenet = ipv6Servicenet;
    }

    public String getIpv4Public() {
        return ipv4Public;
    }

    public void setIpv4Public(String ipv4Public) {
        this.ipv4Public = ipv4Public;
    }

    public String getIpv6Public() {
        return ipv6Public;
    }

    public void setIpv6Public(String ipv6Public) {
        this.ipv6Public = ipv6Public;
    }

}
