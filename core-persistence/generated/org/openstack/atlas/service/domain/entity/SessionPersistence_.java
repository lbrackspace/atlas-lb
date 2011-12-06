package org.openstack.atlas.service.domain.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(SessionPersistence.class)
public abstract class SessionPersistence_ extends org.openstack.atlas.service.domain.entity.Entity_ {

	public static volatile SingularAttribute<SessionPersistence, String> persistenceType;
	public static volatile SingularAttribute<SessionPersistence, LoadBalancer> loadBalancer;

}

