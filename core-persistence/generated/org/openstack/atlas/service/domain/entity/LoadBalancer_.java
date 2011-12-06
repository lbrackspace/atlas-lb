package org.openstack.atlas.service.domain.entity;

import java.util.Calendar;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(LoadBalancer.class)
public abstract class LoadBalancer_ extends org.openstack.atlas.service.domain.entity.Entity_ {

	public static volatile SingularAttribute<LoadBalancer, Integer> port;
	public static volatile SingularAttribute<LoadBalancer, String> protocol;
	public static volatile SingularAttribute<LoadBalancer, Integer> accountId;
	public static volatile SingularAttribute<LoadBalancer, Host> host;
	public static volatile SingularAttribute<LoadBalancer, ConnectionThrottle> connectionThrottle;
	public static volatile SingularAttribute<LoadBalancer, String> status;
	public static volatile SetAttribute<LoadBalancer, LoadBalancerJoinVip> loadBalancerJoinVipSet;
	public static volatile SingularAttribute<LoadBalancer, String> algorithm;
	public static volatile SingularAttribute<LoadBalancer, HealthMonitor> healthMonitor;
	public static volatile SingularAttribute<LoadBalancer, Calendar> created;
	public static volatile SingularAttribute<LoadBalancer, Calendar> updated;
	public static volatile SetAttribute<LoadBalancer, LoadBalancerJoinVip6> loadBalancerJoinVip6Set;
	public static volatile SetAttribute<LoadBalancer, Node> nodes;
	public static volatile SingularAttribute<LoadBalancer, String> name;
	public static volatile SetAttribute<LoadBalancer, UsageRecord> usage;
	public static volatile SingularAttribute<LoadBalancer, SessionPersistence> sessionPersistence;

}

