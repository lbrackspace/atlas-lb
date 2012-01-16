package org.openstack.atlas.rax.domain.entity;

import org.openstack.atlas.service.domain.entity.*;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="vendor",
    discriminatorType=DiscriminatorType.STRING
)
@DiscriminatorValue(Discriminator.RAX)
@Table(name = "access_list")
public class RaxAccessList extends org.openstack.atlas.service.domain.entity.Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @ManyToOne
    @JoinColumn(name = "loadbalancer_id")
    private RaxLoadBalancer loadbalancer;

    @Column(name = "ip_address", length = 39)
    private String ipAddress;

    @Column(name = "ip_version")
    @Enumerated(EnumType.STRING)
    private IpVersion ipVersion;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private RaxAccessListType type;

    public RaxLoadBalancer getLoadbalancer() {
        return loadbalancer;
    }

    public void setLoadbalancer(RaxLoadBalancer loadbalancer) {
        this.loadbalancer = loadbalancer;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public IpVersion getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(IpVersion ipVersion) {
        this.ipVersion = ipVersion;
    }

    public RaxAccessListType getType() {
        return type;
    }

    public void setType(RaxAccessListType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "AccessListTable{" +
                "type=" + type +
                ", ipVersion=" + ipVersion +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}
