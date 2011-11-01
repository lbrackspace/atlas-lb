package org.openstack.atlas.service.domain.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

@javax.persistence.Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "vendor",
        discriminatorType = DiscriminatorType.STRING
)
@DiscriminatorValue("CORE")
@Table(name = "virtual_ipv4")
public class VirtualIp extends org.openstack.atlas.service.domain.entity.Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @OneToMany(mappedBy = "virtualIp")
    private Set<LoadBalancerJoinVip> loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();

    @Column(name = "address", length = 39, unique = true, nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private VirtualIpType vipType;

    @ManyToOne
    @JoinColumn(name = "cluster_id", nullable = true) // TODO: Should not be nullable. Need to get cluster internally
    private Cluster cluster;

    @Column(name = "last_deallocation")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar lastDeallocation;

    @Column(name = "last_allocation")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar lastAllocation;

    @Column(name = "is_allocated", nullable = false)
    private Boolean isAllocated = false;

    public Set<LoadBalancerJoinVip> getLoadBalancerJoinVipSet() {
        if(loadBalancerJoinVipSet == null) loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();
        return loadBalancerJoinVipSet;
    }

    public void setLoadBalancerJoinVipSet(Set<LoadBalancerJoinVip> loadBalancerJoinVipSet) {
        this.loadBalancerJoinVipSet = loadBalancerJoinVipSet;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public VirtualIpType getVipType() {
        return vipType;
    }

    public void setVipType(VirtualIpType vipType) {
        this.vipType = vipType;
    }

    public Cluster getCluster() {
        return this.cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public Calendar getLastDeallocation() {
        return lastDeallocation;
    }

    public void setLastDeallocation(Calendar lastDeallocation) {
        this.lastDeallocation = lastDeallocation;
    }

    public Calendar getLastAllocation() {
        return lastAllocation;
    }

    public void setLastAllocation(Calendar lastAllocation) {
        this.lastAllocation = lastAllocation;
    }

    public Boolean isAllocated() {
        return isAllocated;
    }

    public void setAllocated(Boolean allocated) {
        isAllocated = allocated;
    }

    @Override
    public String toString() {
        return "VirtualIp{" +
                "loadBalancerJoinVipSet=" + loadBalancerJoinVipSet +
                ", ipAddress='" + address + '\'' +
                ", vipType=" + vipType +
                ", cluster=" + cluster +
                ", lastDeallocation=" + lastDeallocation +
                ", lastAllocation=" + lastAllocation +
                ", isAllocated=" + isAllocated +
                '}';
    }
}
