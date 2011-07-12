package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "loadbalancer_virtualip")
public class LoadBalancerJoinVip implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Embeddable
    public static class Id implements Serializable {
        private final static long serialVersionUID = 532512316L;

        @Column(name = "loadbalancer_id")
        private Integer loadBalancerId;

        @Column(name = "virtualip_id")
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
    @JoinColumn(name = "virtualip_id", insertable = false, updatable = false)
    private VirtualIp virtualIp;

    public LoadBalancerJoinVip() {}

    public LoadBalancerJoinVip(Integer port, LoadBalancer loadBalancer, VirtualIp virtualIp) {
        this.port = port;
        this.loadBalancer = loadBalancer;
        this.virtualIp = virtualIp;
        this.id.loadBalancerId = loadBalancer.getId();
        this.id.virtualIpId = virtualIp.getId();
        loadBalancer.getLoadBalancerJoinVipSet().add(this);
        virtualIp.getLoadBalancerJoinVipSet().add(this);
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

    public VirtualIp getVirtualIp() {
        return virtualIp;
    }

    public void setVirtualIp(VirtualIp virtualIp) {
        this.virtualIp = virtualIp;
    }
}
