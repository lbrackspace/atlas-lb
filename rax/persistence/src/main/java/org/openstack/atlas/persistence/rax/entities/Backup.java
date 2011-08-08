package org.openstack.atlas.persistence.rax.entities;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Calendar;

@Entity
@Table(name = "host_backup")
public class Backup extends org.openstack.atlas.service.domain.entities.Backup implements Serializable {

}
