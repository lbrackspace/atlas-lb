package org.openstack.atlas.service.domain.entities;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterStatus;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "allowed_domains")
public class AllowedDomains extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return getName().toString();
    }
}
