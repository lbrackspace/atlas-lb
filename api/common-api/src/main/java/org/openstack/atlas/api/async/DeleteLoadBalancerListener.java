package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.UsageEventCollectionException;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.util.debug.Debug;

import javax.jms.Message;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.DELETE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.DELETE_LOADBALANCER;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.USAGE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;

public class DeleteLoadBalancerListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(DeleteLoadBalancerListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);

        LoadBalancer queueLb = getLoadbalancerFromMessage(message);
        LoadBalancer dbLoadBalancer;

        try {
            dbLoadBalancer = loadBalancerService.getWithUserPages(queueLb.getId(), queueLb.getAccountId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", queueLb.getId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(queueLb.getAccountId(), queueLb.getId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

        List<SnmpUsage> usages = new ArrayList<SnmpUsage>();
        try {
            LOG.info(String.format("Collecting DELETE_LOADBALANCER usage for load balancer %s...", dbLoadBalancer.getId()));
            usages = usageEventCollection.getUsage(dbLoadBalancer);
            LOG.info(String.format("Successfully collected DELETE_LOADBALANCER usage for load balancer %s", dbLoadBalancer.getId()));
        } catch (UsageEventCollectionException e) {
            LOG.error(String.format("Collection of the DELETE_LOADBALANCER usage event failed for " +
                    "load balancer: %s :: Exception: %s", dbLoadBalancer.getId(), e));
        }

        // We don't want to remove shared vip vTM references...
        Set<LoadBalancerJoinVip> vips = dbLoadBalancer.getLoadBalancerJoinVipSet();
        Set<LoadBalancerJoinVip6> vips6 = dbLoadBalancer.getLoadBalancerJoinVip6Set();

        // Verify and remove shared vips from the lists to be purged
        for (LoadBalancerJoinVip vipset : vips) {
            if (loadBalancerService.isSharedVip4(dbLoadBalancer, vipset.getVirtualIp())) {
                vips.remove(vipset);
            }
        }

        for (LoadBalancerJoinVip6 vipset : vips6) {
            if (loadBalancerService.isSharedVip6(dbLoadBalancer, vipset.getVirtualIp())) {
                vips6.remove(vipset);
            }
        }

        // Process vTM deletion...
        try {
            if (isRestAdapter()) {
                LOG.debug(String.format("Deleting load balancer '%d' in backend...", dbLoadBalancer.getId()));
                reverseProxyLoadBalancerVTMService.deleteLoadBalancer(dbLoadBalancer);
                LOG.debug(String.format("Successfully deleted load balancer '%d' in backend.", dbLoadBalancer.getId()));
            } else {
                LOG.debug(String.format("Deleting load balancer '%d' in ZXTM...", dbLoadBalancer.getId()));
                reverseProxyLoadBalancerService.deleteLoadBalancer(dbLoadBalancer);
                LOG.debug(String.format("Successfully deleted load balancer '%d' in Zeus.", dbLoadBalancer.getId()));
            }
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            LOG.error(String.format("LoadBalancer status before error was: '%s'", dbLoadBalancer.getStatus()));
            String alertDescription = String.format("Error deleting loadbalancer '%d' in Zeus.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);

            //Do not store usage here because the load balancer went into ERROR status and thus is not really deleted.
            return;
        }

        // Ensure SSLTermination references are removed from the database
        if (dbLoadBalancer.hasSsl()) {
            LOG.debug(String.format("Deleting load balancer '%d' ssl termination in database...", dbLoadBalancer.getId()));
            sslTerminationService.deleteSslTermination(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId());
            LOG.debug(String.format("Successfully deleted load balancer ssl termination '%d' in database.", dbLoadBalancer.getId()));
        }

        // Mark the load balancer in a pseudo DELETED status for retention policy
        dbLoadBalancer = loadBalancerService.pseudoDelete(dbLoadBalancer);
        loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.DELETED);

        // Add atom entry
        String atomTitle = "Load Balancer Successfully Deleted";
        String atomSummary = "Load balancer successfully deleted";
        notificationService.saveLoadBalancerEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, DELETE_LOADBALANCER, DELETE, INFO);

        Calendar eventTime = Calendar.getInstance();

        // Notify usage processor
        LOG.info(String.format("Processing DELETE_LOADBALANCER usage for load balancer %s...", dbLoadBalancer.getId()));
        try {
        usageEventCollection.processUsageEvent(usages, dbLoadBalancer, UsageEvent.DELETE_LOADBALANCER, eventTime);
        } catch (Exception exc) {
            String exceptionStackTrace = Debug.getExtendedStackTrace(exc);
            String alertDescription = String.format("An error occurred while processing the usage for an event on loadbalancer %d",
                    dbLoadBalancer.getId());
            String alertDescriptionLog = String.format("%s %d: \n%s\n\n%s",
                    alertDescription, dbLoadBalancer.getId(), exc.getMessage(), exceptionStackTrace);
            LOG.error(alertDescriptionLog);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), exc, USAGE_FAILURE.name(), alertDescription);
        }
        LOG.info(String.format("Completed processing DELETE_LOADBALANCER usage for load balancer %s", dbLoadBalancer.getId()));

        LOG.info(String.format("Load balancer '%d' successfully deleted.", dbLoadBalancer.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Deleting Load Balancer";
        String desc = "Could not delete the load balancer at this time.";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, DELETE_LOADBALANCER, DELETE, CRITICAL);
    }
}
