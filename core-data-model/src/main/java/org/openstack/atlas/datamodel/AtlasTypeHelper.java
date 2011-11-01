package org.openstack.atlas.datamodel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class AtlasTypeHelper {
    private static AlgorithmType algorithmType = new CoreAlgorithmType();
    private static ProtocolType protocolType = new CoreProtocolType();
    private static LoadBalancerStatus loadBalancerStatus = new CoreLoadBalancerStatus();
    private static NodeStatus nodeStatus = new CoreNodeStatus();
    private static PersistenceType persistenceType = new CorePersistenceType();
    private static HealthMonitorType healthMonitorType = new CoreHealthMonitorType();

    @Autowired(required = true)
    public void setAlgorithmType(AlgorithmType algorithmType) {
        AtlasTypeHelper.algorithmType = algorithmType;
    }

    @Autowired(required = true)
    public void setProtocolType(ProtocolType protocolType) {
        AtlasTypeHelper.protocolType = protocolType;
    }

    @Autowired(required = true)
    public void setLoadBalancerStatus(LoadBalancerStatus loadBalancerStatus) {
        AtlasTypeHelper.loadBalancerStatus = loadBalancerStatus;
    }

    @Autowired(required = true)
    public void setNodeStatus(NodeStatus nodeStatus) {
        AtlasTypeHelper.nodeStatus = nodeStatus;
    }

    @Autowired(required = true)
    public void setPersistenceType(PersistenceType persistenceType) {
        AtlasTypeHelper.persistenceType = persistenceType;
    }

    @Autowired(required = true)
    public void setHealthMonitorType(HealthMonitorType healthMonitorType) {
        AtlasTypeHelper.healthMonitorType = healthMonitorType;
    }

    public static boolean isValidAlgorithm(String algorithm) {
        return isValidAtlasType(algorithm, algorithmType);
    }

    public static boolean isValidProtocol(String protocol) {
        return isValidAtlasType(protocol, protocolType);
    }

    public static boolean isValidLoadBalancerStatus(String status) {
        return isValidAtlasType(status, loadBalancerStatus);
    }

    public static boolean isValidNodeStatus(String status) {
        return isValidAtlasType(status, nodeStatus);
    }

    public static boolean isValidPersistenceType(String type) {
        return isValidAtlasType(type, persistenceType);
    }

    public static boolean isValidHealthMonitorType(String type) {
        return isValidAtlasType(type, healthMonitorType);
    }

    private static boolean isValidAtlasType(String string, AtlasType atlasType) {
        boolean isValidString = false;
        for (int i = 0; i < atlasType.toList().length; i++) {
            if (atlasType.toList()[i].equals(string)) {
                isValidString = true;
                break;
            }
        }

        return isValidString;
    }
}
