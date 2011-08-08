package org.openstack.atlas.persistence.rax.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "version")
public class Version extends org.openstack.atlas.service.domain.entities.Version implements Serializable {

}
