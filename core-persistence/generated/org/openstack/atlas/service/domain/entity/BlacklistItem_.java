package org.openstack.atlas.service.domain.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(BlacklistItem.class)
public abstract class BlacklistItem_ extends org.openstack.atlas.service.domain.entity.Entity_ {

	public static volatile SingularAttribute<BlacklistItem, BlacklistType> blacklistType;
	public static volatile SingularAttribute<BlacklistItem, IpVersion> ipVersion;
	public static volatile SingularAttribute<BlacklistItem, String> cidrBlock;

}

