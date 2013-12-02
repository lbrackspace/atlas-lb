package org.openstack.atlas.service.domain.usage.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;

@MappedSuperclass
public abstract class BigIntEntity implements Serializable {
    private final static long serialVersionUID = 532512317L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

