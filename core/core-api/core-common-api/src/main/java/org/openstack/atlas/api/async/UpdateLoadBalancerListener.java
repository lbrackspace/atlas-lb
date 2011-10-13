package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.common.converters.StringConverter;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.LoadBalancerService;
import org.openstack.atlas.service.domain.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.List;

import static org.openstack.atlas.service.domain.common.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.common.AlertType.LBDEVICE_FAILURE;
import static org.openstack.atlas.service.domain.event.entity.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.event.entity.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.event.entity.EventType.UPDATE_LOADBALANCER;

@Component
public class UpdateLoadBalancerListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(UpdateLoadBalancerListener.class);

    @Autowired
    private LoadBalancerService loadBalancerService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private LoadBalancerRepository loadBalancerRepository;

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);

        LoadBalancer queueLb = getDataContainerFromMessage(message).getLoadBalancer();
        StringBuffer atomSummary = new StringBuffer("Load balancer successfully updated with ");

        LoadBalancer dbLoadBalancer;
        try {
            dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(queueLb.getId(), queueLb.getAccountId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", queueLb.getId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(queueLb.getAccountId(), queueLb.getId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

        try {
            LOG.debug(String.format("Updating load balancer '%d' in LB Device...", dbLoadBalancer.getId()));
            reverseProxyLoadBalancerService.updateLoadBalancer(dbLoadBalancer.getAccountId(), dbLoadBalancer);
            LOG.debug(String.format("Successfully updated load balancer '%d' in LB Device.", dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error updating loadbalancer '%d' in LB Device.", dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, LBDEVICE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(queueLb);
            return;
        }

        /*if (queueLb.getAlgorithm() != null) {
            try {
                LOG.debug(String.format("Updating algorithm for load balancer '%d' to '%s' in LB Device...", dbLoadBalancer.getId(), dbLoadBalancer.getAlgorithm().name()));
                reverseProxyLoadBalancerService.updateAlgorithm(dbLoadBalancer);
                LOG.debug(String.format("Successfully updated algorithm for load balancer '%d' to '%s' in LB Device.", dbLoadBalancer.getId(), dbLoadBalancer.getAlgorithm().name()));
                atomSummary.append("algorithm: '").append(dbLoadBalancer.getAlgorithm().name()).append("', ");
            } catch (Exception e) {
                loadBalancerRepository.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
                String alertDescription = String.format("Error updating algorithm for load balancer '%d' to '%s' in LB Device.", dbLoadBalancer.getId(), dbLoadBalancer.getAlgorithm().name());
                LOG.error(alertDescription, e);
                notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, LBDEVICE_FAILURE.name(), alertDescription);
                sendErrorToEventResource(queueLb);
                return;
            }

            if (queueLb.getAlgorithm().equals(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN)) {
                try {
                    LOG.debug(String.format("Updating node weights for load balancer '%d' in LB Device...", dbLoadBalancer.getId()));
                    reverseProxyLoadBalancerService.setNodeWeights(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getNodes());
                    LOG.debug(String.format("Successfully updated node weights for load balancer '%d' in LB Device.", dbLoadBalancer.getId()));
                } catch (Exception e) {
                    loadBalancerRepository.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
                    String alertDescription = String.format("Error updating node weights for load balancer '%d' in LB Device...", dbLoadBalancer.getId());
                    LOG.error(alertDescription, e);
                    notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, LBDEVICE_FAILURE.name(), alertDescription);
                    sendErrorToEventResource(queueLb);
                    return;
                }
            }
        }

        if (queueLb.getProtocol() != null) {
            try {
                LOG.debug(String.format("Updating protocol for load balancer '%d' to '%s' in LB Device...", dbLoadBalancer.getId(), dbLoadBalancer.getProtocol().name()));
                reverseProxyLoadBalancerService.updateProtocol(dbLoadBalancer);
                LOG.debug(String.format("Successfully updated protocol for load balancer '%d' to '%s' in LB Device.", dbLoadBalancer.getId(), dbLoadBalancer.getProtocol().name()));
                atomSummary.append("protocol: '").append(dbLoadBalancer.getProtocol().name()).append("', ");
            } catch (Exception e) {
                loadBalancerRepository.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
                String alertDescription = String.format("Error updating protocol for load balancer '%d' to '%s' in LB Device.", dbLoadBalancer.getId(), dbLoadBalancer.getProtocol().name());
                LOG.error(alertDescription, e);
                notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, LBDEVICE_FAILURE.name(), alertDescription);
                sendErrorToEventResource(queueLb);
                return;
            }
        }

        if (queueLb.getPort() != null) {
            LOG.debug("Updating loadbalancer port to " + dbLoadBalancer.getPort() + " in LB Device...");
            try {
                LOG.debug(String.format("Updating port for load balancer '%d' to '%d' in LB Device...", dbLoadBalancer.getId(), dbLoadBalancer.getPort()));
                reverseProxyLoadBalancerService.updatePort(dbLoadBalancer);
                LOG.debug(String.format("Successfully updated port for load balancer '%d' to '%d' in LB Device.", dbLoadBalancer.getId(), dbLoadBalancer.getPort()));
                atomSummary.append("port: '").append(dbLoadBalancer.getPort()).append("', ");
            } catch (Exception e) {
                loadBalancerRepository.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
                String alertDescription = String.format("Error updating port for load balancer '%d' to '%d' in LB Device.", dbLoadBalancer.getId(), dbLoadBalancer.getPort());
                LOG.error(alertDescription, e);
                notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, LBDEVICE_FAILURE.name(), alertDescription);
                sendErrorToEventResource(queueLb);
                return;
            }
        }
*/
        List<String> updateStrList = new ArrayList<String>();
        if (queueLb.getName() != null) {
            LOG.debug("Updating loadbalancer name to " + queueLb.getName());
            LOG.debug(String.format("Successfully updated name for load balancer '%d' to '%s'.", queueLb.getId(), queueLb.getName()));
            updateStrList.add(String.format("%s: '%s'","name",queueLb.getName()));
        }

        // Update load balancer status in DB
        loadBalancerRepository.changeStatus(dbLoadBalancer, CoreLoadBalancerStatus.ACTIVE);

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
