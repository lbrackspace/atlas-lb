package org.openstack.atlas.usage.execution;

import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.usagerefactor.StingrayUsageClient;
import org.openstack.atlas.usagerefactor.StingrayUsageClientImpl;

import java.util.concurrent.Callable;

public class Worker implements Callable<Object> {
    public final StingrayUsageClient stingrayUsageClient;
    public final Host host;

    public Worker(Host host) {
        stingrayUsageClient = new StingrayUsageClientImpl();
        this.host = host;
    }

    @Override
    public Object call() {
        try {
            return stingrayUsageClient.getHostUsage(host);
        } catch (Exception e) {
            String retString = String.format("Request for host %s usage from SNMP server failed.", host.getName());
//            LOG.error(retString, e);
        }
        return null;
    }
}