package org.openstack.atlas.persistence.rax.entities;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
@Table(name = "lb_suspension")
public class Suspension extends org.openstack.atlas.service.domain.entities.Suspension implements Serializable {

}
