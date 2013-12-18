package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.events.entities.*;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.helpers.AlertHelper;
import org.openstack.atlas.service.domain.services.helpers.AtomHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationServiceImpl extends BaseService implements NotificationService {

    @Transactional
    public void saveNodeEvent(String userName, Integer accountId, Integer loadbalancerId, Integer nodeId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        NodeEvent nE = AtomHelper.createNodeEvent(userName, accountId, loadbalancerId, nodeId, title, desc, eventType, category, severity);
        loadBalancerEventRepository.save(nE);

        LoadBalancerServiceEvent lsE = AtomHelper.createloadBalancerServiceEvent(userName, accountId, loadbalancerId, title, desc, eventType, category, severity, nodeId);
        loadBalancerEventRepository.save(lsE);
    }

    @Transactional
    public void saveNodeServiceEvent(String userName, Integer accountId, Integer loadbalancerId, Integer nodeId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity, String detailedMessage) {
        NodeServiceEvent nE = AtomHelper.createNodeServiceEvent(userName, accountId, loadbalancerId, nodeId, title, desc, eventType, category, severity, detailedMessage);
        loadBalancerEventRepository.save(nE);

        LoadBalancerServiceEvent lsE = AtomHelper.createloadBalancerServiceEvent(userName, accountId, loadbalancerId, title, desc, eventType, category, severity, nodeId);
        loadBalancerEventRepository.save(lsE);
    }

    @Transactional
    public void saveSslTerminationEvent(String userName, Integer accountId, Integer loadbalancerId, Integer sslTerminationId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        LoadBalancerServiceEvent lsE = AtomHelper.createloadBalancerSslTerminationEvent(userName, accountId, loadbalancerId, title, desc, eventType, category, severity, sslTerminationId);
        loadBalancerEventRepository.save(lsE);
    }


    public void saveAccessListEvent(String userName, Integer accountId, Integer loadbalancerId, Integer accessListId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        AccessListEvent nE = AtomHelper.createAccessListEvent(userName, accountId, loadbalancerId, accessListId, title, desc, eventType, category, severity);
        loadBalancerEventRepository.save(nE);

        LoadBalancerServiceEvent lsE = AtomHelper.createloadBalancerServiceEvent(userName, accountId, loadbalancerId, title, desc, eventType, category, severity, accessListId);
        loadBalancerEventRepository.save(lsE);
    }

    public void saveConnectionLimitEvent(String userName, Integer accountId, Integer loadbalancerId, Integer connectionLimitId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        ConnectionLimitEvent nE = AtomHelper.createConnectionLimitEvent(userName, accountId, loadbalancerId, connectionLimitId, title, desc, eventType, category, severity);
        loadBalancerEventRepository.save(nE);

        LoadBalancerServiceEvent lsE = AtomHelper.createloadBalancerServiceEvent(userName, accountId, loadbalancerId, title, desc, eventType, category, severity, connectionLimitId);
        loadBalancerEventRepository.save(lsE);
    }

    public void saveHealthMonitorEvent(String userName, Integer accountId, Integer loadbalancerId, Integer hmId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        HealthMonitorEvent nE = AtomHelper.createHealtheMonitorEvent(userName, accountId, loadbalancerId, hmId, title, desc, eventType, category, severity);
        loadBalancerEventRepository.save(nE);

        LoadBalancerServiceEvent lsE = AtomHelper.createloadBalancerServiceEvent(userName, accountId, loadbalancerId, title, desc, eventType, category, severity, hmId);
        loadBalancerEventRepository.save(lsE);
    }

    public void saveLoadBalancerEvent(String userName, Integer accountId, Integer loadbalancerId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        LoadBalancerEvent nE = AtomHelper.createLoadBalancerEvent(userName, accountId, loadbalancerId, title, desc, eventType, category, severity);
        loadBalancerEventRepository.save(nE);

        LoadBalancerServiceEvent lsE = AtomHelper.createloadBalancerServiceEvent(userName, accountId, loadbalancerId, title, desc, eventType, category, severity, null);
        loadBalancerEventRepository.save(lsE);
    }

    public void saveSessionPersistenceEvent(String userName, Integer accountId, Integer loadbalancerId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        SessionPersistenceEvent nE = AtomHelper.createSessionPersistenceEvent(userName, accountId, loadbalancerId, title, desc, eventType, category, severity);
        loadBalancerEventRepository.save(nE);

        LoadBalancerServiceEvent lsE = AtomHelper.createloadBalancerServiceEvent(userName, accountId, loadbalancerId, title, desc, eventType, category, severity, null);
        loadBalancerEventRepository.save(lsE);
    }

    public void saveVirtualIpEvent(String userName, Integer accountId, Integer loadbalancerId, Integer virtualIpId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        VirtualIpEvent nE = AtomHelper.createVirtualIpEvent(userName, accountId, loadbalancerId, virtualIpId, title, desc, eventType, category, severity);
        loadBalancerEventRepository.save(nE);

        LoadBalancerServiceEvent lsE = AtomHelper.createloadBalancerServiceEvent(userName, accountId, loadbalancerId, title, desc, eventType, category, severity, virtualIpId);
        loadBalancerEventRepository.save(lsE);
    }


    public void saveAlert(Integer accountId, Integer loadBalancerId, Exception e, String alertType, String msg) {
        //saving in alert for nimbus notification
        Alert alert = AlertHelper.createAlert(accountId, loadBalancerId, e, alertType, msg);
        alertRepository.save(alert);
    }

    public void saveAlert(Exception e, String alertType, String msg) {
        //saving in alert for nimbus notification
        Alert alert = AlertHelper.createAlert(e, alertType, msg);
        alertRepository.save(alert);
    }

    public void updateAlert(Alert dbAlert) {
        alertRepository.update(dbAlert);
    }

    public Alert getAlert(Integer alertId) throws Exception {
        return (alertRepository.getById(alertId));
    }

}
