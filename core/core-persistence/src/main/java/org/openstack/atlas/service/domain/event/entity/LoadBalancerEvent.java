package org.openstack.atlas.service.domain.event.entity;

import javax.persistence.Table;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "loadbalancer_event")
public class LoadBalancerEvent extends Event implements Serializable {

    private final static long serialVersionUID = 532512316L;
}
