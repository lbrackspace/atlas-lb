package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "loadbalancer_virtualipv6")
public class LoadBalancerJoinVip6 implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Embeddable
    public static class Id implements Serializable {
        private final static long serialVersionUID = 532512316L;

        @Column(name = "loadbalancer_id")
        private Integer loadBalancerId;

        @Column(name = "virtualip6_id")
        private Integer virtualIpId;

        public Id() {}

        public Id(Integer loadBalancerId, Integer virtualIpId) {
            this.loadBalancerId = loadBalancerId;
            this.virtualIpId = virtualIpId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Id id = (Id) o;

            if (loadBalancerId != null ? !loadBalancerId.equals(id.loadBalancerId) : id.loadBalancerId != null)
                return false;
            if (virtualIpId != null ? !virtualIpId.equals(id.virtualIpId) : id.virtualIpId != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = loadBalancerId != null ? loadBalancerId.hashCode() : 0;
            result = 31 * result + (virtualIpId != null ? virtualIpId.hashCode() : 0);
            return result;
        }
    }

    @EmbeddedId
    private Id id = new Id();

    @Column(name = "port")
    private Integer port;

    @ManyToOne
    @JoinColumn(name = "loadbalancer_id", insertable = false, updatable = false)
    private LoadBalancer loadBalancer;

    @ManyToOne
    @JoinColumn(name = "virtualip6_id", insertable = false, updatable = false)
    private VirtualIpv6 virtualIp;

    public LoadBalancerJoinVip6() {}

    public LoadBalancerJoinVip6(Integer port, LoadBalancer loadBalancer, VirtualIpv6 virtualIpv6) {
        this.port = port;
        this.loadBalancer = loadBalancer;
        this.virtualIp = virtualIpv6;
        this.id.loadBalancerId = loadBalancer.getId();
        this.id.virtualIpId = virtualIpv6.getId();
        loadBalancer.getLoadBalancerJoinVip6Set().add(this);
        virtualIpv6.getLoadBalancerJoinVip6Set().add(this);
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public VirtualIpv6 getVirtualIp() {
        return virtualIp;
    }

    public void setVirtualIp(VirtualIpv6 virtualIp) {
        this.virtualIp = virtualIp;
    }
}
