package org.openstack.atlas.api.mgmt.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Cidr;
import org.openstack.atlas.lb.helpers.ipstring.IPv4Ranges;
import org.openstack.atlas.lb.helpers.ipstring.IPv4ToolSet;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.VirtualIpType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
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
            notificationService.saveAlert(ex, AlertType.ZEUS_FAILURE.name(), ex.getMessage());
        }
        LOG.info(String.format("Completed migrating vips to new cluster id = %d", newClusterId));
    }
}
