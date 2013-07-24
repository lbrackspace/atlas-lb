package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.atom.EntryHelper;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.UsageEventCollectionException;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.usagerefactor.SnmpUsage;

import javax.jms.Message;
import java.util.*;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.UPDATE_SSL_TERMINATION;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;

public class UpdateSslTerminationListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(UpdateSslTerminationListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);

        MessageDataContainer dataContainer = getDataContainerFromMessage(message);
        ZeusSslTermination queTermination = dataContainer.getZeusSslTermination();
        LoadBalancer dbLoadBalancer = new LoadBalancer();

        try {
            LOG.debug("Grabbing loadbalancer...");
            dbLoadBalancer = loadBalancerService.get(dataContainer.getLoadBalancerId(), dataContainer.getAccountId());
            dbLoadBalancer.setUserName(dataContainer.getUserName());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", dataContainer.getLoadBalancerId());
            LOG.error(alertDescription, enfe);
            //OPS requested 11/07/12
//            notificationService.saveAlert(dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dbLoadBalancer);
            return;
        }

        //First pass
        List<SnmpUsage> usages = new ArrayList<SnmpUsage>();
        Map<Integer, SnmpUsage> usagesMap = new HashMap<Integer, SnmpUsage>();
        try {
            LOG.info(String.format("Collecting usage BEFORE ssl event for load balancer %s...", dbLoadBalancer.getId()));
            usages = usageEventCollection.getUsage(dbLoadBalancer);
            for (SnmpUsage usage : usages) {
                usagesMap.put(usage.getHostId(), usage);
            }
            LOG.info(String.format("Successfully collected usage BEFORE ssl event for load balancer %s", dbLoadBalancer.getId()));
        } catch (UsageEventCollectionException e) {
            LOG.error(String.format("Collection of the ssl usage event failed for " +
                    "load balancer: %s :: Exception: %s", dbLoadBalancer.getId(), e));
        }

        try {
            LOG.info("Updating load balancer ssl termination in Zeus...");
            reverseProxyLoadBalancerStmService.updateSslTermination(dbLoadBalancer, queTermination);
            LOG.debug("Successfully updated a load balancer ssl termination in Zeus.");
        } catch (Exception e) {
            dbLoadBalancer.setStatus(LoadBalancerStatus.ERROR);
            String alertDescription = String.format("An error occurred while creating loadbalancer ssl termination '%d' in Zeus.", dbLoadBalancer.getId());
            loadBalancerService.update(dbLoadBalancer);
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dbLoadBalancer);

            return;
        }

        //Second pass
        List<SnmpUsage> usages2 = new ArrayList<SnmpUsage>();
        Map<Integer, SnmpUsage> usagesMap2 = new HashMap<Integer, SnmpUsage>();
        try {
            LOG.info(String.format("Collecting usage AFTER ssl event for load balancer %s...", dbLoadBalancer.getId()));
            usages2 = usageEventCollection.getUsage(dbLoadBalancer);
            for (SnmpUsage usage : usages2) {
                usagesMap2.put(usage.getHostId(), usage);
            }
            LOG.info(String.format("Successfully collected usage AFTER ssl event for load balancer %s", dbLoadBalancer.getId()));
        } catch (UsageEventCollectionException e) {
            LOG.error(String.format("Collection of the ssl usage event failed for " +
                    "load balancer: %s :: Exception: %s", dbLoadBalancer.getId(), e));
        }

        //In case the first pass missed a host
        for (Integer hostId : usagesMap2.keySet()) {
            if (!usagesMap.containsKey(hostId)) {
                usagesMap.put(hostId, usagesMap2.get(hostId));
            }
        }

        //Combine first pass and second pass
        for (Integer hostId : usagesMap.keySet()) {
            SnmpUsage usage1 = usagesMap.get(hostId);
            SnmpUsage usage2 = usagesMap2.get(hostId);
            if (usage1.getBytesIn() < usage2.getBytesIn()) {
                usage1.setBytesIn(usage2.getBytesIn());
            }
            if (usage1.getBytesInSsl() < usage2.getBytesInSsl()) {
                usage1.setBytesInSsl(usage2.getBytesInSsl());
            }
            if (usage1.getBytesOut() < usage2.getBytesOut()) {
                usage1.setBytesOut(usage2.getBytesOut());
            }
            if (usage1.getBytesOutSsl() < usage2.getBytesOutSsl()) {
                usage1.setBytesOutSsl(usage2.getBytesOutSsl());
            }
            if (usage1.getConcurrentConnections() < usage2.getConcurrentConnections()) {
                usage1.setConcurrentConnections(usage2.getConcurrentConnections());
            }
            if (usage1.getConcurrentConnectionsSsl() < usage2.getConcurrentConnectionsSsl()) {
                usage1.setConcurrentConnectionsSsl(usage2.getConcurrentConnectionsSsl());
            }
        }

        Calendar eventTime = Calendar.getInstance();
        // Notify usage processor
        if (queTermination.getSslTermination().isEnabled()) {
            LOG.debug(String.format("SSL Termination is enabled for load balancer: %s", dbLoadBalancer.getId()));
            if (queTermination.getSslTermination().isSecureTrafficOnly()) {
                LOG.debug(String.format("SSL Termination is Secure Traffic Only for load balancer: %s", dbLoadBalancer.getId()));
                usageEventCollection.processUsageEvent(usages, dbLoadBalancer, UsageEvent.SSL_ONLY_ON, eventTime);
            } else {
                LOG.debug(String.format("SSL Termination is Mixed Traffic for load balancer: %s", dbLoadBalancer.getId()));
                usageEventCollection.processUsageEvent(usages, dbLoadBalancer, UsageEvent.SSL_MIXED_ON, eventTime);
            }
        } else {
            LOG.debug(String.format("SSL Termination is NOT Enabled for load balancer: %s", dbLoadBalancer.getId()));
            usageEventCollection.processUsageEvent(usages, dbLoadBalancer, UsageEvent.SSL_OFF, eventTime);
        }
        LOG.info(String.format("Finished processing usage event for load balancer: %s", dbLoadBalancer.getId()));

        // Update load balancer status in DB
        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);

        addAtomEntriesForSslTermination(dbLoadBalancer, dbLoadBalancer.getSslTermination());

        LOG.info(String.format("Updated load balancer '%d' ssl termination successfully for loadbalancer: ", dbLoadBalancer.getId()));
    }

    private void addAtomEntriesForSslTermination(LoadBalancer dbLoadBalancer, SslTermination sslTermination) {
        notificationService.saveSslTerminationEvent(dbLoadBalancer.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), sslTermination.getId(), "UPDATE_SSL_TERMINATION", EntryHelper.createSslTerminationSummary(sslTermination), UPDATE_SSL_TERMINATION, UPDATE, INFO);
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Updating Load Balancer SSL Termination";
        String desc = "Could not update a load balancer SSL Termination at this time";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, UPDATE_SSL_TERMINATION, UPDATE, CRITICAL);
    }
}
