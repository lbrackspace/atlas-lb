package org.openstack.atlas.service.domain.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AccountLimit.class)
public abstract class AccountLimit_ extends org.openstack.atlas.service.domain.entity.Entity_ {

	public static volatile SingularAttribute<AccountLimit, Integer> limit;
	public static volatile SingularAttribute<AccountLimit, LimitType> limitType;
	public static volatile SingularAttribute<AccountLimit, Integer> accountId;

}

