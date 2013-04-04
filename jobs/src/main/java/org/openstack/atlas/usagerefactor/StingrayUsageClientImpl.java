package org.openstack.atlas.usagerefactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.util.snmp.StingraySnmpClient;

public class StingrayUsageClientImpl implements StingrayUsageClient{
    public static final Pattern vsPattern = Pattern.compile("(.*)_(.*)");
    public static final Pattern shadowPattern = Pattern.compile("(.*)_(.*)_S");
    
    @Override
    public Map<Integer, SnmpUsage> getHostUsage(Host host) {
        Map<Integer,SnmpUsage> usage = new HashMap<Integer,SnmpUsage>();
        String snmpIp = host.getManagementIp();
        StingraySnmpClient client = new StingraySnmpClient();
        return usage;
    }

    @Override
    public SnmpUsage getVirtualServerUsage(Host host, LoadBalancer lb) {
        SnmpUsage usage = new SnmpUsage();
        return usage;
    }

}
