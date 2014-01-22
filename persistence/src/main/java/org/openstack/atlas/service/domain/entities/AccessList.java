package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "access_list")
public class AccessList extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @ManyToOne
    @JoinColumn(name = "loadbalancer_id")
    private LoadBalancer loadbalancer;

    @Column(name = "ip_address", length = 39)
    private String ipAddress;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private AccessListType type;

    public LoadBalancer getLoadbalancer() {
        return loadbalancer;
    }

    public void setLoadbalancer(LoadBalancer loadbalancer) {
        this.loadbalancer = loadbalancer;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public AccessListType getType() {
        return type;
    }

    public void setType(AccessListType type) {
        this.type = type;
    }

    private static String vorn(Object obj) {
        return obj == null ? "null" : obj.toString();
    }

    @Override
    public boolean equals(Object accessList) {
        return ((AccessList)accessList).getId().equals(this.getId());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(String.format("id=%s, ", vorn(this.getId())));
        sb.append(String.format("loadbalancer = %s, ", vorn(this.getLoadbalancer())));
        sb.append(String.format("ip_address = %s, ", vorn(this.getIpAddress())));
        sb.append(String.format("type= %s", vorn(this.getType())));
        sb.append("}");

        return sb.toString();
    }
}
