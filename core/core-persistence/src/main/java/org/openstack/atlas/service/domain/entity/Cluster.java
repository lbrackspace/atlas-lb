package org.openstack.atlas.service.domain.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@javax.persistence.Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "vendor",
        discriminatorType = DiscriminatorType.STRING
)
@DiscriminatorValue("CORE")
@Table(name = "cluster")
public class Cluster extends org.openstack.atlas.service.domain.entity.Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "cluster")
    private Set<VirtualIp> virtualIps = new HashSet<VirtualIp>();

    @Column(name = "cluster_ipv6_cidr", length = 43, nullable = true)
    private String clusterIpv6Cidr;

    public Set<VirtualIp> getVirtualIps() {
        if (virtualIps == null) {
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

    public String getClusterIpv6Cidr() {
        return clusterIpv6Cidr;
    }

    public void setClusterIpv6Cidr(String clusterIpv6Cidr) {
        this.clusterIpv6Cidr = clusterIpv6Cidr;
    }

    @Override
    public String toString() {
        return "Cluster{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", virtualIps=" + virtualIps +
                ", clusterIpv6Cidr='" + clusterIpv6Cidr + '\'' +
                '}';
    }
}
