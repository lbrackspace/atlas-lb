package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.event.entity.Alert;
import org.openstack.atlas.service.domain.event.entity.CategoryType;
import org.openstack.atlas.service.domain.event.entity.EventSeverity;
import org.openstack.atlas.service.domain.event.entity.EventType;

public interface NotificationService {

    void saveNodeEvent(String userName, Integer accountId, Integer loadbalancerId, Integer nodeId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity);

    void saveAccessListEvent(String userName, Integer accountId, Integer loadbalancerId, Integer accessListId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity);

    void saveConnectionThrottleEvent(String userName, Integer accountId, Integer loadbalancerId, Integer connectionThrottleId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity);

    void saveHealthMonitorEvent(String userName, Integer accountId, Integer loadbalancerId, Integer hmId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity);

    void saveLoadBalancerEvent(String userName, Integer accountId, Integer loadbalancerId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity);

    void saveSessionPersistenceEvent(String userName, Integer accountId, Integer loadbalancerId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity);

    void saveVirtualIpEvent(String userName, Integer accountId, Integer loadbalancerId, Integer virtualIpId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity);

    void saveAlert(Integer accountId, Integer loadBalancerId, Exception e, String alertType, String msg);

    void saveAlert(Exception e, String alertType, String msg);

    void updateAlert(Alert dbAlert);

    Alert getAlert(Integer alertId) throws Exception;
}
