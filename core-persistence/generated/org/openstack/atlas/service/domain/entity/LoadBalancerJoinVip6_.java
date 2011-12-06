package org.openstack.atlas.service.domain.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip6.Id;

@StaticMetamodel(LoadBalancerJoinVip6.class)
public abstract class LoadBalancerJoinVip6_ {

	public static volatile SingularAttribute<LoadBalancerJoinVip6, Integer> port;
	public static volatile SingularAttribute<LoadBalancerJoinVip6, Id> id;
	public static volatile SingularAttribute<LoadBalancerJoinVip6, LoadBalancer> loadBalancer;
	public static volatile SingularAttribute<LoadBalancerJoinVip6, VirtualIpv6> virtualIp;

}

