package org.openstack.atlas.service.domain.operation;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Scope("request")
public class CoreOperation implements Operation {
    public static final String CREATE_LOADBALANCER = "CREATE_LOADBALANCER";
    public static final String UPDATE_LOADBALANCER = "UPDATE_LOADBALANCER";
    public static final String DELETE_LOADBALANCER = "DELETE_LOADBALANCER";
    public static final String CREATE_NODES = "CREATE_NODES";
    public static final String UPDATE_NODE = "UPDATE_NODE";
    public static final String DELETE_NODES = "DELETE_NODES";
    public static final String UPDATE_CONNECTION_THROTTLE = "UPDATE_CONNECTION_THROTTLE";
    public static final String DELETE_CONNECTION_THROTTLE = "DELETE_CONNECTION_THROTTLE";
    public static final String UPDATE_HEALTH_MONITOR = "UPDATE_HEALTH_MONITOR";
    public static final String DELETE_HEALTH_MONITOR = "DELETE_HEALTH_MONITOR";
    public static final String UPDATE_SESSION_PERSISTENCE = "UPDATE_SESSION_PERSISTENCE";
    public static final String DELETE_SESSION_PERSISTENCE = "DELETE_SESSION_PERSISTENCE";
    private static final Set<String> operationTypes;

    static {
        operationTypes = new HashSet<String>();
        operationTypes.add(CREATE_LOADBALANCER);
        operationTypes.add(UPDATE_LOADBALANCER);
        operationTypes.add(DELETE_LOADBALANCER);
        operationTypes.add(CREATE_NODES);
        operationTypes.add(UPDATE_NODE);
        operationTypes.add(DELETE_NODES);
        operationTypes.add(UPDATE_CONNECTION_THROTTLE);
        operationTypes.add(DELETE_CONNECTION_THROTTLE);
        operationTypes.add(UPDATE_HEALTH_MONITOR);
        operationTypes.add(DELETE_HEALTH_MONITOR);
        operationTypes.add(UPDATE_SESSION_PERSISTENCE);
        operationTypes.add(DELETE_SESSION_PERSISTENCE);
    }

    @Override
    public String[] toList() {
        return new String[0];
    }
    
    protected static void add(String operation) {
        operationTypes.add(operation);
    }

    public boolean contains(String str) {
        boolean out;
        out = operationTypes.contains(str);
        return out;
    }

    public static String[] values() {
        return operationTypes.toArray(new String[operationTypes.size()]);
    }
}
