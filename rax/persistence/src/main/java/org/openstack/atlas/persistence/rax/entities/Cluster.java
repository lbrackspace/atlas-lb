package org.openstack.atlas.persistence.rax.entities;

import org.openstack.atlas.service.domain.entities.DataCenter;
import org.openstack.atlas.service.domain.entities.VirtualIp;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cluster")
public class Cluster extends org.openstack.atlas.service.domain.entities.Cluster implements Serializable {

}