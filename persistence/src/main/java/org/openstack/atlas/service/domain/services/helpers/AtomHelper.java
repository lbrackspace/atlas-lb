package org.openstack.atlas.service.domain.services.helpers;

import org.openstack.atlas.service.domain.events.entities.*;

import java.util.Calendar;

import static org.openstack.atlas.service.domain.events.entities.EventType.*;

public class AtomHelper {


    public static LoadBalancerServiceEvent createloadBalancerServiceEvent(String userName, Integer accountId, Integer loadbalancerId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity, Integer additionalId) {
        LoadBalancerServiceEvent lse = new LoadBalancerServiceEvent();
        lse.setAccountId(accountId);
        lse.setLoadbalancerId(loadbalancerId);
        lse.setAuthor(userName);
        lse.setCreated(Calendar.getInstance());
        lse.setTitle(title);
        lse.setDescription(desc);
        lse.setCategory(category);
        lse.setSeverity(severity);
        lse.setType(eventType);
        lse.setRelativeUri(createRelativeUri(accountId, loadbalancerId, eventType, additionalId));
        return lse;
    }

    public static LoadBalancerServiceEvent createloadBalancerSslTerminationEvent(String userName, Integer accountId, Integer loadbalancerId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity, Integer additionalId) {
        LoadBalancerServiceEvent lse = new LoadBalancerServiceEvent();
        lse.setAccountId(accountId);
        lse.setLoadbalancerId(loadbalancerId);
        lse.setAuthor(userName);
        lse.setCreated(Calendar.getInstance());
        lse.setTitle(title);
        lse.setDescription(desc);
        lse.setCategory(category);
        lse.setSeverity(severity);
        lse.setType(eventType);
        lse.setRelativeUri(createRelativeUri(accountId, loadbalancerId, eventType, additionalId));
        return lse;
    }

    public static NodeEvent createNodeEvent(String userName, Integer accountId, Integer loadbalancerId, Integer nodeId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        NodeEvent ne = new NodeEvent();
        ne.setAccountId(accountId);
        ne.setLoadbalancerId(loadbalancerId);
        ne.setAuthor(userName);
        ne.setNodeId(nodeId);
        ne.setCreated(Calendar.getInstance());
        ne.setTitle(title);
        ne.setDescription(desc);
        ne.setCategory(category);
        ne.setSeverity(severity);
        ne.setType(eventType);
        ne.setRelativeUri(createRelativeUri(accountId, loadbalancerId, eventType, nodeId));
        return ne;
    }

    public static NodeServiceEvent createNodeServiceEvent(String userName, Integer accountId, Integer loadbalancerId, Integer nodeId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity, String detailedMessage) {
        NodeServiceEvent ne = new NodeServiceEvent();
        ne.setAccountId(accountId);
        ne.setLoadbalancerId(loadbalancerId);
        ne.setAuthor(userName);
        ne.setNodeId(nodeId);
        ne.setCreated(Calendar.getInstance());
        ne.setTitle(title);
        ne.setDescription(desc);
        ne.setCategory(category);
        ne.setSeverity(severity);
        ne.setType(eventType);
        ne.setDetailedMessage(detailedMessage);
        ne.setRelativeUri(createRelativeUri(accountId, loadbalancerId, eventType, nodeId) + "/events");
        return ne;
    }

    public static AccessListEvent createAccessListEvent(String userName, Integer accountId, Integer loadbalancerId, Integer accessListId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        AccessListEvent lse = new AccessListEvent();
        lse.setAccountId(accountId);
        lse.setLoadbalancerId(loadbalancerId);
        lse.setAuthor(userName);
        lse.setAccess_list_id(accessListId);
        lse.setCreated(Calendar.getInstance());
        lse.setTitle(title);
        lse.setDescription(desc);
        lse.setCategory(category);
        lse.setSeverity(severity);
        lse.setType(eventType);
        lse.setRelativeUri(createRelativeUri(accountId, loadbalancerId, eventType, accessListId));

        return lse;
    }

    public static ConnectionLimitEvent createConnectionLimitEvent(String userName, Integer accountId, Integer loadbalancerId, Integer connectLimitId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        ConnectionLimitEvent lse = new ConnectionLimitEvent();
        lse.setAccountId(accountId);
        lse.setLoadbalancerId(loadbalancerId);
        lse.setAuthor(userName);
        lse.setConnectionLimitId(connectLimitId);
        lse.setCreated(Calendar.getInstance());
        lse.setTitle(title);
        lse.setDescription(desc);
        lse.setCategory(category);
        lse.setSeverity(severity);
        lse.setType(eventType);
        lse.setRelativeUri(createRelativeUri(accountId, loadbalancerId, eventType, connectLimitId));
        return lse;
    }

