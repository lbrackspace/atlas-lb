package org.openstack.atlas.service.domain.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Node.class)
public abstract class Node_ extends org.openstack.atlas.service.domain.entity.Entity_ {

	public static volatile SingularAttribute<Node, Integer> port;
	public static volatile SingularAttribute<Node, Boolean> enabled;
	public static volatile SingularAttribute<Node, Integer> weight;
	public static volatile SingularAttribute<Node, String> status;
	public static volatile SingularAttribute<Node, String> address;
	public static volatile SingularAttribute<Node, LoadBalancer> loadBalancer;

}

