package org.openstack.atlas.service.domain.entities;

import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@javax.persistence.Entity
@Table(name = "node")
public class Node extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @ManyToOne
    @JoinColumn(name = "loadbalancer_id")
    private LoadBalancer loadbalancer;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "node", fetch = FetchType.EAGER)
    @OrderBy("id")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private Set<Meta> metadata = new HashSet<Meta>();

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


    @JoinColumn(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NodeType type = NodeType.PRIMARY;

    @Transient
    private boolean isNew;

    @Transient
    private boolean isToBeUpdated;

    public NodeCondition getCondition() {
        return condition;
    }

    public void setCondition(NodeCondition condition) {
        this.condition = condition;
    }

    public LoadBalancer getLoadbalancer() {
        return loadbalancer;
    }

    public void setLoadbalancer(LoadBalancer loadbalancer) {
        this.loadbalancer = loadbalancer;
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

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public Set<Meta> getMetadata() {
        return metadata;
    }

    public void setMetadata(Set<Meta> metadata) {
        this.metadata = metadata;
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

    private static String vorn(Object obj) {
        return obj == null ? "null" : obj.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        String lid;
        if(this.getLoadbalancer() == null || this.getLoadbalancer().getId() == null){
            lid = "null";
        }else{
            lid = this.getLoadbalancer().getId().toString();
        }
        sb.append("{");
        sb.append(String.format("id=%s, ", vorn(this.getId())));
        sb.append(String.format("loadbalancer_id=%s, ", lid));
        sb.append(String.format("port=%s, ", vorn(this.getPort())));
        sb.append(String.format("wieght=%s, ", vorn(this.getWeight())));
        sb.append(String.format("condition=%s, ", vorn(this.getCondition())));
        sb.append(String.format("status=%s,", vorn(this.getStatus())));
        sb.append(String.format("type=%s",this.getType().toString()));
        sb.append("}");
        return sb.toString();
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }
}
