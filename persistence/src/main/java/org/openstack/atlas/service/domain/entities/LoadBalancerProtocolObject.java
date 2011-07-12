package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "lb_protocol")
public class LoadBalancerProtocolObject implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Id
    @Column(name = "name", length = 32, unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private LoadBalancerProtocol name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "port", nullable = false)
    private Integer port;

    @Column(name = "enabled", nullable = false)
    private boolean isEnabled;

    public LoadBalancerProtocolObject() {
    }

    public LoadBalancerProtocolObject(LoadBalancerProtocol name, String description, Integer port, Boolean enabled) {
        this.name = name;
        this.description = description;
        this.port = port;
        isEnabled = enabled;
    }

    public LoadBalancerProtocol getName() {
        return name;
    }

    public void setName(LoadBalancerProtocol name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
