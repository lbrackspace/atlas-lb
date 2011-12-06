package org.openstack.atlas.service.domain.entity;

import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Host.class)
public abstract class Host_ extends org.openstack.atlas.service.domain.entity.Entity_ {

	public static volatile SingularAttribute<Host, String> ipv6Public;
	public static volatile SingularAttribute<Host, String> username;
	public static volatile SingularAttribute<Host, String> ipv4Public;
	public static volatile SingularAttribute<Host, Boolean> endpointActive;
	public static volatile SingularAttribute<Host, String> ipv4ServiceNet;
	public static volatile SingularAttribute<Host, String> name;
	public static volatile SingularAttribute<Host, Cluster> cluster;
	public static volatile SingularAttribute<Host, HostStatus> hostStatus;
	public static volatile SingularAttribute<Host, String> password;
	public static volatile SingularAttribute<Host, String> ipv6ServiceNet;
	public static volatile SingularAttribute<Host, String> endpoint;
	public static volatile ListAttribute<Host, LoadBalancer> loadbalancers;

}

