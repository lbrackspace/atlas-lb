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
        CertificateMapping queueCertMapping = dataContainer.getCertificateMapping();
        LoadBalancer dbLoadBalancer;

        try {
            dbLoadBalancer = loadBalancerService.getWithUserPages(dataContainer.getLoadBalancerId(), dataContainer.getAccountId());
        } catch (EntityNotFoundException enfe) {
            String alertDescription = String.format("Load balancer '%d' not found in database.", dataContainer.getLoadBalancerId());
            LOG.error(alertDescription, enfe);
            notificationService.saveAlert(dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), enfe, DATABASE_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dataContainer);
            return;
        }

        try {
            if (isRestAdapter()) {
                LOG.debug(String.format("Updating certificate mappings for load balancer '%d' in vTM...", dbLoadBalancer.getId()));
                reverseProxyLoadBalancerVTMService.updateCertificateMapping(dbLoadBalancer, queueCertMapping);
                LOG.debug(String.format("Successfully updated certificate mappings for load balancer '%d' in vTM...", dbLoadBalancer.getId()));
            } else {
                LOG.info(String.format("Adding/Updating certificate mapping '%d' for load balancer '%d' in ZXTM...", queueCertMapping.getId(), dbLoadBalancer.getId()));
                CertificateMapping dbCertMapping = certificateMappingService.getByIdAndLoadBalancerId(queueCertMapping.getId(), dbLoadBalancer.getId());
                reverseProxyLoadBalancerService.updateCertificateMapping(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId(), dbCertMapping);
                LOG.debug(String.format("Successfully added/updated certificate mapping '%d' for load balancer '%d' in Zeus...", queueCertMapping.getId(), dbLoadBalancer.getId()));
            }
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ERROR);
            String alertDescription = String.format("Error adding/updating certificate mapping '%d' in Zeus for loadbalancer '%d'.", queueCertMapping.getId(), dbLoadBalancer.getId());
            LOG.error(alertDescription, e);
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, ZEUS_FAILURE.name(), alertDescription);
            sendErrorToEventResource(dataContainer);
            return;
        }

        // Update load balancer status in database
        loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);
        // Save a load balancer status record
        loadBalancerStatusHistoryService.save(dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), LoadBalancerStatus.ACTIVE);

        LOG.info(String.format("Add/Update certificate mapping operation complete for load balancer '%d' with certificate mapping '%d'.", dbLoadBalancer.getId(), queueCertMapping.getId()));
    }

    private void sendErrorToEventResource(MessageDataContainer dataContainer) {
        String title = "Error Updating Certificate Mapping";
        String desc = "Could not update the certificate mapping settings at this time.";
        notificationService.saveLoadBalancerEvent(dataContainer.getUserName(), dataContainer.getAccountId(), dataContainer.getLoadBalancerId(), title, desc, UPDATE_CERTIFICATE_MAPPING, UPDATE, CRITICAL);
    }
}
