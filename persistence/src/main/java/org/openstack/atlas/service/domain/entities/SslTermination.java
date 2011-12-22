package org.openstack.atlas.service.domain.entities;

import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "lb_ssl")
public class SslTermination extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @OneToOne
    @JoinColumn(name = "loadbalancer_id")
    private LoadBalancer loadbalancer;

    @Column(name = "key", nullable = true, length = 64, columnDefinition = "mediumtext")
    private String key;

    @Column(name = "cert", nullable = true,length = 64, columnDefinition = "mediumtext")
    private String cert;

    @Column(name = "enabled", nullable = true)
    private boolean enabled;

    public LoadBalancer getLoadbalancer() {
        return loadbalancer;
    }

    public void setLoadbalancer(LoadBalancer loadbalancer) {
        this.loadbalancer = loadbalancer;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCert() {
        return cert;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
