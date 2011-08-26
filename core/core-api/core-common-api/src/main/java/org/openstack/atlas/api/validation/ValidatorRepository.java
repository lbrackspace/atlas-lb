package org.openstack.atlas.api.validation;

import org.openstack.atlas.core.api.v1.*;
import org.openstack.atlas.api.validation.validator.*;

import java.util.HashMap;
import java.util.Map;

public final class ValidatorRepository {
    private static final Map<Class, ResourceValidator> classKeyedValidatorMap = new HashMap<Class, ResourceValidator>();

    public static <R> ResourceValidator<R> getValidatorFor(Class<R> classOfObjectToValidate) {
        classKeyedValidatorMap.put(HealthMonitor.class, new HealthMonitorValidator());
        classKeyedValidatorMap.put(Node.class, new NodeValidator());
        classKeyedValidatorMap.put(VirtualIp.class, new VirtualIpValidator());
        classKeyedValidatorMap.put(ConnectionThrottle.class, new ConnectionThrottleValidator());

        if (!classKeyedValidatorMap.containsKey(classOfObjectToValidate)) {
            throw new NullPointerException(String.format("No Validator registered in repository for Class: %s", classOfObjectToValidate.getName()));
        }
        return classKeyedValidatorMap.get(classOfObjectToValidate);
    }
}
