package org.openstack.atlas.service.domain.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(HealthMonitor.class)
public abstract class HealthMonitor_ extends org.openstack.atlas.service.domain.entity.Entity_ {

	public static volatile SingularAttribute<HealthMonitor, LoadBalancer> loadBalancer;
	public static volatile SingularAttribute<HealthMonitor, String> path;
	public static volatile SingularAttribute<HealthMonitor, Integer> delay;
	public static volatile SingularAttribute<HealthMonitor, String> type;
	public static volatile SingularAttribute<HealthMonitor, Integer> attemptsBeforeDeactivation;
	public static volatile SingularAttribute<HealthMonitor, Integer> timeout;

}

