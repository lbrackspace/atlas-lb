package org.openstack.atlas.rax.domain.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(RaxConnectionThrottle.class)
public abstract class RaxConnectionThrottle_ extends org.openstack.atlas.service.domain.entity.ConnectionThrottle_ {

	public static volatile SingularAttribute<RaxConnectionThrottle, Integer> minConnections;
	public static volatile SingularAttribute<RaxConnectionThrottle, Integer> maxConnections;

}

