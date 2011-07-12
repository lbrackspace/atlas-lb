package org.openstack.atlas.service.domain.events.entities;

import javax.persistence.Table;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "session_persistence_event")
public class SessionPersistenceEvent extends Event implements Serializable {
    private final static long serialVersionUID = 532512316L;
}
