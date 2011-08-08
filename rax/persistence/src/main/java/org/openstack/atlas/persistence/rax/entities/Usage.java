package org.openstack.atlas.persistence.rax.entities;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Calendar;

@Entity
@Table(name="lb_usage")
public class Usage extends org.openstack.atlas.service.domain.entities.Usage implements Serializable {

}
