package org.openstack.atlas.usagerefactor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class HostThread implements Callable {
    final Log LOG = LogFactory.getLog(HostThread.class);
    public final StingrayUsageClient stingrayUsageClient;
    public final Host host;

    public HostThread(Host host) {
        stingrayUsageClient = new StingrayUsageClientImpl();
        this.host = host;
    }

    @Override
    public Map<Integer, SnmpUsage> call() throws Exception {
        try {
            return stingrayUsageClient.getHostUsage(host);
        } catch (Exception e) {
            String retString = String.format("Request for host %s usage from SNMP server failed.", host.getName());
            LOG.error(retString, e);
        }

        return new HashMap<Integer, SnmpUsage>();
    }
}