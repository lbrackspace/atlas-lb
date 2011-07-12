package org.openstack.atlas.api.async;

import org.openstack.atlas.service.domain.entities.AccountUsage;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.VirtualIpType;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.UnauthorizedException;
import org.openstack.atlas.service.domain.repository.AccountUsageRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsageEvent;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageEventRepository;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.openstack.atlas.service.domain.events.UsageEvent.*;

public class UsageEventListener implements MessageListener {

    private final Log LOG = LogFactory.getLog(UsageEventListener.class);
    protected LoadBalancerUsageRepository usageRepository;
    protected LoadBalancerUsageEventRepository usageEventRepository;
    protected LoadBalancerRepository loadBalancerRepository;
    protected AccountUsageRepository accountUsageRepository;
    protected VirtualIpRepository virtualIpRepository;

    @Required
    public void setUsageRepository(LoadBalancerUsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    @Required
    public void setUsageEventRepository(LoadBalancerUsageEventRepository usageEventRepository) {
        this.usageEventRepository = usageEventRepository;
    }

    @Required
    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    @Required
    public void setAccountUsageRepository(AccountUsageRepository accountUsageRepository) {
        this.accountUsageRepository = accountUsageRepository;
    }

    @Required
    public void setVirtualIpRepository(VirtualIpRepository virtualIpRepository) {
        this.virtualIpRepository = virtualIpRepository;
    }

    public final void onMessage(Message message) {
        try {
            doOnMessage(message);
        } catch (UnauthorizedException ue) {
            System.err.println("Error processing message, " + ue);
            ue.printStackTrace();
        } catch (Exception e) {
            //ToDo: When in production log a cleaner message. But for now show the whole stack trace
            Log L = LogFactory.getLog(this.getClass());
            L.error(String.format("Error processing message In Class %s: %s ", this.getClass().getSimpleName(), getStackTrace(e)));
        }
    }

    public String getStackTrace(Exception ex) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("Exception: %s:%s\n", ex.getMessage(), ex.getClass().getName()));
        for (StackTraceElement se : ex.getStackTrace()) {
            sb.append(String.format("%s\n", se.toString()));
        }
        return sb.toString();
    }

    public void doOnMessage(Message message) throws Exception {
        LOG.info("Processing usage event...");
        Calendar eventTime = Calendar.getInstance();
        ObjectMessage object = (ObjectMessage) message;
        LoadBalancer loadBalancer = (LoadBalancer) object.getObject();
        String usageEventString = (String) message.getObjectProperty("usageEvent");
        UsageEvent usageEvent = UsageEvent.valueOf(usageEventString);

        List<LoadBalancerUsageEvent> newUsages = new ArrayList<LoadBalancerUsageEvent>();

        LoadBalancerUsageEvent newUsageEvent = new LoadBalancerUsageEvent();
        newUsageEvent.setAccountId(loadBalancer.getAccountId());
        newUsageEvent.setLoadbalancerId(loadBalancer.getId());
        newUsageEvent.setStartTime(eventTime);
        newUsageEvent.setNumVips(loadBalancer.getLoadBalancerJoinVipSet().size());
        newUsageEvent.setEventType(usageEvent.toString()); // TODO: Use cached values from database???

        newUsages.add(newUsageEvent);

        if (!newUsages.isEmpty()) usageEventRepository.batchCreate(newUsages);

        // If account specific event then create entry in account usage table
        if (usageEvent.equals(CREATE_LOADBALANCER) || usageEvent.equals(DELETE_LOADBALANCER) || usageEvent.equals(CREATE_VIRTUAL_IP) || usageEvent.equals(UsageEvent.DELETE_VIRTUAL_IP)) {
            createAccountUsageEntry(loadBalancer, eventTime);
        }

        LOG.info(String.format("'%s' usage event processed.", usageEvent.name()));
    }

    private void createAccountUsageEntry(LoadBalancer loadBalancer, Calendar eventTime) {
        Integer accountId = loadBalancer.getAccountId();
        AccountUsage usage = new AccountUsage();
        usage.setAccountId(accountId);
        usage.setStartTime(eventTime);
        usage.setNumLoadBalancers(loadBalancerRepository.getNumNonDeletedLoadBalancersForAccount(accountId));
        usage.setNumPublicVips(virtualIpRepository.getNumUniqueVipsForAccount(accountId, VirtualIpType.PUBLIC));
        usage.setNumServicenetVips(virtualIpRepository.getNumUniqueVipsForAccount(accountId, VirtualIpType.SERVICENET));
        accountUsageRepository.save(usage);
    }
}
