package org.openstack.atlas.rax.adapter.zxtm;

import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.adapter.zxtm.ZxtmUsageAdapterImpl;
import org.openstack.atlas.adapter.zxtm.helper.ZxtmNameHelper;
import org.openstack.atlas.adapter.zxtm.service.ZxtmServiceStubs;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Primary
@Service
public class RaxZxtmUsageAdapterImpl extends ZxtmUsageAdapterImpl implements RaxZxtmUsageAdapter {

    @Override
    public Map<Integer, Integer> getCurrentConnectionCount(LoadBalancerEndpointConfiguration config, List<LoadBalancer> lbs) throws AdapterException {
        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            Map<Integer, Integer> currentConnectionMap = new HashMap<Integer, Integer>();
            List<String> validVsNames = getValidVsNames(config, toVirtualServerNames(lbs));

            int[] currentConnections = serviceStubs.getSystemStatsBinding().getVirtualserverCurrentConn(validVsNames.toArray(new String[validVsNames.size()]));

            for (int i = 0; i < validVsNames.size(); i++) {
                currentConnectionMap.put(ZxtmNameHelper.stripLbIdFromName(validVsNames.get(i)), currentConnections[i]);
            }

            return currentConnectionMap;
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }
}
