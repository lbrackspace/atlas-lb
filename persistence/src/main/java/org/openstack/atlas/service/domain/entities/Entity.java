package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;

@MappedSuperclass
public abstract class Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Transient
    private String userName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}

