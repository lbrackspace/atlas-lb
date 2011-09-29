package org.openstack.atlas.api.mapper.dozer.converter;

import org.openstack.atlas.docs.loadbalancers.api.v1.IpVersion;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItemType;
import org.openstack.atlas.docs.loadbalancers.api.v1.PersistenceType;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.NoMappableConstantException;
import org.dozer.CustomConverter;

public class EnumCustomConverter implements CustomConverter {

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class destinationClass, Class sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        // LoadBalancerProtocol -> String
        if (sourceClass == LoadBalancerProtocol.class && destinationClass == java.lang.String.class) {
            return ((LoadBalancerProtocol) sourceFieldValue).name();
        }

        // String -> LoadBalancerProtocol
        if (sourceClass == java.lang.String.class && destinationClass ==  LoadBalancerProtocol.class) {
            return LoadBalancerProtocol.valueOf((String)sourceFieldValue);
        }

        // LoadBalancerStatus -> String
        if (sourceFieldValue instanceof LoadBalancerStatus && destinationClass == java.lang.String.class) {
            return ((LoadBalancerStatus)sourceFieldValue).toString();
        }

        // String -> LoadBalancerStatus
        if(sourceFieldValue instanceof java.lang.String && destinationClass == LoadBalancerStatus.class) {
            return LoadBalancerStatus.valueOf((String)sourceFieldValue);
        }

        // String -> AlgorithmType
        if(sourceFieldValue instanceof java.lang.String && destinationClass == LoadBalancerAlgorithm.class) {
            return LoadBalancerAlgorithm.valueOf((String)sourceFieldValue);
        }

        // AlgorithmType -> String
        if(sourceFieldValue instanceof LoadBalancerAlgorithm && destinationClass == java.lang.String.class) {
            return ((LoadBalancerAlgorithm)sourceFieldValue).toString();
        }

        if (sourceFieldValue instanceof VirtualIp) {
            return IpVersion.IPV4;
        }

        if (sourceFieldValue instanceof IpVersion) {
            return org.openstack.atlas.service.domain.entities.IpVersion.fromDataType((IpVersion) sourceFieldValue);
        }

        if (sourceFieldValue instanceof AccessListType) {
            return ((AccessListType) sourceFieldValue).getDataType();
        }

        if (sourceFieldValue instanceof NetworkItemType) {
            return AccessListType.fromDataType((NetworkItemType) sourceFieldValue);
        }

        if (sourceFieldValue instanceof SessionPersistence) {
            return ((SessionPersistence) sourceFieldValue).getDataType();
        }

        if (sourceFieldValue instanceof PersistenceType) {
            return SessionPersistence.fromDataType((PersistenceType) sourceFieldValue);
        }

        if(sourceFieldValue instanceof String && destinationClass == AccountLimitType.class) {
            return AccountLimitType.valueOf((String) sourceFieldValue);
        }

        if (sourceFieldValue instanceof AccountLimitType && destinationClass == java.lang.String.class) {
            return ((AccountLimitType)sourceFieldValue).toString();
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}
