package org.openstack.atlas.service.domain.event.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(NodeEvent.class)
public abstract class NodeEvent_ extends org.openstack.atlas.service.domain.event.entity.Event_ {

	public static volatile SingularAttribute<NodeEvent, Integer> nodeId;

}

