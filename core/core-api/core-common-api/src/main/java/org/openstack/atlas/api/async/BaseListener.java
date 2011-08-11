package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.openstack.atlas.service.domain.entity.Cluster;
import org.openstack.atlas.service.domain.entity.Host;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.event.UsageEvent;
import org.openstack.atlas.service.domain.exception.UnauthorizedException;
import org.openstack.atlas.service.domain.pojo.LBDeviceEvent;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.service.domain.pojo.Sync;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;

public abstract class BaseListener implements MessageListener {

    protected Log LOG = LogFactory.getLog(this.getClass());

    protected JmsTemplate jmsTemplate;

    protected ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
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
            // TODO: When in production log a cleaner message. But for now show the whole stack trace
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
}
