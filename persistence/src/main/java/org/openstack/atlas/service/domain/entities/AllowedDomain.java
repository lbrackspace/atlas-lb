package org.openstack.atlas.service.domain.entities;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "allowed_domain")
//TODO: refactor table name
public class AllowedDomain extends Entity implements Serializable {
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
