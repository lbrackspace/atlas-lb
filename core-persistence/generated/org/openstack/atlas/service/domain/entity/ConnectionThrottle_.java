package org.openstack.atlas.service.domain.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ConnectionThrottle.class)
public abstract class ConnectionThrottle_ extends org.openstack.atlas.service.domain.entity.Entity_ {

	public static volatile SingularAttribute<ConnectionThrottle, LoadBalancer> loadBalancer;
	public static volatile SingularAttribute<ConnectionThrottle, Integer> rateInterval;
	public static volatile SingularAttribute<ConnectionThrottle, Integer> maxRequestRate;

}

