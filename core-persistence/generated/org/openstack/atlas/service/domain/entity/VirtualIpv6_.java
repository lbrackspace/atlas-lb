package org.openstack.atlas.service.domain.entity;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(VirtualIpv6.class)
public abstract class VirtualIpv6_ extends org.openstack.atlas.service.domain.entity.Entity_ {

	public static volatile SingularAttribute<VirtualIpv6, Integer> accountId;
	public static volatile SetAttribute<VirtualIpv6, LoadBalancerJoinVip6> loadBalancerJoinVip6Set;
	public static volatile SingularAttribute<VirtualIpv6, Integer> vipOctets;
	public static volatile SingularAttribute<VirtualIpv6, Cluster> cluster;

}

