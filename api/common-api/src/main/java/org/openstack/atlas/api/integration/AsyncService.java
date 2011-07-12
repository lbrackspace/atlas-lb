package org.openstack.atlas.api.integration;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;

import javax.jms.JMSException;

public interface AsyncService {
    @Deprecated // Start using other method
    public void callAsyncLoadBalancingOperation(final Operation operation, final LoadBalancer lb) throws JMSException;

    public void callAsyncLoadBalancingOperation(final Operation operation, final MessageDataContainer dataContainer) throws JMSException;
}
