package org.openstack.atlas.api.mgmt.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.services.helpers.AlertType;

import javax.jms.Message;

import java.util.Set;

import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.ACTIVE;


public class ChangeHostListener extends BaseListener {

    final Log LOG = LogFactory.getLog(ChangeHostListener.class);

    @Override
    public void doOnMessage(Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        LoadBalancer dbLoadBalancer;
        MessageDataContainer mdc = getDataContainerFromMessage(message);
        LoadBalancerStatus finalStatus = ACTIVE;

        try {
            dbLoadBalancer = loadBalancerService.getWithUserPages(mdc.getLoadBalancerId(), mdc.getAccountId());
        } catch (EntityNotFoundException enfe) {
            LOG.error(String.format("EntityNotFoundException thrown while attempting to move Loadbalancer #%d: ", mdc.getLoadBalancerId()));
            return;
        }

        final LoadBalancerStatus loadBalancerStatus = mdc.getStatus();
        final Cluster oldCluster = dbLoadBalancer.getHost().getCluster();
        final Cluster newCluster = mdc.getMoveHost().getCluster();

        try {
            LOG.debug(String.format("Changing host for loadbalancer: %s in STM...", dbLoadBalancer.getId()));

            reverseProxyLoadBalancerStmService.changeHostForLoadBalancer(dbLoadBalancer, mdc.getMoveHost());

            // Update cluster for VIPs
            if (!newCluster.getId().equals(oldCluster.getId())) {
                Set<LoadBalancerJoinVip> joinVips = dbLoadBalancer.getLoadBalancerJoinVipSet();
                for (LoadBalancerJoinVip joinVip : joinVips) {
                    VirtualIp vip = joinVip.getVirtualIp();
                    vip.setCluster(newCluster);
                    virtualIpService.merge(vip);
                }
            }

            dbLoadBalancer.setHost(mdc.getMoveHost());
            loadBalancerService.update(dbLoadBalancer);

            LOG.debug(String.format("Successfully Changed Host for loadbalancer: %s in STM...", dbLoadBalancer.getId()));
        } catch (Exception e) {
            String msg = String.format("Error moving #%d in ChangeHostListener(), " +
                    "reverting status, original status: %s:", mdc.getLoadBalancerId(), loadBalancerStatus);

            finalStatus = loadBalancerStatus;
            notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, AlertType.ZEUS_FAILURE.name(), msg);
            LOG.error(msg, e);
        }

        loadBalancerService.setStatus(dbLoadBalancer, finalStatus);
        LOG.info(String.format("Move operation complete for loadbalancer #%d ", mdc.getLoadBalancerId()));
    }

}
