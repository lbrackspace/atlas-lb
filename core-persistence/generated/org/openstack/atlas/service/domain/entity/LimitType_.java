package org.openstack.atlas.service.domain.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(LimitType.class)
public abstract class LimitType_ {

	public static volatile SingularAttribute<LimitType, String> description;
	public static volatile SingularAttribute<LimitType, AccountLimitType> name;
	public static volatile SingularAttribute<LimitType, Integer> defaultValue;

}

