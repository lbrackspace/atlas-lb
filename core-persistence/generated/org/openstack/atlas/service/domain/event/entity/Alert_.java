package org.openstack.atlas.service.domain.event.entity;

import java.util.Calendar;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Alert.class)
public abstract class Alert_ {

	public static volatile SingularAttribute<Alert, String> message;
	public static volatile SingularAttribute<Alert, Integer> id;
	public static volatile SingularAttribute<Alert, Integer> accountId;
	public static volatile SingularAttribute<Alert, Calendar> created;
	public static volatile SingularAttribute<Alert, AlertStatus> status;
	public static volatile SingularAttribute<Alert, Integer> loadbalancerId;
	public static volatile SingularAttribute<Alert, String> alertType;
	public static volatile SingularAttribute<Alert, String> messageName;

}

