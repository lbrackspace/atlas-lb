package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "lb_suspension")
public class Suspension extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @OneToOne
    @JoinColumn(name = "loadbalancer_id",nullable=false)
    private LoadBalancer loadbalancer;

    @Column(name = "user")
    private String user;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Transient
    private Ticket ticket;

    public LoadBalancer getLoadbalancer() {
        return loadbalancer;
    }

    public void setLoadbalancer(LoadBalancer loadbalancer) {
        this.loadbalancer = loadbalancer;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }
}
