package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.util.converters.StringConverter;

import javax.jms.Message;
import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.List;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.UPDATE_LOADBALANCER;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;

public class UpdateLoadBalancerListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(UpdateLoadBalancerListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);

        List<String> updateStrList = new ArrayList<String>();

        ObjectMessage object = (ObjectMessage) message;
        LoadBalancer queueLb = (LoadBalancer) object.getObject();
        LoadBalancer dbLoadBalancer;
        StringBuilder atomSummary = new StringBuilder("Load balancer successfully updated with ");

        try {
            dbLoadBalancer = loadBalancerService.getWithUserPages(queueLb.getId(), queueLb.getAccountId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", queueLb.getId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(queueLb.getAccountId(), queueLb.getId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

            try {
                LOG.debug(String.format("Updating load balancer '%d' backend...", dbLoadBalancer.getId()));
                reverseProxyLoadBalancerVTMService.updateLoadBalancer(dbLoadBalancer, queueLb);
                LOG.debug(String.format("Successfully updated load balancer '%d' in backend.", dbLoadBalancer.getId()));
                atomSummary.append("algorithm: '").append(dbLoadBalancer.getAlgorithm().name()).append("', ");
            } catch (Exception e) {
                loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
                String alertDescription = String.format("Error updating load balancer '%d' in backend.", dbLoadBalancer.getId());
                LOG.error(alertDescription, e);
                notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
                sendErrorToEventResource(queueLb);
                return;
            }

        //This doesn't appear to do anything... No point in being misleading in the logs, commenting it out.
//        if (queueLb.getName() != null) {
//            LOG.debug("Updating loadbalancer name to " + queueLb.getName());
//            LOG.debug(String.format("Successfully updated name for load balancer '%d' to '%s'.", queueLb.getId(), queueLb.getName()));
//        }

        if (queueLb.getAlgorithm() != null)
            atomSummary.append("algorithm: '").append(dbLoadBalancer.getAlgorithm().name()).append("', ");
        if (queueLb.getProtocol() != null)
            atomSummary.append("protocol: '").append(dbLoadBalancer.getProtocol().name()).append("', ");
        if (queueLb.getPort() != null) atomSummary.append("port: '").append(dbLoadBalancer.getPort()).append("', ");
        if (queueLb.getTimeout() != null)
            atomSummary.append("timeout: '").append(dbLoadBalancer.getTimeout()).append("', ");
        if (queueLb.getHalfClosed() != null)
            atomSummary.append("half-close: '").append(dbLoadBalancer.getHalfClosed()).append("', ");
        if (queueLb.getName() != null) updateStrList.add(String.format("%s: '%s'", "name", queueLb.getName()));

        // Update load balancer status in DB
        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);

        // Add atom entry
        String atomTitle = "Load Balancer Successfully Updated";
        atomSummary.append(StringConverter.commaSeperatedStringList(updateStrList));
        notificationService.saveLoadBalancerEvent(queueLb.getUserName(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), atomTitle, atomSummary.toString(), UPDATE_LOADBALANCER, UPDATE, INFO);

        LOG.info(String.format("Load balancer '%d' successfully updated.", dbLoadBalancer.getId()));
    }

    private void sendErrorToEventResource(LoadBalancer lb) {
        String title = "Error Updating Load Balancer";
        String desc = "Could not update the load balancer at this time.";
        notificationService.saveLoadBalancerEvent(lb.getUserName(), lb.getAccountId(), lb.getId(), title, desc, UPDATE_LOADBALANCER, UPDATE, CRITICAL);
    }
}
