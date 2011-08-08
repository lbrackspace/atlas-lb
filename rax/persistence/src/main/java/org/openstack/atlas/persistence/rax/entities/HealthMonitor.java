package org.openstack.atlas.persistence.rax.entities;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
@Table(name = "health_monitor")
public class HealthMonitor extends org.openstack.atlas.service.domain.entities.HealthMonitor implements Serializable {

}
