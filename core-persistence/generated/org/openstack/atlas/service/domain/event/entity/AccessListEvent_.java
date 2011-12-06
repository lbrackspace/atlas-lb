package org.openstack.atlas.service.domain.event.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AccessListEvent.class)
public abstract class AccessListEvent_ extends org.openstack.atlas.service.domain.event.entity.Event_ {

	public static volatile SingularAttribute<AccessListEvent, Integer> access_list_id;

}

