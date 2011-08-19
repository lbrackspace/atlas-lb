package org.openstack.atlas.service.domain.entity;

import org.openstack.atlas.common.ip.IPv6;
import org.openstack.atlas.common.ip.IPv6Cidr;
import org.openstack.atlas.common.ip.exception.IPStringConversionException1;

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
@Table(name = "virtual_ipv6")
public class VirtualIpv6 extends org.openstack.atlas.service.domain.entity.Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @OneToMany(mappedBy = "virtualIp")
    private Set<LoadBalancerJoinVip6> loadBalancerJoinVip6Set = new HashSet<LoadBalancerJoinVip6>();

    @ManyToOne
    @JoinColumn(name = "cluster_id", nullable = true) // TODO: Should not be nullable. Need to get cluster internally
    private Cluster cluster;

    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @Column(name = "vip_octets", nullable = false)
    private Integer vipOctets;

    public Integer getVipOctets() {
        return vipOctets;
    }

    public void setVipOctets(Integer vipOctets) {
        this.vipOctets = vipOctets;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Set<LoadBalancerJoinVip6> getLoadBalancerJoinVip6Set() {
        return loadBalancerJoinVip6Set;
    }

    public void setLoadBalancerJoinVip6Set(Set<LoadBalancerJoinVip6> loadBalancerJoinVip6Set) {
        this.loadBalancerJoinVip6Set = loadBalancerJoinVip6Set;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public String getDerivedIpString() throws IPStringConversionException1 {
        String out;
        String clusterCidrString = this.getCluster().getClusterIpv6Cidr();
        if (clusterCidrString == null) {
            String msg = String.format("Cluster[%d] has null value for ClusterIpv6Cider", this.getCluster().getId());
            throw new IPStringConversionException1(msg);
        }
        IPv6Cidr v6Cidr = new IPv6Cidr(clusterCidrString);
        IPv6 v6 = new IPv6("::");
        v6.setClusterPartition(v6Cidr);
        v6.setAccountPartition(this.getAccountId());
        v6.setVipOctets(this.getVipOctets());
        out = v6.expand();
        return out;
    }
}
