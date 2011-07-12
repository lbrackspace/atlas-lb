package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@javax.persistence.Entity
@Table(name = "lb_rate_limit")
public class RateLimit extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @OneToOne
    @JoinColumn(name = "loadbalancer_id")
    private LoadBalancer loadbalancer;

    @Column(name = "expiration_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar expirationTime;

    @Column(name = "max_requests_per_second", nullable = false)
    private Integer maxRequestsPerSecond;

    @Transient
    private Ticket ticket;

    public LoadBalancer getLoadbalancer() {
        return loadbalancer;
    }

    public void setLoadbalancer(LoadBalancer loadbalancer) {
        this.loadbalancer = loadbalancer;
    }

    public Calendar getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Calendar expirationTime) {
        this.expirationTime = expirationTime;
    }

    public Integer getMaxRequestsPerSecond() {
        return maxRequestsPerSecond;
    }

    public void setMaxRequestsPerSecond(Integer maxRequestsPerSecond) {
        this.maxRequestsPerSecond = maxRequestsPerSecond;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }
}
