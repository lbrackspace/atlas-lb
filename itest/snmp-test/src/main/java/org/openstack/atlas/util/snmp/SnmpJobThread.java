package org.openstack.atlas.util.snmp;

import java.util.Map;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.StingrayUsageClient;
import org.openstack.atlas.util.common.VerboseLogger;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpGeneralException;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public class SnmpJobThread extends Thread {

    public static VerboseLogger getVlog() {
        return vlog;
    }

    private StingrayUsageClient client;
    private static final VerboseLogger vlog = new VerboseLogger(SnmpJobThread.class);
    private Exception exception = null;
    private Host host;
    private Map<Integer, SnmpUsage> usage;

    @Override
    public void run() {
        try {
            usage = client.getHostUsage(host);
        } catch (Exception ex) {
            String msg = String.format("Thread for client host %s throw an exceotion: %s", host.toString(), StaticStringUtils.getExtendedStackTrace(ex));
            exception = new StingraySnmpGeneralException(msg, ex);
            return;
        }
    }

    public StingrayUsageClient getClient() {
        return client;
    }

    public void setClient(StingrayUsageClient client) {
        this.client = client;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public Map<Integer, SnmpUsage> getUsage() {
        return usage;
    }

    public void setUsage(Map<Integer, SnmpUsage> usage) {
        this.usage = usage;
    }

}
