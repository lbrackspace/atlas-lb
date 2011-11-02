package org.openstack.atlas.service.domain.entities;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@javax.persistence.Entity
@Table(name = "cluster")
public class Cluster extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "cluster_status", length=32, nullable = false)
    private ClusterStatus clusterStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_center", length=32, nullable = false)
    private DataCenter dataCenter;

    @OneToMany(fetch=FetchType.LAZY,mappedBy="cluster")
    private Set<VirtualIp> virtualIps = new HashSet<VirtualIp>();

    @Column(name = "cluster_ipv6_cidr",length=43,nullable=true)
    private String clusterIpv6Cidr;

    public Set<VirtualIp> getVirtualIps() {
        if(virtualIps == null) {
            virtualIps = new HashSet<VirtualIp>();
        }
        return virtualIps;
    }

    public void setVirtualIps(Set<VirtualIp> virtualIps) {
        this.virtualIps = virtualIps;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DataCenter getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(DataCenter dataCenter) {
        this.dataCenter = dataCenter;
    }

    public ClusterStatus getStatus() {
        return clusterStatus;
    }

    public void setStatus(ClusterStatus clusterStatus) {
        this.clusterStatus = clusterStatus;
    }

    public String getClusterIpv6Cidr() {
        return clusterIpv6Cidr;
    }

    public void setClusterIpv6Cidr(String clusterIpv6Cidr) {
        this.clusterIpv6Cidr = clusterIpv6Cidr;
    }


}