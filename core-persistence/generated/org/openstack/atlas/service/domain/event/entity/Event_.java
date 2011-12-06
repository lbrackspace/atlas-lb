package org.openstack.atlas.service.domain.event.entity;

import java.util.Calendar;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Event.class)
public abstract class Event_ {

	public static volatile SingularAttribute<Event, Integer> id;
	public static volatile SingularAttribute<Event, String> author;
	public static volatile SingularAttribute<Event, CategoryType> category;
	public static volatile SingularAttribute<Event, String> title;
	public static volatile SingularAttribute<Event, Integer> accountId;
	public static volatile SingularAttribute<Event, Calendar> created;
	public static volatile SingularAttribute<Event, String> description;
	public static volatile SingularAttribute<Event, Integer> loadbalancerId;
	public static volatile SingularAttribute<Event, String> relativeUri;
	public static volatile SingularAttribute<Event, EventSeverity> severity;
	public static volatile SingularAttribute<Event, EventType> type;

}

