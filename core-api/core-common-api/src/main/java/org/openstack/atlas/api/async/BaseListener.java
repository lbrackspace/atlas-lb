package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.*;

@Component
public abstract class BaseListener implements MessageListener {
    protected Log LOG = LogFactory.getLog(BaseListener.class);
    @Autowired
    protected JmsTemplate jmsTemplate;

    @Autowired
    protected ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;

    public final void onMessage(Message message) {
        try {
            doOnMessage(message);
        } catch (Exception e) {
            LOG.error("Error processing JMS message", e);
        }
    }

    public abstract void doOnMessage(Message message) throws Exception;

    protected MessageDataContainer getDataContainerFromMessage(Message message) throws JMSException {
        ObjectMessage object = (ObjectMessage) message;
        return (MessageDataContainer) object.getObject();
    }

    protected void notifyUsageProcessor(final Message message, final LoadBalancer loadBalancer, final String event) throws JMSException {
        LOG.debug("Sending notification to usage processor...");
        final String finalDestination = "USAGE_EVENT";
        jmsTemplate.send(finalDestination, new MessageCreator() {

            public Message createMessage(Session session) throws JMSException {
                ObjectMessage response = session.createObjectMessage(loadBalancer);
                response.setJMSCorrelationID(message.getJMSCorrelationID());
                response.setObjectProperty("usageEvent", event);
                return response;
            }
        });
    }
}
