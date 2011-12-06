package org.openstack.atlas.service.domain.event.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(HealthMonitorEvent.class)
public abstract class HealthMonitorEvent_ extends org.openstack.atlas.service.domain.event.entity.Event_ {

	public static volatile SingularAttribute<HealthMonitorEvent, Integer> healthMonitorId;

}

