package org.openstack.atlas.service.domain.entity;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Cluster.class)
public abstract class Cluster_ extends org.openstack.atlas.service.domain.entity.Entity_ {

	public static volatile SingularAttribute<Cluster, String> username;
	public static volatile SingularAttribute<Cluster, String> description;
	public static volatile SingularAttribute<Cluster, String> name;
	public static volatile SingularAttribute<Cluster, String> clusterIpv6Cidr;
	public static volatile SetAttribute<Cluster, VirtualIp> virtualIps;
	public static volatile SingularAttribute<Cluster, String> password;

}

