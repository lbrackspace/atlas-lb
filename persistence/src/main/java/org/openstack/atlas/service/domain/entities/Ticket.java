package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "ticket")
public class Ticket extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @ManyToOne
    @JoinColumn(name = "loadbalancer_id")
    private LoadBalancer loadbalancer;

    @Column(name = "ticket_id", length = 32, nullable = false)
    private String ticketId;

    @Column(name = "comment", columnDefinition="text",  nullable = false)
    private String comment;

    public LoadBalancer getLoadbalancer() {
        return loadbalancer;
    }

    public void setLoadbalancer(LoadBalancer loadbalancer) {
        this.loadbalancer = loadbalancer;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
