package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "defaults")
public class Defaults extends Entity implements Serializable {

    private final static long serialVersionUID = 532512316L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @Column
    private String name;

    @Column(name = "value", nullable = true, length = 32, columnDefinition = "mediumtext")
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
