package org.openstack.atlas.usagerefactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.util.snmp.RawSnmpUsage;
import org.openstack.atlas.util.snmp.StingraySnmpClient;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpGeneralException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpRetryExceededException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpSetupException;

public class StingrayUsageClientImpl implements StingrayUsageClient {

    public static final Pattern vsPattern = Pattern.compile("[0-9]+_([0-9]+)");
    public static final Pattern shadowPattern = Pattern.compile("[0-9]+_([0-9]+)_S");

    @Override
    public Map<Integer, SnmpUsage> getHostUsage(Host host) throws StingraySnmpRetryExceededException, StingraySnmpSetupException, StingraySnmpGeneralException  {
        Map<Integer, SnmpUsage> usage = new HashMap<Integer, SnmpUsage>();
        String snmpIp = host.getManagementIp();
        StingraySnmpClient client = new StingraySnmpClient();
        client.setAddress(snmpIp);
        Map<String, RawSnmpUsage> rawMap = client.getSnmpUsage();
        for (RawSnmpUsage rawValue : rawMap.values()) {
            String vsName = rawValue.getVsName();
            Matcher m;
            m = vsPattern.matcher(vsName);
            if (m.find()) {
                Integer loadbalancerId = Integer.valueOf(m.group(1));
                if (!usage.containsKey(loadbalancerId)) {
                    SnmpUsage newUsage = new SnmpUsage();
                    newUsage.setLoadbalancerId(loadbalancerId);
                    newUsage.setHostId(host.getId());
                    usage.put(loadbalancerId, newUsage);
                }
                usage.get(loadbalancerId).setBytesIn(rawValue.getBytesIn());
                usage.get(loadbalancerId).setBytesOut(rawValue.getBytesOut());
                usage.get(loadbalancerId).setConcurrentConnections((int) rawValue.getConcurrentConnections());
            }
            m = shadowPattern.matcher(vsName);
            if (m.find()) {
                Integer loadbalancerId = Integer.valueOf(m.group(1));
                if (!usage.containsKey(m.group(1))) {
                    SnmpUsage newUsage = new SnmpUsage();
                    newUsage.setLoadbalancerId(loadbalancerId);
                    newUsage.setHostId(host.getId());
                    usage.put(loadbalancerId,newUsage);
                }
                usage.get(loadbalancerId).setBytesInSsl(rawValue.getBytesIn());
                usage.get(loadbalancerId).setBytesOutSsl(rawValue.getBytesOut());
                usage.get(loadbalancerId).setConcurrentConnectionsSsl((int)rawValue.getConcurrentConnections());
            }
        }
        return usage;
    }

    @Override
    public SnmpUsage getVirtualServerUsage(Host host, LoadBalancer lb) throws StingraySnmpSetupException, StingraySnmpGeneralException {
        SnmpUsage usage = new SnmpUsage();
        StingraySnmpClient client = new StingraySnmpClient();
        client.setAddress(host.getManagementIp());

        // Fetch Virtual Server Usage
        String vsName = buildVsName(lb,false);
        usage.setBytesIn(client.getBytesIn(vsName));
        usage.setBytesOut(client.getBytesOut(vsName));
        usage.setConcurrentConnections((int)client.getConcurrentConnections(vsName));

        // Fetch Shadow Server Usage
        String shadowName = buildVsName(lb,true);
        usage.setBytesInSsl(client.getBytesIn(shadowName));
        usage.setBytesOutSsl(client.getBytesOut(shadowName));
        usage.setConcurrentConnectionsSsl((int)client.getConcurrentConnections(shadowName));
        return usage;
    }

    private static String buildVsName(LoadBalancer lb, boolean isShadowServer) {
        StringBuilder sb = new StringBuilder();
        sb.append(lb.getAccountId()).append("_").append(lb.getId());
        if (isShadowServer) {
            sb.append("_S");
        }
        return sb.toString();
    }
}
