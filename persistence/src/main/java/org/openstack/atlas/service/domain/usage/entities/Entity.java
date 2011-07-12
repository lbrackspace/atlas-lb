package org.openstack.atlas.service.domain.usage.entities;

import javax.persistence.*;
import java.io.Serializable;

@MappedSuperclass
public abstract class Entity implements Serializable {
    private final static long serialVersionUID = 532512317L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}

