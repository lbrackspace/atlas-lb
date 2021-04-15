package org.openstack.atlas.api.mgmt.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Cidr;
import org.openstack.atlas.service.domain.services.helpers.AlertType;

import javax.jms.Message;


public class MigrateVipsToClusterListener extends BaseListener {


    private final Log LOG = LogFactory.getLog(MigrateVipsToClusterListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        Cidr cidr = getEsbRequestFromMessage(message).getCidr();
        Integer newClusterId = getEsbRequestFromMessage(message).getClusterId();
        try {
            virtualIpService.migrateVipsToClusterByCidrBlock(newClusterId, cidr);
        } catch (Exception ex) {
            String msg = String.format("Unabled to migrate vips in CIDR: %s to Cluster ID: %d. Exception: %s",
                    cidr.getBlock(), newClusterId, ex.getMessage());
            notificationService.saveAlert(ex, AlertType.ZEUS_FAILURE.name(), msg);
        }
        LOG.info(String.format("Completed migrating vips to new cluster id = %d", newClusterId));
    }
}
