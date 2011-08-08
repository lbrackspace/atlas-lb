package org.openstack.atlas.persistence.rax.entities;

import org.openstack.atlas.service.domain.entities.SessionPersistence;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "lb_session_persistence")
public class SessionPersistenceObject extends org.openstack.atlas.service.domain.entities.SessionPersistenceObject implements Serializable {

}
