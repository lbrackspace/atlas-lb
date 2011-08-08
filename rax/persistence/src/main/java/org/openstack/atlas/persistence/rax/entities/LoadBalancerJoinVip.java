package org.openstack.atlas.persistence.rax.entities;

import org.openstack.atlas.service.domain.entities.VirtualIp;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
@Table(name = "loadbalancer_virtualip")
public class LoadBalancerJoinVip extends org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip implements Serializable {

}
