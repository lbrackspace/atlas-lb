package org.openstack.atlas.api.mgmt.async;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.exceptions.UsageEventCollectionException;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.util.debug.Debug;

import javax.jms.Message;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.USAGE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.UPDATE_LOADBALANCER;

public class MgmtCreateSuspensionListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(MgmtCreateSuspensionListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        LoadBalancer requestLb = getEsbRequestFromMessage(message).getLoadBalancer();
        LoadBalancer dbLoadBalancer;

        try {
            dbLoadBalancer = loadBalancerService.get(requestLb.getId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", requestLb.getId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(requestLb.getAccountId(), requestLb.getId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(requestLb);
            return;
        }

        List<SnmpUsage> usages = new ArrayList<SnmpUsage>();
        try {
            LOG.info(String.format("Collecting SUSPEND_LOADBALANCER usage for load balancer %s...", dbLoadBalancer.getId()));
            usages = usageEventCollection.getUsage(dbLoadBalancer);
            LOG.info(String.format("Successfully collected SUSPEND_LOADBALANCER usage for load balancer %s", dbLoadBalancer.getId()));
        } catch (UsageEventCollectionException e) {
            LOG.error(String.format("Collection of the SUSPEND_LOADBALANCER usage event failed for " +
                    "load balancer: %s :: Exception: %s", dbLoadBalancer.getId(), e));
        }

        try {
            if (isRestAdapter()) {
                LOG.debug(String.format("Suspending load balancer '%d' in STM...", dbLoadBalancer.getId()));
                reverseProxyLoadBalancerStmService.suspendLoadBalancer(dbLoadBalancer);
                LOG.debug(String.format("Successfully suspended load balancer '%d' in STM.", dbLoadBalancer.getId()));
            } else {
                LOG.debug(String.format("Suspending load balancer '%d' in ZXTM...", dbLoadBalancer.getId()));
                reverseProxyLoadBalancerService.suspendLoadBalancer(dbLoadBalancer);
                LOG.debug(String.format("Successfully suspended load balancer '%d' in ZXTM.", dbLoadBalancer.getId()));
            }
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);

            String alertDescription = String.format("Error suspending load balancer '%d' in Zeus.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(requestLb);
            return;
        }

        // Update load balancer in DB
        LOG.debug("Adding the suspension to the database...");
        loadBalancerService.createSuspension(dbLoadBalancer, requestLb.getSuspension());
        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.SUSPENDED);

        // Add atom entry
        String atomTitle = "Load Balancer Suspended";
        String atomSummary = "Load balancer suspended. Please contact support if you have any questions.";
        notificationService.saveLoadBalancerEvent(requestLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, UPDATE_LOADBALANCER, UPDATE, INFO);

        Calendar eventTime = Calendar.getInstance();
        try {
        usageEventCollection.processUsageEvent(usages, dbLoadBalancer, UsageEvent.SUSPEND_LOADBALANCER, eventTime);
        } catch (Exception exc) {
            String exceptionStackTrace = Debug.getExtendedStackTrace(exc);
            String usageAlertDescription = String.format("An error occurred while processing the usage for an event on loadbalancer %d: \n%s\n\n%s",
                                                         dbLoadBalancer.getId(), exc.getMessage(), exceptionStackTrace);
            LOG.error(usageAlertDescription);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), exc, USAGE_FAILURE.name(), usageAlertDescription);
        }


        LOG.info(String.format("Suspend load balancer operation complete for load balancer '%d'.", dbLoadBalancer.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Suspending Load Balancer";
        String desc = "Could not suspend the loadbalancer at this time.";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, UPDATE_LOADBALANCER, UPDATE, CRITICAL);
    }
}


