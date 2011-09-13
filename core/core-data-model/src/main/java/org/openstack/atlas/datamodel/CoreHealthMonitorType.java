package org.openstack.atlas.datamodel;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Scope("request")
public class CoreHealthMonitorType implements HealthMonitorType {
    public static final String CONNECT = "CONNECT";
    public static final String HTTP = "HTTP";
    public static final String HTTPS = "HTTPS";
    protected static final Set<String> healthMonitorTypes;
    private String type;

    static {
        healthMonitorTypes = new HashSet<String>();
        healthMonitorTypes.add(CONNECT);
        healthMonitorTypes.add(HTTP);
        healthMonitorTypes.add(HTTPS);
    }

    public CoreHealthMonitorType() {
    }

    public CoreHealthMonitorType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public boolean contains(String str) {
        boolean out;
        out = healthMonitorTypes.contains(str);
        return out;
    }

    public static String[] values() {
        return healthMonitorTypes.toArray(new String[healthMonitorTypes.size()]);
    }

    @Override
    public String[] toList() {
        return healthMonitorTypes.toArray(new String[healthMonitorTypes.size()]);
    }
}
