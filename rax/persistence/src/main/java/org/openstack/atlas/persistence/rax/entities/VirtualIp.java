package org.openstack.atlas.persistence.rax.entities;

import org.openstack.atlas.service.domain.entities.IpVersion;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "virtual_ip_ipv4")
public class VirtualIp extends org.openstack.atlas.service.domain.entities.VirtualIp implements Serializable {

}
