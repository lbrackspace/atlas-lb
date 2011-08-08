package org.openstack.atlas.persistence.rax.entities;

import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "lb_protocol")
public class LoadBalancerProtocolObject extends org.openstack.atlas.service.domain.entities.LoadBalancerProtocolObject implements Serializable {

}
