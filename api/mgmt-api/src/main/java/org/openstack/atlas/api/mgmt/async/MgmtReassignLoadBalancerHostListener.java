package org.openstack.atlas.api.mgmt.async;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;
import javax.persistence.EntityExistsException;
import java.util.ArrayList;
import java.util.List;

public class MgmtReassignLoadBalancerHostListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(MgmtReassignLoadBalancerHostListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        List<LoadBalancer> requestLbs = getEsbRequestFromMessage(message).getLoadBalancers();

        LoadBalancer dbLb = null;
        try {
            //Loop through and update the new configurations asynchronously
            for (LoadBalancer lb : requestLbs) {
                try {
                    dbLb = loadBalancerService.getWithUserPages(lb.getId());
                } catch (EntityNotFoundException e) {
                    throw new EntityExistsException("There was a problem retrieving one of the requesting load balancers configuration.");
                }

                if (isRestAdapter()) {
                    LOG.debug("Updating host to " + lb.getHost().getId() + " in trafficmanager for loadbalancer " + lb.getId());
                    //TODO: simplify this to use list object for changeHost Adapter call instead of repacking them
                    ArrayList<LoadBalancer> lbs = new ArrayList<>();
                    lbs.add(dbLb);
                    reverseProxyLoadBalancerVTMService.changeHostForLoadBalancers(lbs, lb.getHost());
                } else {
                    LOG.debug("Modifying Host in ZXTM.. ");
                    LOG.debug("Updating host to " + lb.getHost().getId() + " in zeus for loadbalancer " + lb.getId());
                    reverseProxyLoadBalancerService.changeHostForLoadBalancer(dbLb, lb.getHost());
                }
                loadBalancerService.setStatus(lb, LoadBalancerStatus.ACTIVE);

                LOG.debug("Successfully updated load balancer:" + lb.getId() + " in Zeus.");
            }
            LOG.debug("Successfully reassigned load balancer hosts in zeus.");
        } catch (Exception e) {
            loadBalancerService.setStatus(dbLb, LoadBalancerStatus.ERROR);

            throw new Exception("ZeuFailureException: One or more of the load balancers failed while being configured for Zeus : " + e);
        }
        loadBalancerService.updateLoadBalancers(requestLbs);
    }
}


