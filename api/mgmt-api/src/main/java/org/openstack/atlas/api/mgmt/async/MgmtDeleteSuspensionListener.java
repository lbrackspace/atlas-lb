package org.openstack.atlas.api.mgmt.async;


import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.exceptions.UsageEventCollectionException;
import org.openstack.atlas.util.debug.Debug;

import javax.jms.Message;

import java.util.Calendar;
import java.util.Map;

import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.USAGE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.UPDATE_LOADBALANCER;

public class MgmtDeleteSuspensionListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(MgmtDeleteSuspensionListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        LoadBalancer requestLb = getEsbRequestFromMessage(message).getLoadBalancer();

        Map<Integer, LoadBalancer> vipSharingLbs = loadBalancerService.fetchLBsThatShareIPsWith(requestLb.getId());
        StringBuffer lbsUnSuspended = new StringBuffer();
        int unsuspendedLBCount = 0;
        for (LoadBalancer dbLoadBalancer : vipSharingLbs.values()) {
            if (dbLoadBalancer.getStatus().equals(LoadBalancerStatus.SUSPENDED)
                    || dbLoadBalancer.getStatus().equals(LoadBalancerStatus.PENDING_UPDATE)) {//the requested lb will be in PENDING_UPDATE

                try {
                        LOG.debug(String.format("Removing suspension from load balancer '%d' in backend...", dbLoadBalancer.getId()));
                        reverseProxyLoadBalancerVTMService.removeSuspension(dbLoadBalancer);
                        LOG.debug(String.format("Successfully removed suspension from load balancer '%d' in STM.", dbLoadBalancer.getId()));

                } catch (Exception e) {
                    loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
                    String alertDescription = String.format("Error removing suspension from load balancer '%d' backend...", dbLoadBalancer.getId());
                    LOG.error(alertDescription, e);
                    notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
                    sendErrorToEventResource(dbLoadBalancer);
                    continue;
                }

                Calendar eventTime = Calendar.getInstance();
                // Notify usage processor
                try {
                    usageEventCollection.processZeroUsageEvent(dbLoadBalancer, UsageEvent.UNSUSPEND_LOADBALANCER, eventTime);
                } catch (UsageEventCollectionException uex) {
                    LOG.error(String.format("Collection and processing of the usage event failed for load balancer: %s " +
                            ":: Exception: %s", dbLoadBalancer.getId(), uex));
                } catch (Exception exc) {
                    String exceptionStackTrace = Debug.getExtendedStackTrace(exc);
                    String usageAlertDescription = String.format("An error occurred while processing the usage for an event on loadbalancer %d: \n%s\n\n%s",
                            dbLoadBalancer.getId(), exc.getMessage(), exceptionStackTrace);
                    LOG.error(usageAlertDescription);
                    notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), exc, USAGE_FAILURE.name(), usageAlertDescription);
                }

                // Update load balancer in DB
                LOG.debug("Deleting Suspension from database...");
                loadBalancerService.removeSuspension(dbLoadBalancer.getId());
                loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);

                // Add atom entry
                String atomTitle = "Load Balancer Un-Suspended";
                String atomSummary = "Load balancer un-suspended";
                notificationService.saveLoadBalancerEvent(requestLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, UPDATE_LOADBALANCER, UPDATE, INFO);

                LOG.info(String.format("Remove suspension operation complete for load balancer '%d'.", dbLoadBalancer.getId()));
                unsuspendedLBCount++;
                lbsUnSuspended.append(dbLoadBalancer.getId() + " ");
            }
        }
        if (unsuspendedLBCount > 1)
            LOG.info(String.format("Remove suspension operation completed for the loadbalancers { %s} that shared the VIPs with loadbalancer %d.", lbsUnSuspended, requestLb.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Un-Suspending Load Balancer";
        String desc = "Could not un-suspend the loadbalancer at this time.";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, UPDATE_LOADBALANCER, UPDATE, CRITICAL);
    }
}
