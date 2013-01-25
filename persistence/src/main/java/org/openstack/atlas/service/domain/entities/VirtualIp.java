package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@javax.persistence.Entity
@Table(name = "virtual_ip_ipv4")
public class VirtualIp extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @OneToMany(mappedBy = "virtualIp")
    private Set<LoadBalancerJoinVip> loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();
    @Column(name = "ip_address", length = 39, unique = true, nullable = false)
    private String ipAddress;
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
    private Boolean isAllocated;
    @Transient
    private Ticket ticket;
    @Transient
    private IpVersion ipVersion;

    public IpVersion getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(IpVersion ipVersion) {
        this.ipVersion = ipVersion;
    }

    public Set<LoadBalancerJoinVip> getLoadBalancerJoinVipSet() {
        if(loadBalancerJoinVipSet == null) loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();
        return loadBalancerJoinVipSet;
    }

    public void setLoadBalancerJoinVipSet(Set<LoadBalancerJoinVip> loadBalancerJoinVipSet) {
        this.loadBalancerJoinVipSet = loadBalancerJoinVipSet;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
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

    //Value or None
    private static String vorn(Object obj) {
        return obj == null ? "null" : obj.toString();
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("{"));
        sb.append(String.format("id = %s,", vorn(this.getId())));
        sb.append(String.format("ip_address = %s,", vorn(this.getIpAddress())));
        sb.append(String.format("vip_type = %s,", vorn(this.getVipType())));
        sb.append(String.format("last_deallocation = %s", vorn(this.getLastDeallocation())));
        sb.append(String.format("}"));
        return sb.toString();
    }
}
