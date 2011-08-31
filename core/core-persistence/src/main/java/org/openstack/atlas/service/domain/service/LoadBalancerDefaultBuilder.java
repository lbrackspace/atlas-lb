package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.common.Constants;
import org.openstack.atlas.service.domain.common.NodesHelper;
import org.openstack.atlas.service.domain.entity.*;

public class LoadBalancerDefaultBuilder {

    private LoadBalancerDefaultBuilder() {
    }

    public static LoadBalancer addDefaultValues(final LoadBalancer loadBalancer) {
        loadBalancer.setStatus(LoadBalancerStatus.BUILD);
        NodesHelper.setNodesToStatus(loadBalancer, NodeStatus.ONLINE);
        if (loadBalancer.getAlgorithm() == null) {
            loadBalancer.setAlgorithm(LoadBalancerAlgorithm.RANDOM);
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
                loadBalancer.setProtocol(LoadBalancerProtocol.HTTP);
            }
            if(loadBalancer.getPort() == null) {
                loadBalancer.setPort(8080);
            }
        }

        if (loadBalancer.getSessionPersistence() == null) {
            loadBalancer.setSessionPersistence(SessionPersistence.NONE);
        }

        for (Node node : loadBalancer.getNodes()) {
            if (node.getWeight() == null) {
                node.setWeight(Constants.DEFAULT_NODE_WEIGHT);
            }
        }
        return loadBalancer;
    }
}
