package org.openstack.atlas.service.domain.entity;

import java.util.Calendar;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(UsageRecord.class)
public abstract class UsageRecord_ extends org.openstack.atlas.service.domain.entity.Entity_ {

	public static volatile SingularAttribute<UsageRecord, Calendar> startTime;
	public static volatile SingularAttribute<UsageRecord, Long> transferBytesOut;
	public static volatile SingularAttribute<UsageRecord, Long> lastBytesOutCount;
	public static volatile SingularAttribute<UsageRecord, Long> lastBytesInCount;
	public static volatile SingularAttribute<UsageRecord, LoadBalancer> loadBalancer;
	public static volatile SingularAttribute<UsageRecord, String> event;
	public static volatile SingularAttribute<UsageRecord, Long> transferBytesIn;
	public static volatile SingularAttribute<UsageRecord, Calendar> endTime;

}

