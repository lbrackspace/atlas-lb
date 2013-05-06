package org.openstack.atlas.usagerefactor.collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.UsageEventCollectionException;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.snmp.StingrayUsageClient;
import org.openstack.atlas.usagerefactor.snmp.StingrayUsageClientImpl;

import java.util.concurrent.Callable;

public class SnmpVSCollector implements Callable<SnmpUsage> {
    private final Log LOG = LogFactory.getLog(SnmpVSCollector.class);

    public StingrayUsageClient stingrayUsageClient;
    public Host host;
    public LoadBalancer lb;

    public SnmpVSCollector(Host host, LoadBalancer lb) {
        stingrayUsageClient = new StingrayUsageClientImpl();
        this.host = host;
        this.lb = lb;
    }

    public SnmpVSCollector() {
    }

    @Override
    public SnmpUsage call() throws UsageEventCollectionException {
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
            throw new UsageEventCollectionException(retString, e);

        }
        return snmpusage;
    }

    public StingrayUsageClient getStingrayUsageClient() {
        return stingrayUsageClient;
    }

    public void setStingrayUsageClient(StingrayUsageClient stingrayUsageClient) {
        this.stingrayUsageClient = stingrayUsageClient;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public LoadBalancer getLb() {
        return lb;
    }

    public void setLb(LoadBalancer lb) {
        this.lb = lb;
    }
}