package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.Stats;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.DELETE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.DELETE_LOADBALANCER;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;

public class DeleteLoadBalancerListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(DeleteLoadBalancerListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);

        LoadBalancer queueLb = getLoadbalancerFromMessage(message);
        LoadBalancer dbLoadBalancer;
        Long bytesOut;
        Long bytesIn;
        Integer concurrentConns;
        Long bytesOutSsl;
        Long bytesInSsl;
        Integer concurrentConnsSsl;

        try {
            dbLoadBalancer = loadBalancerService.get(queueLb.getId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", queueLb.getId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(queueLb.getAccountId(), queueLb.getId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

        // Try to get non-ssl usage
        try {
            bytesOut = reverseProxyLoadBalancerService.getLoadBalancerBytesOut(dbLoadBalancer, false);
            bytesIn = reverseProxyLoadBalancerService.getLoadBalancerBytesIn(dbLoadBalancer, false);
            concurrentConns = reverseProxyLoadBalancerService.getLoadBalancerCurrentConnections(dbLoadBalancer, false);
        } catch (Exception e) {
            LOG.warn("Couldn't retrieve load balancer usage stats. Setting them to null.");
            bytesOut = null;
            bytesIn = null;
            concurrentConns = null;
        }

        // Try to get ssl usage
        try {
            bytesOutSsl = reverseProxyLoadBalancerService.getLoadBalancerBytesOut(dbLoadBalancer, true);
            bytesInSsl = reverseProxyLoadBalancerService.getLoadBalancerBytesIn(dbLoadBalancer, true);
            concurrentConnsSsl = reverseProxyLoadBalancerService.getLoadBalancerCurrentConnections(dbLoadBalancer, true);
        } catch (Exception e) {
            LOG.warn("Couldn't retrieve load balancer usage stats for ssl virtual server. Setting them to null.");
            bytesOutSsl = null;
            bytesInSsl = null;
            concurrentConnsSsl = null;
        }

        try {
            LOG.debug(String.format("Deleting load balancer '%d' in Zeus...", dbLoadBalancer.getId()));
            reverseProxyLoadBalancerService.deleteLoadBalancer(dbLoadBalancer);
            LOG.debug(String.format("Successfully deleted load balancer '%d' in Zeus.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            LOG.error(String.format("LoadBalancer status before error was: '%s'", dbLoadBalancer.getStatus()));
            String alertDescription = String.format("Error deleting loadbalancer '%d' in Zeus.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);

            // Notify usage processor
            usageEventHelper.processUsageEvent(dbLoadBalancer, UsageEvent.DELETE_LOADBALANCER, bytesOut, bytesIn, concurrentConns, bytesOutSsl, bytesInSsl, concurrentConnsSsl);

            return;
        }

        if (dbLoadBalancer.hasSsl()) {
            LOG.debug(String.format("Deleting load balancer '%d' ssl termination in database...", dbLoadBalancer.getId()));
            sslTerminationService.deleteSslTermination(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId());
            LOG.debug(String.format("Successfully deleted load balancer ssl termination '%d' in database.", dbLoadBalancer.getId()));
        }

        dbLoadBalancer = loadBalancerService.pseudoDelete(dbLoadBalancer);
        loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.DELETED);


        // Add atom entry
        String atomTitle = "Load Balancer Successfully Deleted";
        String atomSummary = "Load balancer successfully deleted";
        notificationService.saveLoadBalancerEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, DELETE_LOADBALANCER, DELETE, INFO);

        // Notify usage processor
        usageEventHelper.processUsageEvent(dbLoadBalancer, UsageEvent.DELETE_LOADBALANCER, bytesOut, bytesIn, concurrentConns, bytesOutSsl, bytesInSsl, concurrentConnsSsl);

        LOG.info(String.format("Load balancer '%d' successfully deleted.", dbLoadBalancer.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Deleting Load Balancer";
        String desc = "Could not delete the load balancer at this time.";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, DELETE_LOADBALANCER, DELETE, CRITICAL);
    }
}
