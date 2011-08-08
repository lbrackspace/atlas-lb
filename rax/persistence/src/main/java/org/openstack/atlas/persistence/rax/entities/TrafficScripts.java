package org.openstack.atlas.persistence.rax.entities;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Calendar;

@Entity
@Table(name = "traffic_scripts")
public class TrafficScripts extends org.openstack.atlas.service.domain.entities.TrafficScripts implements Serializable {

}
