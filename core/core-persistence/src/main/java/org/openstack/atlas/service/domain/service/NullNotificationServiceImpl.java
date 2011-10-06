package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.event.entity.Alert;
import org.openstack.atlas.service.domain.event.entity.CategoryType;
import org.openstack.atlas.service.domain.event.entity.EventSeverity;
import org.openstack.atlas.service.domain.event.entity.EventType;
import org.springframework.stereotype.Service;

@Service
public class NullNotificationServiceImpl implements NotificationService {
    @Override
    public void saveNodeEvent(String userName, Integer accountId, Integer loadbalancerId, Integer nodeId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void saveAccessListEvent(String userName, Integer accountId, Integer loadbalancerId, Integer accessListId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void saveConnectionThrottleEvent(String userName, Integer accountId, Integer loadbalancerId, Integer connectionThrottleId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void saveHealthMonitorEvent(String userName, Integer accountId, Integer loadbalancerId, Integer hmId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void saveLoadBalancerEvent(String userName, Integer accountId, Integer loadbalancerId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void saveSessionPersistenceEvent(String userName, Integer accountId, Integer loadbalancerId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void saveVirtualIpEvent(String userName, Integer accountId, Integer loadbalancerId, Integer virtualIpId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void saveAlert(Integer accountId, Integer loadBalancerId, Exception e, String alertType, String msg) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void saveAlert(Exception e, String alertType, String msg) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateAlert(Alert dbAlert) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Alert getAlert(Integer alertId) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
