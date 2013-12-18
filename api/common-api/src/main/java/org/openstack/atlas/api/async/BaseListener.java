package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.UnauthorizedException;
import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.pojos.Sync;
import org.openstack.atlas.service.domain.pojos.ZeusEvent;
import org.openstack.atlas.service.domain.services.*;
import org.openstack.atlas.usagerefactor.collection.UsageEventCollection;
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
    protected ContentCachingService contentCachingService;
    protected SessionPersistenceService sessionPersistenceService;
    protected AccessListService accessListService;
    protected RateLimitingService rateLimitingService;
    protected NodeService nodeService;
    protected SslTerminationService sslTerminationService;
    protected LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;
    protected ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;
    protected UsageEventHelper usageEventHelper;
//    protected UsageEventProcessor usageEventProcessor;
    protected UsageEventCollection usageEventCollection;

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

    public void setContentCachingService(ContentCachingService contentCachingService) {
        this.contentCachingService = contentCachingService;
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

    public void setSslTerminationService(SslTerminationService sslTerminationService) {
        this.sslTerminationService = sslTerminationService;
    }

    public void setUsageEventHelper(UsageEventHelper usageEventHelper) {
        this.usageEventHelper = usageEventHelper;
    }

    public void setLoadBalancerStatusHistoryService(LoadBalancerStatusHistoryService loadBalancerStatusHistoryService) {
        this.loadBalancerStatusHistoryService = loadBalancerStatusHistoryService;
    }

//    public void setUsageEventProcessor(UsageEventProcessor usageEventProcessor) {
//        this.usageEventProcessor = usageEventProcessor;
//    }

    public void setUsageEventCollection(UsageEventCollection usageEventCollection) {
        this.usageEventCollection = usageEventCollection;
    }

    public final void onMessage(Message message) {
        try {
            doOnMessage(message);
        } catch (UnauthorizedException ue) {
            System.err.println("Error processing message, " + ue);
            ue.printStackTrace();
        } catch (Exception e) {
            //ToDo: When in production log a cleaner message. But for now show the whole stack trace
//            System.out.println("Exception in BaseListener" + e.getMessage());
            LOG.error(getStackTrace(e));
            Log L = LogFactory.getLog(this.getClass());
            L.error(String.format("Error processing message In Class %s: %s ", this.getClass().getSimpleName(), getStackTrace(e)));
            onRollback(message, e);
        }
    }

    public abstract void doOnMessage(Message message) throws Exception;

    protected void onRollback(final Message message, final Exception e) {
    }

    public String getStackTrace(Exception ex) {
        StringBuilder sb = new StringBuilder();
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

    protected ZeusEvent getZeusEventFromMessage(Message message) throws JMSException {
        ObjectMessage object = (ObjectMessage) message;
        ZeusEvent zeusEvent = (ZeusEvent) object.getObject();
        return zeusEvent;
    }

    protected void sendToRetryDestination(final Message message, final LoadBalancer loadBalancer) throws JMSException {
        final String operationName = (String) message.getObjectProperty("operationName");

        final String finalDestination = "lbOperation:" + "LOADBALANCER_RETRY";
        jmsTemplate.send(finalDestination, new MessageCreator() {

            @Override
            public Message createMessage(Session session) throws JMSException {
                ObjectMessage response = session.createObjectMessage(loadBalancer);
                response.setJMSCorrelationID(message.getJMSCorrelationID());
                response.setObjectProperty("operationName", operationName);
                return response;
            }
        });
    }

/*    protected void notifyUsageProcessor(final Message message, final LoadBalancer loadBalancer, final UsageEvent event) throws JMSException {
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
    }*/

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
