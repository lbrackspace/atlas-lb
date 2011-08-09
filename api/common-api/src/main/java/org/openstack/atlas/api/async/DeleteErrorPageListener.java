package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

import javax.jms.Message;

import java.util.ArrayList;
import java.util.List;

import static org.openstack.atlas.service.domain.events.entities.CategoryType.DELETE;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.CRITICAL;
import static org.openstack.atlas.service.domain.events.entities.EventSeverity.INFO;
import static org.openstack.atlas.service.domain.events.entities.EventType.DELETE_NODE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.DATABASE_FAILURE;
import static org.openstack.atlas.service.domain.services.helpers.AlertType.ZEUS_FAILURE;

public class DeleteErrorPageListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(DeleteErrorPageListener.class);
    //TODO:Grab from config..
    private static final String DEFAULT_ERROR_PAGE = "global_error.html";

    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);

        if (getDataContainerFromMessage(message).getAccountId() == null && getDataContainerFromMessage(message).getLoadBalancerId() == null) {
            List<Host> hosts = hostService.getAllHosts();
            for (Host host : hosts) {
                reverseProxyLoadBalancerService.deleteErrorFile(host, DEFAULT_ERROR_PAGE);
            }
        } else {
            reverseProxyLoadBalancerService.removeAndSetDefaultErrorFile();
        }
    }
}
