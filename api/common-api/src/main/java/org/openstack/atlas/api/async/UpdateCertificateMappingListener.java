package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.CertificateMapping;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;

import javax.jms.Message;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.UPDATE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventType.UPDATE_CERTIFICATE_MAPPING;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;

public class UpdateCertificateMappingListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(UpdateCertificateMappingListener.class);

    @Override
    public void doOnMessage(Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);

        MessageDataContainer dataContainer = getDataContainerFromMessage(message);
        CertificateMapping certMappingToUpdate = dataContainer.getCertificateMapping();
        LoadBalancer dbLoadBalancer;

        try {
            dbLoadBalancer = loadBalancerService.get(dataContainer.getLoadBalancerId(), dataContainer.getAccountId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", dataContainer.getLoadBalancerId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dataContainer);
            return;
        }

        try {
            LOG.info(String.format("Adding/Updating certificate mapping '%d' for load balancer '%d' in ZXTM...", dataContainer.getCertificateMappingId(), dbLoadBalancer.getId()));
            reverseProxyLoadBalancerService.updateCertificateMapping(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId(), certMappingToUpdate);
            LOG.debug(String.format("Successfully added/updated certificate mapping '%d' for load balancer '%d' in Zeus...", dataContainer.getCertificateMappingId(), dbLoadBalancer.getId()));
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error adding/updating certificate mapping '%d' in Zeus for loadbalancer '%d'.", certMappingToUpdate.getId(), dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dataContainer);
            return;
        }

        // Update load balancer status in database
        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);
        // Save a load balancer status record
        loadBalancerStatusHistoryService.save(dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), LoadBalancerStatus.ACTIVE);

        LOG.info(String.format("Add/Update certificate mapping operation complete for load balancer '%d' with certificate mapping '%d'.", dbLoadBalancer.getId(), certMappingToUpdate.getId()));
    }

    private void sendErrorToEventResource(MessageDataContainer dataContainer) {
        String title = "Error Updating Certificate Mapping";
        String desc = "Could not update the certificate mapping settings at this time.";
        notificationService.saveLoadBalancerEvent(dataContainer.getUserName(), dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), title, desc, UPDATE_CERTIFICATE_MAPPING, UPDATE, CRITICAL);
    }
}
