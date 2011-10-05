package org.openstack.atlas.service.domain.common;

import org.openstack.atlas.datamodel.CoreAlgorithmType;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.datamodel.CoreNodeStatus;
import org.openstack.atlas.datamodel.CoreProtocolType;
import org.openstack.atlas.service.domain.common.Constants;
import org.openstack.atlas.service.domain.common.NodesHelper;
import org.openstack.atlas.service.domain.entity.*;

public class LoadBalancerDefaultBuilder {

    private LoadBalancerDefaultBuilder() {
    }

    public static LoadBalancer addDefaultValues(final LoadBalancer loadBalancer) {
        loadBalancer.setStatus(CoreLoadBalancerStatus.BUILD);
        NodesHelper.setNodesToStatus(loadBalancer, CoreNodeStatus.ONLINE);
        if (loadBalancer.getAlgorithm() == null) {
            loadBalancer.setAlgorithm(CoreAlgorithmType.ROUND_ROBIN);
        }
        if (loadBalancer.getConnectionLogging() == null) {
            loadBalancer.setConnectionLogging(false);
        }

        if (loadBalancer.getProtocol() == null || loadBalancer.getPort() == null) {
            /*LoadBalancerProtocolObject defaultProtocol = loadBalancerRepository.getDefaultProtocol();
            if (loadBalancer.getProtocol() == null) {
                loadBalancer.setProtocol(defaultProtocol.getName());
            }
            if (loadBalancer.getPort() == null) {
                loadBalancer.setPort(defaultProtocol.getPort());
            }*/
            if(loadBalancer.getProtocol() == null) {
                loadBalancer.setProtocol(CoreProtocolType.HTTP);
            }
            if(loadBalancer.getPort() == null) {
                loadBalancer.setPort(8080);
            }
        }

        for (Node node : loadBalancer.getNodes()) {
            if (node.getWeight() == null) {
                node.setWeight(Constants.DEFAULT_NODE_WEIGHT);
            }
        }
        return loadBalancer;
    }
}
