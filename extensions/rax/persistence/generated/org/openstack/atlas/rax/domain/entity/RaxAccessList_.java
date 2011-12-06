package org.openstack.atlas.rax.domain.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import org.openstack.atlas.service.domain.entity.IpVersion;

@StaticMetamodel(RaxAccessList.class)
public abstract class RaxAccessList_ extends org.openstack.atlas.service.domain.entity.Entity_ {

	public static volatile SingularAttribute<RaxAccessList, IpVersion> ipVersion;
	public static volatile SingularAttribute<RaxAccessList, RaxLoadBalancer> loadbalancer;
	public static volatile SingularAttribute<RaxAccessList, RaxAccessListType> type;
	public static volatile SingularAttribute<RaxAccessList, String> ipAddress;

}

