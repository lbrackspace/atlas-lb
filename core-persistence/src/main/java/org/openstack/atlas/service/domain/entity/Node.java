package org.openstack.atlas.service.domain.entity;

import org.openstack.atlas.datamodel.AtlasTypeHelper;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "vendor",
        discriminatorType = DiscriminatorType.STRING
)
@DiscriminatorValue("CORE")
@Table(name = "node")
public class Node extends org.openstack.atlas.service.domain.entity.Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;
    public final static Integer DEFAULT_NODE_WEIGHT = 1;

    @ManyToOne
    @JoinColumn(name = "load_balancer_id")
    private LoadBalancer loadBalancer;

    @Column(name = "address", length = 39, nullable = false)
    private String address;

    @Column(name = "port", nullable = false)
    private Integer port;

    @Column(name = "weight", nullable = false)
    private Integer weight = DEFAULT_NODE_WEIGHT;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "status")
    private String status;

    @Transient
    private boolean isToBeUpdated;

    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (!AtlasTypeHelper.isValidNodeStatus(status)) throw new RuntimeException("Node status not supported.");
        this.status = status;
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
                ", address='" + address + '\'' +
                ", port=" + port +
                ", weight=" + weight +
                ", enabled=" + enabled +
                ", status='" + status + '\'' +
                '}';
    }
}
