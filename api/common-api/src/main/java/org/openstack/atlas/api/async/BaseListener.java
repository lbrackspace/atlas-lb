package org.openstack.atlas.api.async;

import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.UnauthorizedException;
import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.pojos.Sync;
import org.openstack.atlas.service.domain.pojos.LBDeviceEvent;
import org.openstack.atlas.service.domain.services.HealthMonitorService;
import org.openstack.atlas.service.domain.services.HostService;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.*;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;

public abstract class BaseListener implements MessageListener {

    protected Log LOG = LogFactory.getLog(this.getClass());

    protected JmsTemplate jmsTemplate;

    protected LoadBalancerService loadBalancerService;

    protected VirtualIpService virtualIpService;

    protected NotificationService notificationService;

    protected HostService hostService;

    protected HealthMonitorService healthMonitorService;

    protected ConnectionThrottleService connectionThrottleService;

    protected ConnectionLoggingService connectionLoggingService;

    protected SessionPersistenceService sessionPersistenceService;

    protected AccessListService accessListService;

    protected RateLimitingService rateLimitingService;

    protected NodeService nodeService;

    protected ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void setLoadBalancerService(LoadBalancerService loadBalancerService) {
        this.loadBalancerService = loadBalancerService;
    }

    public void setVirtualIpService(VirtualIpService virtualIpService) {
        this.virtualIpService = virtualIpService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setHostService(HostService hostService) {
        this.hostService = hostService;
    }

    public void setHealthMonitorService(HealthMonitorService healthMonitorService) {
        this.healthMonitorService = healthMonitorService;
    }

    public void setConnectionThrottleService(ConnectionThrottleService connectionThrottleService) {
        this.connectionThrottleService = connectionThrottleService;
    }

    public void setConnectionLoggingService(ConnectionLoggingService connectionLoggingService) {
        this.connectionLoggingService = connectionLoggingService;
    }

    public void setSessionPersistenceService(SessionPersistenceService sessionPersistenceService) {
        this.sessionPersistenceService = sessionPersistenceService;
    }

    public void setRateLimitingService(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setReverseProxyLoadBalancerService(ReverseProxyLoadBalancerService reverseProxyLoadBalancerService) {
        this.reverseProxyLoadBalancerService = reverseProxyLoadBalancerService;
    }

    public final void onMessage(Message message) {
        try {
            doOnMessage(message);
        } catch (UnauthorizedException ue) {
            System.err.println("Error processing message, " + ue);
            ue.printStackTrace();
        } catch (Exception e) {
            //ToDo: When in production log a cleaner message. But for now show the whole stack trace
            System.out.println("Exception in BaseListener" + e.getMessage());
            Log L = LogFactory.getLog(this.getClass());
            L.error(String.format("Error processing message In Class %s: %s ", this.getClass().getSimpleName(), getStackTrace(e)));
            onRollback(message, e);
        }
    }

    public abstract void doOnMessage(Message message) throws Exception;

    protected void onRollback(final Message message, final Exception e) {
    }

    public String getStackTrace(Exception ex) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("Exception: %s:%s\n", ex.getMessage(), ex.getClass().getName()));
        for (StackTraceElement se : ex.getStackTrace()) {
            sb.append(String.format("%s\n", se.toString()));
        }
        return sb.toString();
    }

    protected Cluster getClusterFromMessage(Message message) throws JMSException {
        ObjectMessage object = (ObjectMessage) message;
        return (Cluster) object.getObject();
    }

    protected Host getHostFromMessage(Message message) throws JMSException {
        ObjectMessage object = (ObjectMessage) message;
        return (Host) object.getObject();
    }

    protected LoadBalancer getLoadbalancerFromMessage(Message message) throws JMSException {
        ObjectMessage object = (ObjectMessage) message;
        return (LoadBalancer) object.getObject();
    }

    protected Sync getSyncObjectFromMessage(Message message) throws JMSException {
        ObjectMessage object = (ObjectMessage) message;
        return (Sync) object.getObject();
    }

    protected EsbRequest getEsbRequestFromMessage(Message message) throws JMSException {
        ObjectMessage object = (ObjectMessage) message;
        EsbRequest esbRequest = (EsbRequest) object.getObject();
        return esbRequest;
    }

    protected MessageDataContainer getDataContainerFromMessage(Message message) throws JMSException {
        ObjectMessage object = (ObjectMessage) message;
        MessageDataContainer dataContainer = (MessageDataContainer) object.getObject();
        return dataContainer;
    }

    protected LBDeviceEvent getLBDeviceEventFromMessage(Message message) throws JMSException {
        ObjectMessage object = (ObjectMessage) message;
        LBDeviceEvent lbDeviceEvent = (LBDeviceEvent) object.getObject();
        return lbDeviceEvent;
    }

    protected void sendToRetryDestination(final Message message, final LoadBalancer loadBalancer) throws JMSException {
        final String operationName = (String) message.getObjectProperty("operationName");

        final String finalDestination = "lbOperation:" + "LOADBALANCER_RETRY";
        jmsTemplate.send(finalDestination, new MessageCreator() {

            public Message createMessage(Session session) throws JMSException {
                ObjectMessage response = session.createObjectMessage(loadBalancer);
                response.setJMSCorrelationID(message.getJMSCorrelationID());
                response.setObjectProperty("operationName", operationName);
                return response;
            }
        });
    }

    protected void notifyUsageProcessor(final Message message, final LoadBalancer loadBalancer, final UsageEvent event) throws JMSException {
        LOG.debug("Sending notification to usage processor...");
        final String finalDestination = "USAGE_EVENT";
        jmsTemplate.send(finalDestination, new MessageCreator() {

            public Message createMessage(Session session) throws JMSException {
                ObjectMessage response = session.createObjectMessage(loadBalancer);
                response.setJMSCorrelationID(message.getJMSCorrelationID());
                response.setObjectProperty("usageEvent", event.toString());
                return response;
            }
        });
    }

    public static String getId(String name,Object obj) {
        int hashcode;
        String hexOut;
        if(name==null){
            name = obj.getClass().getName();
        }
        if(obj==null) {
            return String.format("%s=null",name);
        }
        hashcode = System.identityHashCode(obj);
        hexOut = Integer.toHexString(hashcode);
        return String.format("%s=%s",name,hexOut);
    }

    /**
     * @param accessListService the accessListService to set
     */
    public void setAccessListService(AccessListService accessListService) {
        this.accessListService = accessListService;
    }
}
