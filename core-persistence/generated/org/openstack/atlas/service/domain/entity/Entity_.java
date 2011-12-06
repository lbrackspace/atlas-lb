package org.openstack.atlas.service.domain.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Entity.class)
public abstract class Entity_ {

	public static volatile SingularAttribute<Entity, Integer> id;

}

