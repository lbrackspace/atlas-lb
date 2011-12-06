package org.openstack.atlas.rax.domain.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(RaxHost.class)
public abstract class RaxHost_ extends org.openstack.atlas.service.domain.entity.Host_ {

	public static volatile SingularAttribute<RaxHost, String> foo;

}

