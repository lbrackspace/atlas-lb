package org.openstack.atlas.persistence.rax.entities;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Calendar;

@Entity
@Table(name = "lb_rate_limit")
public class RateLimit extends org.openstack.atlas.service.domain.entities.RateLimit implements Serializable {

}
