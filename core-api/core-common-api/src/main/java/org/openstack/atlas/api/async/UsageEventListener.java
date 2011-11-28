package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.UsageEventRecord;
import org.openstack.atlas.service.domain.repository.UsageEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Component
public class UsageEventListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(UsageEventListener.class);

    @Autowired
    protected UsageEventRepository usageEventRepository;

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.info("Processing usage event...");
        Calendar eventTime = Calendar.getInstance();
        ObjectMessage object = (ObjectMessage) message;
        LoadBalancer loadBalancer = (LoadBalancer) object.getObject();
        String usageEventType = (String) message.getObjectProperty("usageEvent");

        List<UsageEventRecord> newUsages = new ArrayList<UsageEventRecord>();

        UsageEventRecord newUsageEvent = new UsageEventRecord();
        newUsageEvent.setLoadBalancer(loadBalancer);
        newUsageEvent.setStartTime(eventTime);
        newUsageEvent.setEvent(usageEventType);
        newUsages.add(newUsageEvent);

        if (!newUsages.isEmpty()) usageEventRepository.batchCreate(newUsages);
        LOG.info(String.format("'%s' usage event processed for load balancer '%d'.", usageEventType, loadBalancer.getId()));
    }
}