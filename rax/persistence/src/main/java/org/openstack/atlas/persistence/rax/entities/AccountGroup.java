package org.openstack.atlas.persistence.rax.entities;

import org.openstack.atlas.service.domain.entities.Entity;
import org.openstack.atlas.service.domain.entities.GroupRateLimit;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "account_group")
public class AccountGroup extends org.openstack.atlas.service.domain.entities.AccountGroup implements Serializable {

}
