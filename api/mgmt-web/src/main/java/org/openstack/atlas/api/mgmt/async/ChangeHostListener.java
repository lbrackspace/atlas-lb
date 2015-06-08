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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.ACTIVE;


public class ChangeHostListener extends BaseListener {

    final Log LOG = LogFactory.getLog(ChangeHostListener.class);

    @Override
    public void doOnMessage(Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        List<LoadBalancer> dbLoadBalancers = new ArrayList<LoadBalancer>();
        MessageDataContainer mdc = getDataContainerFromMessage(message);
        Integer lastLbId = -1;
        String lbIdString = "";

        try {
            for (Integer lbId : mdc.getIds()) {
                lastLbId = lbId;
                LoadBalancer lb = loadBalancerService.getWithUserPages(lbId);
                dbLoadBalancers.add(lb);
                lbIdString = lbIdString + ", " + lbId;
            }
            lbIdString = lbIdString.substring(2);
        } catch (EntityNotFoundException enfe) {
            LOG.error(String.format("EntityNotFoundException thrown while attempting to move Loadbalancer #%d: ", lastLbId));

            // Put any touched LBs back into ACTIVE status, since they do exist
            for (LoadBalancer lb : dbLoadBalancers) {
                loadBalancerService.setStatus(lb, ACTIVE);
            }
            return;
        }

        final Cluster oldCluster = dbLoadBalancers.get(0).getHost().getCluster();
        final Cluster newCluster = mdc.getMoveHost().getCluster();

        try {
            LOG.debug(String.format("Changing host for loadbalancer(s): %s in STM...", lbIdString));

            reverseProxyLoadBalancerStmService.changeHostForLoadBalancers(dbLoadBalancers, mdc.getMoveHost());

            // Update cluster for VIPs
            Map<Integer, VirtualIp> vipsToUpdate = new HashMap<Integer, VirtualIp>();
            if (!newCluster.getId().equals(oldCluster.getId())) {
                for (LoadBalancer dbLoadBalancer : dbLoadBalancers) {
                    Set<LoadBalancerJoinVip> joinVips = dbLoadBalancer.getLoadBalancerJoinVipSet();
                    for (LoadBalancerJoinVip joinVip : joinVips) {
                        VirtualIp vip = joinVip.getVirtualIp();
                        vipsToUpdate.put(vip.getId(), vip);
                    }
                }
                for (VirtualIp vip : vipsToUpdate.values()) {
                    vip.setCluster(newCluster);
                    virtualIpService.merge(vip);
                }
            }

            for (LoadBalancer dbLoadBalancer : dbLoadBalancers) {
                dbLoadBalancer.setHost(mdc.getMoveHost());
                loadBalancerService.update(dbLoadBalancer);
            }

            LOG.debug(String.format("Successfully Changed Host for loadbalancer(s): %s in STM...", lbIdString));
        } catch (Exception e) {
            String msg = String.format("Error moving LB(s): %s in ChangeHostListener(), reverting status to ACTIVE", lbIdString);
            LOG.error(msg, e);

            for (LoadBalancer dbLoadBalancer : dbLoadBalancers) {
                // No idea which one failed, just save an alert for all of them so we can track down the issue
                notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, AlertType.ZEUS_FAILURE.name(), msg);
            }
        }

        for (LoadBalancer dbLoadBalancer : dbLoadBalancers) {
            loadBalancerService.setStatus(dbLoadBalancer, ACTIVE);
        }
        LOG.info(String.format("Move operation complete for loadbalancer(s): %s ", lbIdString));
    }

}
