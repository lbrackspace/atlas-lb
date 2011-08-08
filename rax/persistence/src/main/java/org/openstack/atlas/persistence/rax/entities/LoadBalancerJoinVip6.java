package org.openstack.atlas.persistence.rax.entities;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.VirtualIpv6;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "loadbalancer_virtualipv6")
public class LoadBalancerJoinVip6 extends org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip6 implements Serializable {

}
