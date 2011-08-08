package org.openstack.atlas.persistence.rax.entities;

import org.openstack.atlas.service.domain.entities.Entity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "group_rate_limit")
public class GroupRateLimit extends org.openstack.atlas.service.domain.entities.GroupRateLimit implements Serializable {

}




