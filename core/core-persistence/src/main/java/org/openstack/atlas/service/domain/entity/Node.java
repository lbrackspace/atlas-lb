package org.openstack.atlas.service.domain.entity;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "node")
public class Node extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @ManyToOne
    @JoinColumn(name = "load_balancer_id")
    private LoadBalancer loadBalancer;

    @Column(name = "ip_address", length = 39)
    private String ipAddress;

    @Column(name = "port")
    private Integer port;

    @Column(nullable = false)
    private Integer weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_condition")
    private NodeCondition condition;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private NodeStatus status;

    @Transient
    private boolean isNew;

    @Transient
    private boolean isToBeUpdated;

    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public NodeCondition getCondition() {
        return condition;
    }

    public void setCondition(NodeCondition condition) {
        this.condition = condition;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public boolean isToBeUpdated() {
        return isToBeUpdated;
    }

    public void setToBeUpdated(boolean toBeUpdated) {
        isToBeUpdated = toBeUpdated;
    }

    @Override
    public String toString() {
        return "Node{" +
                "isToBeUpdated=" + isToBeUpdated +
                ", isNew=" + isNew +
                ", status=" + status +
                ", condition=" + condition +
                ", weight=" + weight +
                ", port=" + port +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}
