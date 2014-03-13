package org.openstack.atlas.usagerefactor.snmp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.util.snmp.RawSnmpUsage;
import org.openstack.atlas.util.snmp.StingraySnmpClient;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpGeneralException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpObjectNotFoundException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpRetryExceededException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpSetupException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;

public class StingrayUsageClientImpl implements StingrayUsageClient {

    private final Log LOG = LogFactory.getLog(StingrayUsageClientImpl.class);

    public static final Pattern vsPattern = Pattern.compile("^[0-9]+_([0-9]+)$");
    public static final Pattern shadowPattern = Pattern.compile("^[0-9]+_([0-9]+)_S$");

    @Override
    public Map<Integer, SnmpUsage> getHostUsage(Host host) throws StingraySnmpRetryExceededException, StingraySnmpSetupException, StingraySnmpGeneralException {
        RestApiConfiguration configuration = new RestApiConfiguration();
        boolean log_all = configuration.hasKeys(PublicApiServiceConfigurationKeys.usage_poller_log_all_counters) &&
                (configuration.getString(PublicApiServiceConfigurationKeys.usage_poller_log_all_counters).toLowerCase().equals("enabled"));
        Map<Integer, SnmpUsage> usage = new HashMap<Integer, SnmpUsage>();
        String snmpIp = host.getManagementIp();
        StingraySnmpClient client = new StingraySnmpClient();
        client.setAddress(snmpIp);
        Map<String, RawSnmpUsage> rawMap = client.getSnmpUsage();
        StringBuilder counterLogString = new StringBuilder();
        counterLogString.append("\n");
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
                if (log_all) {
                    counterLogString.append(String.format("Host_ID: %d, VirtualServer: %s, BytesIn: %d, BytesOut: %d, ConcurrentConnections: %d\n",
                                            host.getId(), vsName, rawValue.getBytesIn(), rawValue.getBytesOut(), rawValue.getConcurrentConnections()));
                }
            }
            m = shadowPattern.matcher(vsName);
            if (m.find()) {
                Integer loadbalancerId = Integer.valueOf(m.group(1));
                if (!usage.containsKey(loadbalancerId)) {
                    SnmpUsage newUsage = new SnmpUsage();
                    newUsage.setLoadbalancerId(loadbalancerId);
                    newUsage.setHostId(host.getId());
                    usage.put(loadbalancerId, newUsage);
                }
                usage.get(loadbalancerId).setBytesInSsl(rawValue.getBytesIn());
                usage.get(loadbalancerId).setBytesOutSsl(rawValue.getBytesOut());
                usage.get(loadbalancerId).setConcurrentConnectionsSsl((int) rawValue.getConcurrentConnections());
                if (log_all) {
                    counterLogString.append(String.format("Host_ID: %d, VirtualServer: %s, BytesInSsl: %d, BytesOutSsl: %d, ConcurrentConnectionsSsl: %d\n",
                                            host.getId(), vsName, rawValue.getBytesIn(), rawValue.getBytesOut(), rawValue.getConcurrentConnections()));
                }
            }
        }
        LOG.debug(counterLogString.toString());
        return usage;
    }

    @Override
    public SnmpUsage getVirtualServerUsage(Host host, LoadBalancer lb) throws StingraySnmpSetupException, StingraySnmpObjectNotFoundException, StingraySnmpGeneralException {
        SnmpUsage usage = new SnmpUsage();
        StingraySnmpClient client = new StingraySnmpClient();
        client.setAddress(host.getManagementIp());

        usage.setLoadbalancerId(lb.getId());
        usage.setHostId(host.getId());
        // Fetch Virtual Server Usage
        String vsName = buildVsName(lb, false);
        usage.setBytesIn(client.getBytesIn(vsName, false, true));
        usage.setBytesOut(client.getBytesOut(vsName, false, true));
        usage.setConcurrentConnections((int) client.getConcurrentConnections(vsName, false, true));

        // Fetch Shadow Server Usage
        String shadowName = buildVsName(lb, true);
        usage.setBytesInSsl(client.getBytesIn(shadowName, false, true));
        usage.setBytesOutSsl(client.getBytesOut(shadowName, false, true));
        usage.setConcurrentConnectionsSsl((int) client.getConcurrentConnections(shadowName, false, true));
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

    @Override
    public List<SnmpUsage> getHostUsageList(Host host) throws StingraySnmpGeneralException {
        return new ArrayList<SnmpUsage>(getHostUsage(host).values());
    }
}
