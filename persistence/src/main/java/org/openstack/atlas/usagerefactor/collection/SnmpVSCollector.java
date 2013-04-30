package org.openstack.atlas.usagerefactor.collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.StingrayUsageClient;
import org.openstack.atlas.usagerefactor.StingrayUsageClientImpl;

import java.util.concurrent.Callable;

public class SnmpVSCollector implements Callable<SnmpUsage> {
    private final Log LOG = LogFactory.getLog(SnmpVSCollector.class);

    public final StingrayUsageClient stingrayUsageClient;
    public final Host host;
    public final LoadBalancer lb;

    public SnmpVSCollector(Host host, LoadBalancer lb) {
        stingrayUsageClient = new StingrayUsageClientImpl();
        this.host = host;
        this.lb = lb;
    }

    @Override
    public SnmpUsage call() {
        SnmpUsage snmpusage = null;
        try {
            snmpusage = stingrayUsageClient.getVirtualServerUsage(host, lb);
        } catch (Exception e) {
            String hostname;
            if (host.getName() == null) {
                hostname = "NULL-HOST";
            } else {
                hostname = host.getName();
            }
            String retString = String.format("Request for host %s usage from SNMP server failed.", hostname);
            LOG.error(retString, e);

        }
        return snmpusage;
    }
}