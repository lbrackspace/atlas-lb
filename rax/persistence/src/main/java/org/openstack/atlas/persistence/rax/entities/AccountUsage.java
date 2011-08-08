package org.openstack.atlas.persistence.rax.entities;

import org.openstack.atlas.service.domain.entities.Entity;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Calendar;

@javax.persistence.Entity
@Table(name = "account_usage")
public class AccountUsage extends org.openstack.atlas.service.domain.entities.AccountUsage implements Serializable {

}
