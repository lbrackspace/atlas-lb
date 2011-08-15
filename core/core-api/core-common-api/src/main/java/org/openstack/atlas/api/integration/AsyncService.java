package org.openstack.atlas.api.integration;

import org.openstack.atlas.service.domain.operation.Operation;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;

import javax.jms.JMSException;

public interface AsyncService {
    public void callAsyncLoadBalancingOperation(final Operation operation, final MessageDataContainer dataContainer) throws JMSException;
}
