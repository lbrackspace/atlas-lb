package org.openstack.atlas.api.repository;

import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.openstack.atlas.api.validation.validators.*;

import java.util.HashMap;
import java.util.Map;

public final class ValidatorRepository {
    private static final Map<Class, ResourceValidator> classKeyedValidatorMap = new HashMap<Class, ResourceValidator>();

    public static <R> ResourceValidator<R> getValidatorFor(Class<R> classOfObjectToValidate) {
        classKeyedValidatorMap.put(LoadBalancer.class, new LoadBalancerValidator());
        classKeyedValidatorMap.put(AccessList.class, new AccessListValidator());
        classKeyedValidatorMap.put(HealthMonitor.class, new HealthMonitorValidator());
        classKeyedValidatorMap.put(NetworkItem.class, new NetworkItemValidator());
        classKeyedValidatorMap.put(Meta.class, new MetaValidator());
        classKeyedValidatorMap.put(Metadata.class, new MetadataValidator());
        classKeyedValidatorMap.put(Nodes.class, new NodesValidator());
        classKeyedValidatorMap.put(Node.class, new NodeValidator());
        classKeyedValidatorMap.put(SessionPersistence.class, new SessionPersistenceValidator());
        classKeyedValidatorMap.put(VirtualIps.class, new VirtualIpsValidator());
        classKeyedValidatorMap.put(VirtualIp.class, new VirtualIpValidator());
        classKeyedValidatorMap.put(ConnectionThrottle.class, new ConnectionThrottleValidator());
        classKeyedValidatorMap.put(ConnectionLogging.class, new ConnectionLoggingValidator());
        classKeyedValidatorMap.put(ContentCaching.class, new ContentCachingValidator());
        classKeyedValidatorMap.put(SslTermination.class, new SslTerminationValidator());

        if (!classKeyedValidatorMap.containsKey(classOfObjectToValidate)) {
            throw new NullPointerException(String.format("No Validator registered in repository for Class: %s", classOfObjectToValidate.getName()));
        }
        return classKeyedValidatorMap.get(classOfObjectToValidate);
    }
}
