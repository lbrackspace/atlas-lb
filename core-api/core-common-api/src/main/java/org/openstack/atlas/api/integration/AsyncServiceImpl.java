package org.openstack.atlas.api.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

@Service
public class AsyncServiceImpl implements AsyncService {
    private final Log LOG = LogFactory.getLog(AsyncServiceImpl.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @Override
    public void callAsyncLoadBalancingOperation(String operation, final MessageDataContainer dataContainer) throws JMSException {
        LOG.debug(String.format("Sending message to '%s' queue...", operation));
        jmsTemplate.send(operation, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createObjectMessage(dataContainer);
            }
        });
    }
}

