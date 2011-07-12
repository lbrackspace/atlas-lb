package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "lb_session_persistence")
public class SessionPersistenceObject implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Id
    @Column(name = "name", length = 32, unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionPersistence name;

    @Column(name = "description")
    private String description;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    public SessionPersistenceObject() {
    }

    public SessionPersistenceObject(SessionPersistence name, String description, boolean enabled) {
        this.name = name;
        this.description = description;
        this.enabled = enabled;
    }

    public SessionPersistence getName() {
        return name;
    }

    public void setName(SessionPersistence name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
