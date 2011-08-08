package org.openstack.atlas.persistence.rax.entities;

import org.openstack.atlas.service.domain.entities.BlacklistType;
import org.openstack.atlas.service.domain.entities.IpVersion;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
@Table(name = "blacklist_item")
public class BlacklistItem extends org.openstack.atlas.service.domain.entities.BlacklistItem implements Serializable {

}


