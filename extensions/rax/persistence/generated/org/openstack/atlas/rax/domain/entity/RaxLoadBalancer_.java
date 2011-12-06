package org.openstack.atlas.rax.domain.entity;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(RaxLoadBalancer.class)
public abstract class RaxLoadBalancer_ extends org.openstack.atlas.service.domain.entity.LoadBalancer_ {

	public static volatile SetAttribute<RaxLoadBalancer, RaxAccessList> accessLists;
	public static volatile SingularAttribute<RaxLoadBalancer, String> crazyName;
	public static volatile SingularAttribute<RaxLoadBalancer, Boolean> connectionLogging;

}

