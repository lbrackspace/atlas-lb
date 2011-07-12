/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "lb_algorithm")
public class LoadBalancerAlgorithmObject implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Id
    @Column(name = "name", length = 32, unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private LoadBalancerAlgorithm name;

    @Column(name = "description")
    private String description;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    public LoadBalancerAlgorithmObject() {
    }

    public LoadBalancerAlgorithmObject(LoadBalancerAlgorithm name, String description, boolean enabled) {
        this.name = name;
        this.description = description;
        this.enabled = enabled;
    }

    public LoadBalancerAlgorithm getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(LoadBalancerAlgorithm name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
