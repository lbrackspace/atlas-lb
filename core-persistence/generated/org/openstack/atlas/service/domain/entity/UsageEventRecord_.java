package org.openstack.atlas.service.domain.entity;

import java.util.Calendar;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(UsageEventRecord.class)
public abstract class UsageEventRecord_ extends org.openstack.atlas.service.domain.entity.Entity_ {

	public static volatile SingularAttribute<UsageEventRecord, Calendar> startTime;
	public static volatile SingularAttribute<UsageEventRecord, LoadBalancer> loadBalancer;
	public static volatile SingularAttribute<UsageEventRecord, String> event;

}

