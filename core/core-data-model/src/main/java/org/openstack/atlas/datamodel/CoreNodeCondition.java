package org.openstack.atlas.datamodel;

import java.util.HashSet;
import java.util.Set;

public class CoreNodeCondition implements NodeCondition {
    public static final String ENABLED = "ENABLED";
    public static final String DISABLED = "DISABLED";
    protected static final Set<String> nodeConditions;

    static {
        nodeConditions = new HashSet<String>();
        nodeConditions.add(ENABLED);
        nodeConditions.add(DISABLED);
    }

    public CoreNodeCondition() {
    }

    public boolean contains(String str) {
        boolean out;
        out = nodeConditions.contains(str);
        return out;
    }

    public static String[] values() {
        return nodeConditions.toArray(new String[nodeConditions.size()]);
    }

    @Override
    public String[] toList() {
        return nodeConditions.toArray(new String[nodeConditions.size()]);
    }
}
