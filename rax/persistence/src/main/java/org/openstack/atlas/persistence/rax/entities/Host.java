package org.openstack.atlas.persistence.rax.entities;

import org.hibernate.annotations.Cascade;
import org.openstack.atlas.service.domain.entities.Backup;
import org.openstack.atlas.service.domain.entities.Cluster;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "host")
public class Host extends org.openstack.atlas.service.domain.entities.Host implements Serializable {


}
