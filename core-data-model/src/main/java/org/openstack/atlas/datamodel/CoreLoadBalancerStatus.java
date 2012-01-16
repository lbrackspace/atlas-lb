package org.openstack.atlas.datamodel;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Scope("singleton")
public class CoreLoadBalancerStatus implements LoadBalancerStatus {
    public static final String QUEUED = "QUEUED";
    public static final String BUILD = "BUILD";
    public static final String ACTIVE = "ACTIVE";
    public static final String PENDING_UPDATE = "PENDING_UPDATE";
    public static final String SUSPENDED = "SUSPENDED";
    public static final String PENDING_DELETE = "PENDING_DELETE";
    public static final String DELETED = "DELETED";
    public static final String ERROR = "ERROR";
    protected static final Set<String> loadBalancerStatuses;

    static {
        loadBalancerStatuses = new HashSet<String>();
        loadBalancerStatuses.add(QUEUED);
        loadBalancerStatuses.add(BUILD);
        loadBalancerStatuses.add(ACTIVE);
        loadBalancerStatuses.add(PENDING_UPDATE);
        loadBalancerStatuses.add(SUSPENDED);
        loadBalancerStatuses.add(PENDING_DELETE);
        loadBalancerStatuses.add(DELETED);
        loadBalancerStatuses.add(ERROR);
    }

    public boolean contains(String str) {
        return loadBalancerStatuses.contains(str);
    }

    public static String[] values() {
        return loadBalancerStatuses.toArray(new String[loadBalancerStatuses.size()]);
    }

    @Override
    public String[] toList() {
        return loadBalancerStatuses.toArray(new String[loadBalancerStatuses.size()]);
    }
}
