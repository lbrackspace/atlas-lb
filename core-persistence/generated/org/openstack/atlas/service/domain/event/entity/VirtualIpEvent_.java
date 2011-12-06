package org.openstack.atlas.service.domain.event.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(VirtualIpEvent.class)
public abstract class VirtualIpEvent_ extends org.openstack.atlas.service.domain.event.entity.Event_ {

	public static volatile SingularAttribute<VirtualIpEvent, Integer> virtualIpId;

}

