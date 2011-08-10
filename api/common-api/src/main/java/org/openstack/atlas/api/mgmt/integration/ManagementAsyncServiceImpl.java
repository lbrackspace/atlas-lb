package org.openstack.atlas.api.mgmt.integration;

import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

public class ManagementAsyncServiceImpl implements ManagementAsyncService {
    private final Log LOG = LogFactory.getLog(ManagementAsyncServiceImpl.class);
    private JmsTemplate jmsTemplate;

    @Override
    public void callAsyncLoadBalancingOperation(final Operation operation, final EsbRequest esbRequest) throws JMSException {
        LOG.debug(String.format("Sending message to '%s' queue...", operation.name()));
        jmsTemplate.send(operation.name(), new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createObjectMessage(esbRequest);
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
