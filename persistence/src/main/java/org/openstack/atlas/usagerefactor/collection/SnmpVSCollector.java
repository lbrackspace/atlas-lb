package org.openstack.atlas.usagerefactor.collection;

import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.StingrayUsageClient;
import org.openstack.atlas.usagerefactor.StingrayUsageClientImpl;

import java.util.concurrent.Callable;

public class SnmpVSCollector implements Callable<SnmpUsage> {
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
            System.out.println("Thread has run.");
        } catch (Exception e) {
            String retString = String.format("Request for host %s usage from SNMP server failed.", host.getName());
            System.out.println(e);
        }
        return snmpusage;
    }
}