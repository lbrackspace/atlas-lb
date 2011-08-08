package org.openstack.atlas.persistence.rax.entities;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
@Table(name = "account_limits")
public class AccountLimit extends org.openstack.atlas.service.domain.entities.AccountLimit implements Serializable {

}




