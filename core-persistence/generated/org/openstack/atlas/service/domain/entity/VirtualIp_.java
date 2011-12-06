package org.openstack.atlas.service.domain.entity;

import java.util.Calendar;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(VirtualIp.class)
public abstract class VirtualIp_ extends org.openstack.atlas.service.domain.entity.Entity_ {

	public static volatile SingularAttribute<VirtualIp, String> address;
	public static volatile SingularAttribute<VirtualIp, Calendar> lastAllocation;
	public static volatile SingularAttribute<VirtualIp, Cluster> cluster;
	public static volatile SingularAttribute<VirtualIp, VirtualIpType> vipType;
	public static volatile SingularAttribute<VirtualIp, Boolean> isAllocated;
	public static volatile SetAttribute<VirtualIp, LoadBalancerJoinVip> loadBalancerJoinVipSet;
	public static volatile SingularAttribute<VirtualIp, Calendar> lastDeallocation;

}

