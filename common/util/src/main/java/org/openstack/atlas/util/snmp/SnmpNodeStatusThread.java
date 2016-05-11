package org.openstack.atlas.util.snmp;

import java.util.HashMap;
import java.util.Map;
import org.openstack.atlas.util.snmp.SnmpNodeKey;
import org.openstack.atlas.util.snmp.SnmpNodeStatus;
import org.openstack.atlas.util.common.VerboseLogger;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpGeneralException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpSetupException;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;

public class SnmpNodeStatusThread extends Thread {

    private double elapsedTime;
    private StingraySnmpClient client;
    private static VerboseLogger vlog = new VerboseLogger(SnmpNodeStatusThread.class);
    private Exception exception = null;
    private Map<SnmpNodeKey, SnmpNodeStatus> statusMap;

    static {
    }

    @Override
    public void run() {
        try {
            double startTime = StaticDateTimeUtils.getEpochSeconds();
            statusMap = client.getSnmpNodeStatusList();
            double endTime = StaticDateTimeUtils.getEpochSeconds();
            elapsedTime = endTime - startTime;
        } catch (Exception ex) {
            vlog.setLevel(VerboseLogger.LogLevel.ERROR);
            String ipAndPort = String.format("%s %s", client.getAddress(), client.getPort());
            vlog.printf("Error fetching node static on client %s: %s\n", ipAndPort, Debug.getExtendedStackTrace(ex));
            vlog.setLevel(VerboseLogger.LogLevel.INFO);
        }
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(double elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public StingraySnmpClient getClient() {
        return client;
    }

    public void setClient(StingraySnmpClient client) {
        this.client = client;
    }

    public static VerboseLogger getVlog() {
        return vlog;
    }

    public static void setVlog(VerboseLogger aVlog) {
        vlog = aVlog;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Map<SnmpNodeKey, SnmpNodeStatus> getStatusMap() {
        if(statusMap == null){
            statusMap = new HashMap<SnmpNodeKey, SnmpNodeStatus>();
        }
        return statusMap;
    }

    public void setStatusMap(Map<SnmpNodeKey, SnmpNodeStatus> statusMap) {
        this.statusMap = statusMap;
    }

    public String getClientKey() {
        if (client != null && client.getAddress() != null && client.getPort() != null) {
            return String.format("%s:%s", client.getAddress(), client.getPort());
        }
        return "null";
    }
}
