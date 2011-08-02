package org.openstack.atlas.api.mgmt.async;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.Sync;
import org.openstack.atlas.service.domain.pojos.SyncLocation;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.api.helpers.NodesHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.*;
import static org.openstack.atlas.service.domain.entities.NodeStatus.ONLINE;
import static org.openstack.atlas.service.domain.events.UsageEvent.SSL_ON;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.CREATE;
import static org.openstack.atlas.service.domain.events.entities.CategoryType.DELETE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.CREATE_LOADBALANCER;
import static org.openstack.atlas.service.domain.events.entities.EventType.DELETE_LOADBALANCER;

public class SyncListener extends BaseListener {

    final Log LOG = LogFactory.getLog(SyncListener.class);

    @Override
    public void doOnMessage(Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        Sync queueSyncObject = getEsbRequestFromMessage(message).getSyncObject();
        LoadBalancer dbLoadBalancer;

        try {
            dbLoadBalancer = loadBalancerService.get(queueSyncObject.getLoadBalancerId());
        } catch (EntityNotFoundException enfe) {
            LOG.error("EntityNotFoundException thrown.");
            return;
        }

        if (queueSyncObject.getLocationToSyncFrom().equals(SyncLocation.DATABASE)) {
            LOG.debug(String.format("Synchronizing load balancer #%d with database configuration", queueSyncObject.getLoadBalancerId()));

            final LoadBalancerStatus loadBalancerStatus = dbLoadBalancer.getStatus();

            try {
                reverseProxyLoadBalancerService.deleteLoadBalancer(dbLoadBalancer);
            } catch (Exception e) {
                String msg = "Error deleting loadbalancer in SyncListener(): ";
                loadBalancerService.setStatus(dbLoadBalancer, ERROR);
                notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, AlertType.LBDEVICE_FAILURE.name(), msg);
                LOG.error(msg, e);
            }

            if (loadBalancerStatus.equals(PENDING_DELETE) || loadBalancerStatus.equals(DELETED)) {
                loadBalancerService.setStatus(dbLoadBalancer, DELETED);
                loadBalancerService.pseudoDelete(dbLoadBalancer);

                if (loadBalancerStatus.equals(PENDING_DELETE)) {
                    // Add atom entry
                    String atomTitle = "Load Balancer Successfully Deleted";
                    String atomSummary = "Load balancer successfully deleted";
                    notificationService.saveLoadBalancerEvent(dbLoadBalancer.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, DELETE_LOADBALANCER, DELETE, INFO);

                    // Notify usage processor with a usage event
                    notifyUsageProcessor(message, dbLoadBalancer, UsageEvent.DELETE_LOADBALANCER);
                }
            } else {
                try {
                    reverseProxyLoadBalancerService.createLoadBalancer(dbLoadBalancer);
                    loadBalancerService.setStatus(dbLoadBalancer, ACTIVE);

                    if (loadBalancerStatus.equals(BUILD)) {
                        NodesHelper.setNodesToStatus(dbLoadBalancer, ONLINE);
                        dbLoadBalancer.setStatus(ACTIVE);
                        dbLoadBalancer = loadBalancerService.update(dbLoadBalancer);

                        // Add atom entry
                        String atomTitle = "Load Balancer Successfully Created";
                        String atomSummary = createAtomSummary(dbLoadBalancer).toString();
                        notificationService.saveLoadBalancerEvent(dbLoadBalancer.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary, CREATE_LOADBALANCER, CREATE, INFO);

                        // Notify usage processor
                        notifyUsageProcessor(message, dbLoadBalancer, UsageEvent.CREATE_LOADBALANCER);
                        if (dbLoadBalancer.isUsingSsl()) notifyUsageProcessor(message, dbLoadBalancer, SSL_ON);
                    }
                } catch (Exception e) {
                    String msg = "Error re-creating loadbalancer in SyncListener():";
                    loadBalancerService.setStatus(dbLoadBalancer, ERROR);
                    notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, AlertType.LBDEVICE_FAILURE.name(), msg);
                    LOG.error(msg, e);
                }
            }
        } else if (queueSyncObject.getLocationToSyncFrom().equals(SyncLocation.LBDEVICE)) {
            LOG.warn(String.format("Load balancers can only be synchronized with the database at this time."));
        }

        LOG.info("Sync operation complete.");
    }

    private StringBuffer createAtomSummary(LoadBalancer lb) {
        StringBuffer atomSummary = new StringBuffer();
        atomSummary.append("Load balancer successfully created with ");
        atomSummary.append("name: '").append(lb.getName()).append("', ");
        atomSummary.append("algorithm: '").append(lb.getAlgorithm()).append("', ");
        atomSummary.append("protocol: '").append(lb.getProtocol()).append("', ");
        atomSummary.append("port: '").append(lb.getPort()).append("'");
        return atomSummary;
    }
}
