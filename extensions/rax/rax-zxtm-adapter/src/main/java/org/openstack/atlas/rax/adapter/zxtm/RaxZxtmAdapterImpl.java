package org.openstack.atlas.rax.adapter.zxtm;

import com.zxtm.service.client.ObjectDoesNotExist;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.adapter.exception.RollbackException;
import org.openstack.atlas.adapter.zxtm.ZxtmAdapterImpl;
import org.openstack.atlas.adapter.zxtm.helper.ZxtmNameHelper;
import org.openstack.atlas.adapter.zxtm.service.ZxtmServiceStubs;
import org.openstack.atlas.service.domain.entity.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Primary
@Service
public class RaxZxtmAdapterImpl extends ZxtmAdapterImpl implements RaxZxtmAdapter {

    private static Log LOG = LogFactory.getLog(RaxZxtmAdapterImpl.class.getName());

    @Override
    public void addVirtualIps(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Set<VirtualIp> ipv4Vips, Set<VirtualIpv6> ipv6Vips) throws AdapterException {
        try {
            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setAccountId(accountId);
            loadBalancer.setId(lbId);

            for (VirtualIp ipv4Vip : ipv4Vips) {
                LoadBalancerJoinVip joinVip = new LoadBalancerJoinVip(null, loadBalancer, ipv4Vip);
                loadBalancer.getLoadBalancerJoinVipSet().add(joinVip);
            }

            for (VirtualIpv6 ipv6Vip : ipv6Vips) {
                LoadBalancerJoinVip6 joinVip6 = new LoadBalancerJoinVip6(null, loadBalancer, ipv6Vip);
                loadBalancer.getLoadBalancerJoinVip6Set().add(joinVip6);
            }

            addVirtualIps(config, loadBalancer);
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    @Override
    public void deleteVirtualIps(LoadBalancerEndpointConfiguration config, LoadBalancer lb, List<Integer> vipIdsToDelete) throws AdapterException {
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            final String virtualServerName = ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lb.getId(), lb.getAccountId());
            String[][] currentTrafficIpGroups;
            List<String> updatedTrafficIpGroupList = new ArrayList<String>();
            final String rollBackMessage = "Delete virtual ip request canceled.";

            try {
                currentTrafficIpGroups = serviceStubs.getVirtualServerBinding().getListenTrafficIPGroups(new String[]{virtualServerName});
            } catch (Exception e) {
                if (e instanceof ObjectDoesNotExist) {
                    LOG.error("Cannot delete virtual ip from virtual server as the virtual server does not exist.", e);
                }
                LOG.error(rollBackMessage + "Rolling back changes...", e);
                throw new RollbackException(rollBackMessage, e);
            }

            // Convert current traffic groups to array
            List<String> trafficIpGroupNames = new ArrayList<String>();
            for (String[] currentTrafficGroup : currentTrafficIpGroups) {
                trafficIpGroupNames.addAll(Arrays.asList(currentTrafficGroup));
            }

            // Get traffic ip group to delete
            List<String> trafficIpGroupNamesToDelete = new ArrayList<String>();
            for (Integer vipIdToDelete : vipIdsToDelete) {
                trafficIpGroupNamesToDelete.add(ZxtmNameHelper.generateTrafficIpGroupName(lb, vipIdToDelete));
            }

            // Exclude the traffic ip group to delete
            for (String trafficIpGroupName : trafficIpGroupNames) {
                if (!trafficIpGroupNamesToDelete.contains(trafficIpGroupName)) {
                    updatedTrafficIpGroupList.add(trafficIpGroupName);
                    serviceStubs.getTrafficIpGroupBinding().setEnabled(new String[]{trafficIpGroupName}, new boolean[]{true});
                }
            }

            try {
                // Update the virtual server to listen on the updated traffic ip groups
                serviceStubs.getVirtualServerBinding().setListenTrafficIPGroups(new String[]{virtualServerName}, new String[][]{Arrays.copyOf(updatedTrafficIpGroupList.toArray(), updatedTrafficIpGroupList.size(), String[].class)});
            } catch (Exception e) {
                if (e instanceof ObjectDoesNotExist) {
                    LOG.error("Cannot set traffic ip groups to virtual server as it does not exist.", e);
                }
                throw new RollbackException(rollBackMessage, e);
            }

            if (!trafficIpGroupNamesToDelete.isEmpty()) {
                try {
                    deleteTrafficIpGroups(serviceStubs, trafficIpGroupNamesToDelete);
                } catch (RemoteException re) {
                    LOG.error(rollBackMessage + "Rolling back changes...", re);
                    serviceStubs.getVirtualServerBinding().setListenTrafficIPGroups(new String[]{virtualServerName}, new String[][]{Arrays.copyOf(trafficIpGroupNamesToDelete.toArray(), trafficIpGroupNamesToDelete.size(), String[].class)});
                    serviceStubs.getTrafficIpGroupBinding().setEnabled(trafficIpGroupNames.toArray(new String[trafficIpGroupNames.size()]), generateBooleanArray(trafficIpGroupNames.size(), true));
                    throw new RollbackException(rollBackMessage, re);
                }
            }
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    private boolean[] generateBooleanArray(int size, boolean value) {
        boolean[] array = new boolean[size];

        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }

        return array;
    }
}
