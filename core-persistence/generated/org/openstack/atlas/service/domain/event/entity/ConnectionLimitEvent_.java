package org.openstack.atlas.service.domain.event.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ConnectionLimitEvent.class)
public abstract class ConnectionLimitEvent_ extends org.openstack.atlas.service.domain.event.entity.Event_ {

	public static volatile SingularAttribute<ConnectionLimitEvent, Integer> connectionLimitId;

}

