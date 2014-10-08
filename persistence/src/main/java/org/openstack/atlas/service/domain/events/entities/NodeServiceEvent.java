package org.openstack.atlas.service.domain.events.entities;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "node_service_event")
public class NodeServiceEvent extends Event implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Column(name = "node_id", nullable = false)
    private Integer nodeId;

    @Column(name = "detailed_message", columnDefinition = "mediumtext")
    private String detailedMessage;

    @Column(name = "callback_host", nullable=false)
    private String callbackHost;

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    public void setDetailedMessage(String detailedMessage) {
        this.detailedMessage = detailedMessage;
    }

    public String getCallbackHost() {
        return callbackHost;
    }

    public void setCallbackHost(String callbackHost) {
        this.callbackHost = callbackHost;
    }
}
