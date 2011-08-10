package org.openstack.atlas.api.mgmt.integration;

import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;

import javax.jms.JMSException;

public interface ManagementAsyncService {
    @Deprecated // Start using other method
    public void callAsyncLoadBalancingOperation(final Operation operation, final EsbRequest esbRequest) throws JMSException;

    public void callAsyncLoadBalancingOperation(final Operation operation, final MessageDataContainer dataContainer) throws JMSException;
}

