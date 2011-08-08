package org.openstack.atlas.persistence.rax.entities;

import org.openstack.atlas.service.domain.entities.LoadBalancer;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
@Table(name = "ticket")
public class Ticket extends org.openstack.atlas.service.domain.entities.Ticket implements Serializable {

}
