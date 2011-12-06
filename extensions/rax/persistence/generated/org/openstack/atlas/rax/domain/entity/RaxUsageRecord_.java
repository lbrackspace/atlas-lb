package org.openstack.atlas.rax.domain.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(RaxUsageRecord.class)
public abstract class RaxUsageRecord_ extends org.openstack.atlas.service.domain.entity.UsageRecord_ {

	public static volatile SingularAttribute<RaxUsageRecord, Double> averageConcurrentConnections;
	public static volatile SingularAttribute<RaxUsageRecord, Integer> numberOfPolls;

}

