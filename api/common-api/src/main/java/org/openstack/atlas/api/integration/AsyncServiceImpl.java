package org.openstack.atlas.api.integration;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

public class AsyncServiceImpl implements AsyncService {
    private final Log LOG = LogFactory.getLog(AsyncServiceImpl.class);
    private JmsTemplate jmsTemplate;

    @Override
    public void callAsyncLoadBalancingOperation(final Operation operation, final LoadBalancer lb) throws JMSException {
        LOG.debug(String.format("Sending message to '%s' queue...", operation.name()));
        jmsTemplate.send(operation.name(), new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createObjectMessage(lb);
            }
        });
    }

    @Override
    public void callAsyncLoadBalancingOperation(Operation operation, final MessageDataContainer dataContainer) throws JMSException {
        LOG.debug(String.format("Sending message to '%s' queue...", operation.name()));
        jmsTemplate.send(operation.name(), new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createObjectMessage(dataContainer);
            }
        });
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }
}
