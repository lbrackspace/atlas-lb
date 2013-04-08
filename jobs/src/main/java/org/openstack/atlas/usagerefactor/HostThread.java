package org.openstack.atlas.usagerefactor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;

public class HostThread implements Runnable {
    final Log LOG = LogFactory.getLog(HostThread.class);
    public final StingrayUsageClient stingrayUsageClient;
    public final Host host;

    public HostThread(Host host) {
        stingrayUsageClient = new StingrayUsageClientImpl();
        this.host = host;
    }

    @Override
    public void run() {
        try {
            stingrayUsageClient.getHostUsage(host);
            System.out.println("Thread has run.");
        } catch (Exception e) {
            String retString = String.format("Request for host %s usage from SNMP server failed.", host.getName());
            LOG.error(retString, e);
        }
    }
}