package org.openstack.atlas.service.domain.entities;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "group_rate_limit")
public class GroupRateLimit extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;
 
    @Column(name = "group_name", nullable = false)
    private String name;

    @Column(name = "group_desc", nullable = false)
    private String description;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public void setDefault(Boolean aDefault) {
        isDefault = aDefault;
    }
}




