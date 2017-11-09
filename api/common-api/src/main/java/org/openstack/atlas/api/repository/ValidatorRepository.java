package org.openstack.atlas.api.repository;

import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.openstack.atlas.api.validation.validators.*;

public final class ValidatorRepository {

    public static <R> ResourceValidator<R> getValidatorFor(Class<R> vClass) {
        if (vClass == LoadBalancer.class) {
            return (ResourceValidator<R>) new LoadBalancerValidator();
        } else if (vClass == AccessList.class) {
            return (ResourceValidator<R>) new AccessListValidator();
        } else if (vClass == HealthMonitor.class) {
            return (ResourceValidator<R>) new HealthMonitorValidator();
        } else if (vClass == NetworkItem.class) {
            return (ResourceValidator<R>) new NetworkItemValidator();
        } else if (vClass == Meta.class) {
            return (ResourceValidator<R>) new MetaValidator();
        } else if (vClass == Metadata.class) {
            return (ResourceValidator<R>) new MetadataValidator();
        } else if (vClass == Nodes.class) {
            return (ResourceValidator<R>) new NodesValidator();
        } else if (vClass == Node.class) {
            return (ResourceValidator<R>) new NodeValidator();
        } else if (vClass == SessionPersistence.class) {
            return (ResourceValidator<R>) new SessionPersistenceValidator();
        } else if (vClass == VirtualIps.class) {
            return (ResourceValidator<R>) new VirtualIpsValidator();
        } else if (vClass == VirtualIp.class) {
            return (ResourceValidator<R>) new VirtualIpValidator();
        } else if (vClass == ConnectionThrottle.class) {
            return (ResourceValidator<R>) new ConnectionThrottleValidator();
        } else if (vClass == ConnectionLogging.class) {
            return (ResourceValidator<R>) new ConnectionLoggingValidator();
        } else if (vClass == ContentCaching.class) {
            return (ResourceValidator<R>) new ContentCachingValidator();
        } else if (vClass == SslTermination.class) {
            return (ResourceValidator<R>) new SslTerminationValidator();
        } else if (vClass == CertificateMapping.class) {
            return (ResourceValidator<R>) new CertificateMappingValidator();
        }
        throw new NullPointerException(String.format("No Validator registered in repository for Class: %s", vClass.getName()));
    }
}
