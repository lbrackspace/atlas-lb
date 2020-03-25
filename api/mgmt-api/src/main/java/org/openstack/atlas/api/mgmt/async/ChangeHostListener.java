package org.openstack.atlas.api.mgmt.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip6;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.entities.VirtualIpv6;
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
import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.ERROR;


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
        LoadBalancerStatus finalStatus = ACTIVE;

        try {
            for (Integer lbId : mdc.getIds()) {
                lastLbId = lbId;
                LoadBalancer lb = loadBalancerService.getWithUserPages(lbId);
                lb.getUserPages();
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

            reverseProxyLoadBalancerVTMService.changeHostForLoadBalancers(dbLoadBalancers, mdc.getMoveHost());

            // Update cluster for VIPs
            if (!newCluster.getId().equals(oldCluster.getId())) {
                Map<Integer, VirtualIp> vipsToUpdate = new HashMap<Integer, VirtualIp>();
                Map<Integer, VirtualIpv6> vip6sToUpdate = new HashMap<Integer, VirtualIpv6>();
                for (LoadBalancer dbLoadBalancer : dbLoadBalancers) {
                    Set<LoadBalancerJoinVip> joinVips = dbLoadBalancer.getLoadBalancerJoinVipSet();
                    for (LoadBalancerJoinVip joinVip : joinVips) {
                        VirtualIp vip = joinVip.getVirtualIp();
                        vipsToUpdate.put(vip.getId(), vip);
                    }
                    Set<LoadBalancerJoinVip6> joinVip6s = dbLoadBalancer.getLoadBalancerJoinVip6Set();
                    for (LoadBalancerJoinVip6 joinVip6 : joinVip6s) {
                        VirtualIpv6 vip6 = joinVip6.getVirtualIp();
                        vip6sToUpdate.put(vip6.getId(), vip6);
                    }
                }
                for (VirtualIp vip : vipsToUpdate.values()) {
                    virtualIpService.updateCluster(vip, newCluster);
                }
                for (VirtualIpv6 vip6 : vip6sToUpdate.values()) {
                    virtualIpService.updateCluster(vip6, newCluster);
                }
            }

            for (LoadBalancer dbLoadBalancer : dbLoadBalancers) {
                dbLoadBalancer.setHost(mdc.getMoveHost());
                dbLoadBalancer.setUserPages(null);
                loadBalancerService.update(dbLoadBalancer);
            }

            LOG.debug(String.format("Successfully Changed Host for loadbalancer(s): %s in STM...", lbIdString));
        } catch (Exception e) {
            String msg = String.format("Error moving LB(s): %s in ChangeHostListener(), setting status to ERROR.", lbIdString);
            LOG.error(msg, e);
            finalStatus = ERROR;

            for (LoadBalancer dbLoadBalancer : dbLoadBalancers) {
                // No idea which one failed, just save an alert for all of them so we can track down the issue
                notificationService.saveAlert(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), e, AlertType.ZEUS_FAILURE.name(), msg);
            }
        }

        for (LoadBalancer dbLoadBalancer : dbLoadBalancers) {
            loadBalancerService.setStatus(dbLoadBalancer, finalStatus);
        }
        LOG.info(String.format("Move operation complete for loadbalancer(s): %s ", lbIdString));
    }

}
