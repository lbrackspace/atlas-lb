package org.openstack.atlas.service.domain.events.entities;

import javax.persistence.Table;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "loadbalancer_service_event")
public class LoadBalancerServiceEvent extends Event implements Serializable {

    private final static long serialVersionUID = 532512316L;
}
