package org.openstack.atlas.persistence.rax.entities;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
@Table(name = "node")
public class Node extends org.openstack.atlas.service.domain.entities.Node implements Serializable {

}
