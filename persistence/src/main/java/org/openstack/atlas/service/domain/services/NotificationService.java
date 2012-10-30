package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.events.entities.*;

public interface NotificationService {


    public void saveNodeEvent(String userName, Integer accountId, Integer loadbalancerId, Integer nodeId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity);

    public void saveNodeServiceEvent(String userName, Integer accountId, Integer loadbalancerId, Integer nodeId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity, String detailedMessage);

    public void saveSslTerminationEvent(String userName, Integer accountId, Integer loadbalancerId, Integer sslTerminationId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity);


    public void saveAccessListEvent(String userName, Integer accountId, Integer loadbalancerId, Integer accessListId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity);


    public void saveConnectionLimitEvent(String userName, Integer accountId, Integer loadbalancerId, Integer connectionLimitId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity);

    public void saveHealthMonitorEvent(String userName, Integer accountId, Integer loadbalancerId, Integer hmId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity);

    public void saveLoadBalancerEvent(String userName, Integer accountId, Integer loadbalancerId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity);


    public void saveSessionPersistenceEvent(String userName, Integer accountId, Integer loadbalancerId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity);


    public void saveVirtualIpEvent(String userName, Integer accountId, Integer loadbalancerId, Integer virtualIpId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity);

    public void saveAlert(Integer accountId, Integer loadBalancerId, Exception e, String alertType, String msg);


    public void saveAlert(Exception e, String alertType, String msg);


    public void updateAlert(Alert dbAlert);


    public Alert getAlert(Integer alertId) throws Exception;


}
