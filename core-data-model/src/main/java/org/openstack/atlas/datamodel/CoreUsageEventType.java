package org.openstack.atlas.datamodel;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Scope("singleton")
public class CoreUsageEventType implements UsageEventType {
    public static final String CREATE_CONNECTION_THROTTLE = "CREATE_CONNECTION_THROTTLE";
    public static final String UPDATE_CONNECTION_THROTTLE = "UPDATE_CONNECTION_THROTTLE";
    public static final String DELETE_CONNECTION_THROTTLE = "DELETE_CONNECTION_THROTTLE";
    public static final String CREATE_HEALTH_MONITOR = "CREATE_HEALTH_MONITOR";
    public static final String UPDATE_HEALTH_MONITOR = "UPDATE_HEALTH_MONITOR";
    public static final String DELETE_HEALTH_MONITOR = "DELETE_HEALTH_MONITOR";
    public static final String CREATE_LOAD_BALANCER = "CREATE_LOAD_BALANCER";
    public static final String SUSPEND_LOAD_BALANCER = "SUSPEND_LOAD_BALANCER";
    public static final String UNSUSPEND_LOADBALANCER = "UNSUSPEND_LOADBALANCER";
    public static final String DELETE_LOAD_BALANCER = "DELETE_LOAD_BALANCER";
    public static final String CREATE_NODE = "CREATE_NODE";
    public static final String UPDATE_NODE = "UPDATE_NODE";
    public static final String DELETE_NODE = "DELETE_NODE";
    public static final String CREATE_SESSION_PERSISTENCE = "CREATE_SESSION_PERSISTENCE";
    public static final String UPDATE_SESSION_PERSISTENCE = "UPDATE_SESSION_PERSISTENCE";
    public static final String DELETE_SESSION_PERSISTENCE = "DELETE_SESSION_PERSISTENCE";
    private static final Set<String> usageEventTypes;

    static {
        usageEventTypes = new HashSet<String>();
        usageEventTypes.add(CREATE_CONNECTION_THROTTLE);
        usageEventTypes.add(UPDATE_CONNECTION_THROTTLE);
        usageEventTypes.add(DELETE_CONNECTION_THROTTLE);
        usageEventTypes.add(CREATE_HEALTH_MONITOR);
        usageEventTypes.add(UPDATE_HEALTH_MONITOR);
        usageEventTypes.add(DELETE_HEALTH_MONITOR);
        usageEventTypes.add(CREATE_LOAD_BALANCER);
        usageEventTypes.add(SUSPEND_LOAD_BALANCER);
        usageEventTypes.add(UNSUSPEND_LOADBALANCER);
        usageEventTypes.add(DELETE_LOAD_BALANCER);
        usageEventTypes.add(CREATE_NODE);
        usageEventTypes.add(UPDATE_NODE);
        usageEventTypes.add(DELETE_NODE);
        usageEventTypes.add(CREATE_SESSION_PERSISTENCE);
        usageEventTypes.add(UPDATE_SESSION_PERSISTENCE);
        usageEventTypes.add(DELETE_SESSION_PERSISTENCE);
    }

    public boolean contains(String str) {
        return usageEventTypes.contains(str);
    }

    public static String[] values() {
        return usageEventTypes.toArray(new String[usageEventTypes.size()]);
    }

    @Override
    public String[] toList() {
        return usageEventTypes.toArray(new String[usageEventTypes.size()]);
    }

    protected static void add(String nodeStatus) {
        usageEventTypes.add(nodeStatus);
    }
}
