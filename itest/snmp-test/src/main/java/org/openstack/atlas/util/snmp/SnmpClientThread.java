package org.openstack.atlas.util.snmp;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstack.atlas.util.common.VerboseLogger;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpGeneralException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpRetryExceededException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpSetupException;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public class SnmpClientThread extends Thread {

    private static final VerboseLogger vlog = new VerboseLogger(SnmpClientThread.class);

    public static VerboseLogger getVlog() {
        return vlog;
    }
    private String clientName;
    private StingraySnmpClient client;
    private Map<String, RawSnmpUsage> usage;
    private Exception exception = null;

    @Override
    public void run() {
        try {
            usage = client.getSnmpUsage();
        } catch (Exception ex) {
            String msg = String.format("Thread for client %s throw an exception: %s\n",client.toString(), StaticStringUtils.getExtendedStackTrace(ex));
            exception = new StingraySnmpGeneralException(msg, ex);
            return;
        }
    }

    public StingraySnmpClient getClient() {
        return client;
    }

    public void setClient(StingraySnmpClient client) {
        this.client = client;
    }

    public Map<String, RawSnmpUsage> getUsage() {
        if (usage == null) {
            return new HashMap<String, RawSnmpUsage>();
        }
        return usage;
    }

    public void setUsage(Map<String, RawSnmpUsage> usage) {
        this.usage = usage;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
}