    public static HealthMonitorEvent createHealtheMonitorEvent(String userName, Integer accountId, Integer loadbalancerId, Integer hmId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        HealthMonitorEvent lse = new HealthMonitorEvent();
        lse.setAccountId(accountId);
        lse.setLoadbalancerId(loadbalancerId);
        lse.setAuthor(userName);
        lse.setHealthMonitorId(hmId);
        lse.setCreated(Calendar.getInstance());
        lse.setTitle(title);
        lse.setDescription(desc);
        lse.setCategory(category);
        lse.setSeverity(severity);
        lse.setType(eventType);
        lse.setRelativeUri(createRelativeUri(accountId, loadbalancerId, eventType, hmId));

        return lse;
    }

    public static LoadBalancerEvent createLoadBalancerEvent(String userName, Integer accountId, Integer loadbalancerId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        LoadBalancerEvent lse = new LoadBalancerEvent();
        lse.setAccountId(accountId);
        lse.setLoadbalancerId(loadbalancerId);
        lse.setAuthor(userName);
        lse.setCreated(Calendar.getInstance());
        lse.setTitle(title);
        lse.setDescription(desc);
        lse.setCategory(category);
        lse.setSeverity(severity);
        lse.setType(eventType);
        lse.setRelativeUri(createRelativeUri(accountId, loadbalancerId, eventType, null));

        return lse;
    }

    public static SessionPersistenceEvent createSessionPersistenceEvent(String userName, Integer accountId, Integer loadbalancerId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        SessionPersistenceEvent lse = new SessionPersistenceEvent();
        lse.setAccountId(accountId);
        lse.setLoadbalancerId(loadbalancerId);
        lse.setAuthor(userName);
        lse.setCreated(Calendar.getInstance());
        lse.setTitle(title);
        lse.setDescription(desc);
        lse.setCategory(category);
        lse.setSeverity(severity);
        lse.setType(eventType);
        lse.setRelativeUri(createRelativeUri(accountId, loadbalancerId, eventType, null));

        return lse;
    }

    public static VirtualIpEvent createVirtualIpEvent(String userName, Integer accountId, Integer loadbalancerId, Integer vipId, String title, String desc, EventType eventType, CategoryType category, EventSeverity severity) {
        VirtualIpEvent lse = new VirtualIpEvent();
        lse.setAccountId(accountId);
        lse.setLoadbalancerId(loadbalancerId);
        lse.setAuthor(userName);
        lse.setCreated(Calendar.getInstance());
        lse.setVirtualIpId(vipId);
        lse.setTitle(title);
        lse.setDescription(desc);
        lse.setCategory(category);
        lse.setSeverity(severity);
        lse.setType(eventType);
        lse.setRelativeUri(createRelativeUri(accountId, loadbalancerId, eventType, vipId));

        return lse;
    }

    public static String createRelativeUri(Integer accountId, Integer loadblancerId, EventType eventType, Integer additionalId) {
        StringBuilder urI = new StringBuilder("/" + accountId + "/loadbalancers/" + loadblancerId);

        if (eventType.equals(CREATE_ACCESS_LIST) || eventType.equals(UPDATE_ACCESS_LIST) || eventType.equals(DELETE_ACCESS_LIST) || eventType.equals(DELETE_NETWORK_ITEM)) {
            urI.append("/accesslist/");
        } else if (eventType.equals(EventType.CREATE_CONNECTION_THROTTLE) || eventType.equals(EventType.UPDATE_CONNECTION_THROTTLE) || eventType.equals(EventType.DELETE_CONNECTION_THROTTLE)) {
            urI.append("/connectionthrottle/");
        } else if (eventType.equals(EventType.CREATE_HEALTH_MONITOR) || eventType.equals(EventType.UPDATE_HEALTH_MONITOR) || eventType.equals(EventType.DELETE_HEALTH_MONITOR)) {
            urI.append("/healthmonitor/");
        } else if (eventType.equals(EventType.CREATE_NODE) || eventType.equals(EventType.UPDATE_NODE) || eventType.equals(EventType.DELETE_NODE)) {
            urI.append("/nodes/").append(additionalId);
        } else if (eventType.equals(EventType.CREATE_SESSION_PERSISTENCE) || eventType.equals(EventType.UPDATE_SESSION_PERSISTENCE) || eventType.equals(EventType.DELETE_SESSION_PERSISTENCE)) {
            urI.append("/sessionpersistence");
        } else if (eventType.equals(CREATE_VIRTUAL_IP) || eventType.equals(DELETE_VIRTUAL_IP)) {
            urI.append("/virtualips/").append(additionalId);
        }

        return urI.toString();
    }

}
