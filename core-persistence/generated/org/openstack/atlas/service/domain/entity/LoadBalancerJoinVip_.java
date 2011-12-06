package org.openstack.atlas.service.domain.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip.Id;

@StaticMetamodel(LoadBalancerJoinVip.class)
public abstract class LoadBalancerJoinVip_ {

	public static volatile SingularAttribute<LoadBalancerJoinVip, Integer> port;
	public static volatile SingularAttribute<LoadBalancerJoinVip, Id> id;
	public static volatile SingularAttribute<LoadBalancerJoinVip, LoadBalancer> loadBalancer;
	public static volatile SingularAttribute<LoadBalancerJoinVip, VirtualIp> virtualIp;

}

