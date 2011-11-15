package org.openstack.atlas.api.integration;

import org.openstack.atlas.service.domain.pojo.MessageDataContainer;

import javax.jms.JMSException;

public interface AsyncService {
    public void callAsyncLoadBalancingOperation(final String operation, final MessageDataContainer dataContainer) throws JMSException;
}
