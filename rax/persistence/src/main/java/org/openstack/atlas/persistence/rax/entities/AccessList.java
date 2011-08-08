package org.openstack.atlas.persistence.rax.entities;

import org.openstack.atlas.service.domain.entities.AccessListType;
import org.openstack.atlas.service.domain.entities.IpVersion;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
@Table(name = "access_list")
public class AccessList extends org.openstack.atlas.service.domain.entities.AccessList implements Serializable {

}
