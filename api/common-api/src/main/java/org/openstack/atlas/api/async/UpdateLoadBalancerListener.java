package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;
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

        if (isRestAdapter()) {
            try {
                LOG.debug(String.format("Updating load balancer '%d' STM...", dbLoadBalancer.getId()));
                reverseProxyLoadBalancerStmService.updateLoadBalancer(dbLoadBalancer, queueLb);
                LOG.debug(String.format("Successfully updated load balancer '%d' in STM.", dbLoadBalancer.getId()));
                atomSummary.append("algorithm: '").append(dbLoadBalancer.getAlgorithm().name()).append("', ");
            } catch (Exception e) {
                loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
                String alertDescription = String.format("Error updating load balancer '%d' in STM.", dbLoadBalancer.getId());
                LOG.error(alertDescription, e);
                notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
                sendErrorToEventResource(queueLb);
                return;
            }
        } else {
            //Maintain for SOAP compatibility...
            if (queueLb.getAlgorithm() != null) {
                try {
                    LOG.debug(String.format("Updating algorithm for load balancer '%d' to '%s' in ZXTM...", dbLoadBalancer.getId(), dbLoadBalancer.getAlgorithm().name()));
                    reverseProxyLoadBalancerService.updateAlgorithm(dbLoadBalancer);
                    LOG.debug(String.format("Successfully updated algorithm for load balancer '%d' to '%s' in Zeus.", dbLoadBalancer.getId(), dbLoadBalancer.getAlgorithm().name()));
                    atomSummary.append("algorithm: '").append(dbLoadBalancer.getAlgorithm().name()).append("', ");
                } catch (Exception e) {
                    loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);

                    String alertDescription = String.format("Error updating algorithm for load balancer '%d' to '%s' in ZXTM.", dbLoadBalancer.getId(), dbLoadBalancer.getAlgorithm().name());
                    LOG.error(alertDescription, e);
                    notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
                    sendErrorToEventResource(queueLb);
                    return;
                }

                if (queueLb.getAlgorithm().equals(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN) || queueLb.getAlgorithm().equals(LoadBalancerAlgorithm.WEIGHTED_LEAST_CONNECTIONS)) {
                    try {
                        LOG.debug(String.format("Updating node weights for load balancer '%d' in ZXTM...", dbLoadBalancer.getId()));
                        reverseProxyLoadBalancerService.setNodeWeights(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId(), dbLoadBalancer.getNodes());
                        LOG.debug(String.format("Successfully updated node weights for load balancer '%d' in ZXTM.", dbLoadBalancer.getId()));
                    } catch (Exception e) {
                        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);

                        String alertDescription = String.format("Error updating node weights for load balancer '%d' in ZXTM...", dbLoadBalancer.getId());
                        LOG.error(alertDescription, e);
                        notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
                        sendErrorToEventResource(queueLb);
                        return;
                    }
                }
            }

            if (queueLb.getProtocol() != null) {
                try {
                    LOG.debug(String.format("Updating protocol for load balancer '%d' to '%s' in ZXTM...", dbLoadBalancer.getId(), dbLoadBalancer.getProtocol().name()));
                    reverseProxyLoadBalancerService.updateProtocol(dbLoadBalancer);
                    LOG.debug(String.format("Successfully updated protocol for load balancer '%d' to '%s' in ZXTM.", dbLoadBalancer.getId(), dbLoadBalancer.getProtocol().name()));
                    atomSummary.append("protocol: '").append(dbLoadBalancer.getProtocol().name()).append("', ");
                } catch (Exception e) {
                    loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);

                    String alertDescription = String.format("Error updating protocol for load balancer '%d' to '%s' in ZXTM.", dbLoadBalancer.getId(), dbLoadBalancer.getProtocol().name());
                    LOG.error(alertDescription, e);
                    notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
                    sendErrorToEventResource(queueLb);
                    return;
                }
            }

            if (queueLb.getPort() != null) {
                LOG.debug("Updating loadbalancer port to " + dbLoadBalancer.getPort() + " in ZXTM...");
                try {
                    LOG.debug(String.format("Updating port for load balancer '%d' to '%d' in ZXTM...", dbLoadBalancer.getId(), dbLoadBalancer.getPort()));
                    reverseProxyLoadBalancerService.updatePort(dbLoadBalancer);
                    LOG.debug(String.format("Successfully updated port for load balancer '%d' to '%d' in ZXTM.", dbLoadBalancer.getId(), dbLoadBalancer.getPort()));
                    atomSummary.append("port: '").append(dbLoadBalancer.getPort()).append("', ");
                } catch (Exception e) {
                    loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);

                    String alertDescription = String.format("Error updating port for load balancer '%d' to '%d' in ZXTM.", dbLoadBalancer.getId(), dbLoadBalancer.getPort());
                    LOG.error(alertDescription, e);
                    notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
                    sendErrorToEventResource(queueLb);
                    return;
                }
            }

            if (queueLb.getTimeout() != null) {
                LOG.debug("Updating loadbalancer timeout to " + dbLoadBalancer.getTimeout() + " in ZXTM...");
                try {
                    LOG.debug(String.format("Updating timeout for load balancer '%d' to '%d' in ZXTM...", dbLoadBalancer.getId(), dbLoadBalancer.getTimeout()));
                    reverseProxyLoadBalancerService.updateTimeout(dbLoadBalancer);
                    LOG.debug(String.format("Successfully updated timeout for load balancer '%d' to '%d' in ZXTM.", dbLoadBalancer.getId(), dbLoadBalancer.getTimeout()));
                    atomSummary.append("timeout: '").append(dbLoadBalancer.getTimeout()).append("', ");
                } catch (Exception e) {
                    loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);

                    String alertDescription = String.format("Error updating timeout for load balancer '%d' to '%d' in ZXTM.", dbLoadBalancer.getId(), dbLoadBalancer.getTimeout());
                    LOG.error(alertDescription, e);
                    notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
                    sendErrorToEventResource(queueLb);
                    return;
                }
            }

            if (queueLb.isHalfClosed() != null) {
                LOG.debug("Updating loadbalancer half close support to " + dbLoadBalancer.isHalfClosed() + " in ZXTM...");
                try {
                    LOG.debug(String.format("Updating timeout for load balancer '%d' to '%s' in ZXTM...", dbLoadBalancer.getId(), dbLoadBalancer.isHalfClosed()));
                    reverseProxyLoadBalancerService.updateHalfClosed(dbLoadBalancer);
                    LOG.debug(String.format("Successfully updated half close support for load balancer '%d' to '%s' in ZXTM.", dbLoadBalancer.getId(), dbLoadBalancer.isHalfClosed()));
                    atomSummary.append("half-close: '").append(dbLoadBalancer.isHalfClosed()).append("', ");
                } catch (Exception e) {
                    loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
                    String alertDescription = String.format("Error updating half close support for load balancer '%d' to '%s' in ZXTM.", dbLoadBalancer.getId(), dbLoadBalancer.isHalfClosed());
                    LOG.error(alertDescription, e);
                    notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
                    sendErrorToEventResource(queueLb);
                    return;
                }
            }
        }

        if (queueLb.isHttpsRedirect() != null) {
            LOG.debug("Updating loadbalancer HTTPS Redirect to " + dbLoadBalancer.isHttpsRedirect() + " in zeus...");
            try {
                LOG.debug(String.format("Updating HTTPS Redirect for load balancer '%d' to '%s' in Zeus...", dbLoadBalancer.getId(), dbLoadBalancer.isHttpsRedirect()));
                reverseProxyLoadBalancerService.updateHttpsRedirect(dbLoadBalancer);
                LOG.debug(String.format("Successfully updated HTTPS Redirect for load balancer '%d' to '%s' in Zeus.", dbLoadBalancer.getId(), dbLoadBalancer.isHttpsRedirect()));
                atomSummary.append("HTTPS Redirect: '").append(dbLoadBalancer.isHttpsRedirect()).append("', ");
            } catch (Exception e) {
                loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
                String alertDescription = String.format("Error updating HTTPS Redirect for load balancer '%d' to '%s' in Zeus.", dbLoadBalancer.getId(), dbLoadBalancer.isHttpsRedirect());
                LOG.error(alertDescription, e);
                notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
                sendErrorToEventResource(queueLb);
                return;
            }
        }

        if (queueLb.getName() != null) {
            LOG.debug("Updating loadbalancer name to " + queueLb.getName());
            LOG.debug(String.format("Successfully updated name for load balancer '%d' to '%s'.", queueLb.getId(), queueLb.getName()));
        }

        if (queueLb.getAlgorithm() != null)
            atomSummary.append("algorithm: '").append(dbLoadBalancer.getAlgorithm().name()).append("', ");
        if (queueLb.getProtocol() != null)
            atomSummary.append("protocol: '").append(dbLoadBalancer.getProtocol().name()).append("', ");
        if (queueLb.getPort() != null) atomSummary.append("port: '").append(dbLoadBalancer.getPort()).append("', ");
        if (queueLb.getTimeout() != null)
            atomSummary.append("timeout: '").append(dbLoadBalancer.getTimeout()).append("', ");
        if (queueLb.isHalfClosed() != null)
            atomSummary.append("half-close: '").append(dbLoadBalancer.isHalfClosed()).append("', ");
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
