package org.openstack.atlas.api.mapper.dozer.converter;

import org.dozer.CustomConverter;
import org.openstack.atlas.core.api.v1.IpVersion;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.NoMappableConstantException;

public class EnumConverter implements CustomConverter {

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class destinationClass, Class sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        // LoadBalancerProtocol -> String
        if (sourceClass == LoadBalancerProtocol.class && destinationClass == String.class) {
            return ((LoadBalancerProtocol) sourceFieldValue).name();
        }

        // String -> LoadBalancerProtocol
        if (sourceClass == String.class && destinationClass ==  LoadBalancerProtocol.class) {
            return LoadBalancerProtocol.valueOf((String)sourceFieldValue);
        }

        // LoadBalancerStatus -> String
        if (sourceFieldValue instanceof LoadBalancerStatus && destinationClass == String.class) {
            return ((LoadBalancerStatus)sourceFieldValue).toString();
        }

        // String -> LoadBalancerStatus
        if(sourceFieldValue instanceof String && destinationClass == LoadBalancerStatus.class) {
            return LoadBalancerStatus.valueOf((String)sourceFieldValue);
        }

        // String -> AlgorithmType
        if(sourceFieldValue instanceof String && destinationClass == LoadBalancerAlgorithm.class) {
            return LoadBalancerAlgorithm.valueOf((String)sourceFieldValue);
        }

        // AlgorithmType -> String
        if(sourceFieldValue instanceof LoadBalancerAlgorithm && destinationClass == String.class) {
            return ((LoadBalancerAlgorithm)sourceFieldValue).toString();
        }

        if (sourceFieldValue instanceof VirtualIp) {
            return IpVersion.IPV4;
        }

        if (sourceFieldValue instanceof IpVersion) {
            return org.openstack.atlas.service.domain.entity.IpVersion.fromDataType((IpVersion) sourceFieldValue);
        }

        // NodeCondition -> String
        if (sourceClass == NodeCondition.class && destinationClass == String.class) {
            return ((NodeCondition) sourceFieldValue).name();
        }

        // String -> NodeCondition
        if (sourceClass == String.class && destinationClass ==  NodeCondition.class) {
            return NodeCondition.valueOf((String)sourceFieldValue);
        }

        // NodeStatus -> String
        if (sourceClass == NodeStatus.class && destinationClass == String.class) {
            return ((NodeStatus) sourceFieldValue).name();
        }

        // String -> NodeStatus
        if (sourceClass == String.class && destinationClass ==  NodeStatus.class) {
            return NodeStatus.valueOf((String)sourceFieldValue);
        }

        // HealthMonitorType -> String
        if (sourceClass == HealthMonitorType.class && destinationClass == String.class) {
            return ((HealthMonitorType) sourceFieldValue).name();
        }

        // String -> HealthMonitorType
        if (sourceClass == String.class && destinationClass ==  HealthMonitorType.class) {
            return HealthMonitorType.valueOf((String)sourceFieldValue);
        }

        // LoadBalancerStatus -> String
        if (sourceFieldValue instanceof SessionPersistence && destinationClass == String.class) {
            return ((SessionPersistence)sourceFieldValue).toString();
        }

        // String -> LoadBalancerStatus
        if(sourceFieldValue instanceof String && destinationClass == SessionPersistence.class) {
            return SessionPersistence.valueOf((String)sourceFieldValue);
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}
