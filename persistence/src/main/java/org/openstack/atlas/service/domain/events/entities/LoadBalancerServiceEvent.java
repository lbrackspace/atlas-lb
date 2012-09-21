package org.openstack.atlas.service.domain.events.entities;

import javax.persistence.Table;
import java.io.Serializable;
import javax.persistence.Column;

@javax.persistence.Entity
@Table(name = "loadbalancer_service_event")
public class LoadBalancerServiceEvent extends Event implements Serializable {

    private final static long serialVersionUID = 532512316L;
    @Column(name = "detailed_msg", nullable = true, columnDefinition = "mediumtext")
    private String detailedMessage;

    public String getDetailedMessage() {
        return detailedMessage;
    }

    public void setDetailedMessage(String detailedMessage) {
        this.detailedMessage = detailedMessage;
    }
}
