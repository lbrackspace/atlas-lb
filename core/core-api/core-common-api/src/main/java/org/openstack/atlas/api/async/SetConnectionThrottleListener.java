package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;

@Component
public class SetConnectionThrottleListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(SetConnectionThrottleListener.class);

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;
    @Autowired
    private NotificationService notificationService;

    @Override
    public void doOnMessage(Message message) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
        LOG.debug("Connection Throttle async call called");
    }
}
