package org.openstack.atlas.datamodel;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Scope("singleton")
public class CoreNodeStatus implements NodeStatus {
    public static final String ONLINE = "ONLINE";
    public static final String OFFLINE = "OFFLINE";
    private static final Set<String> nodeStatuses;

    static {
        nodeStatuses = new HashSet<String>();
        nodeStatuses.add(ONLINE);
        nodeStatuses.add(OFFLINE);
    }

    public boolean contains(String str) {
        return nodeStatuses.contains(str);
    }

    public static String[] values() {
        return nodeStatuses.toArray(new String[nodeStatuses.size()]);
    }

    @Override
    public String[] toList() {
        return nodeStatuses.toArray(new String[nodeStatuses.size()]);
    }

    protected static void add(String nodeStatus) {
        nodeStatuses.add(nodeStatus);
    }

}
