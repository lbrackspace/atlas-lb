package org.openstack.atlas.util.snmp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.util.common.VerboseLogger;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpGeneralException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpObjectNotFoundException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpRetryExceededException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpSetupException;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;

public class StingraySnmpClient {

    public static long getTimeout() {
        return timeout;
    }

    public static void setTimeout(long aTimeout) {
        timeout = aTimeout;
    }
    private int nonRepeaters = 0;
    private int maxRepetitions = 1000;
    private String address;
    private String port = StingraySnmpConstants.PORT;
    private String community = StingraySnmpConstants.COMMUNITY;
    private long reportUdpCountEveryNMilliSeconds = 1000;
    private int maxRetrys = 13;
    private int version = SnmpConstants.version2c;
    private static final Random rnd = new Random();
    private static final Pattern dotSplitter = Pattern.compile("\\.");
    private static final VerboseLogger vlog = new VerboseLogger(StingraySnmpClient.class);
    private static final Log LOG = LogFactory.getLog(StingraySnmpClient.class);
    private static int requestId;
    private static long timeout = 5000;

    static {
        requestId = Math.abs(rnd.nextInt());
    }

    public static Random getRnd() {
        return rnd;
    }

    public StingraySnmpClient() {
    }

    public StingraySnmpClient(String address) {
        this(address, "1161");
    }

    public StingraySnmpClient(String address, String port) {
        this(address, port, "public");
    }

    public StingraySnmpClient(String address, String port, String community) {
        this.address = address;
        this.port = port;
        this.community = community;
    }

    @Override
    public String toString() {
        return "StringraySnmpClient{address=" + address
                + ", port=" + port
                + ", community=" + community
                + ", maxRetries=" + maxRetrys
                + ", maxRepetitions=" + maxRepetitions
                + ", nonRepeaters=" + nonRepeaters
                + ", curRequestId=" + getRequestId()
                + ", timeOut=" + timeout
                + "}";
    }

    public synchronized static int incRequestId() {
        requestId = (requestId + 1) % Integer.MAX_VALUE;
        return requestId;
    }

    public Map<String, Long> getLongOidVals(String oid) throws StingraySnmpSetupException, StingraySnmpGeneralException {
        vlog.printf("In call getLongOidVals(%s)", oid);
        Map<String, Long> oidMap = new HashMap<String, Long>();
        List<VariableBinding> bindings = getBulkOidBindingList(oid);
        for (VariableBinding vb : bindings) {
            String vsName = getVirtualServerNameFromOid(oid, vb.getOid().toString());
            oidMap.put(vsName, new Long(vb.getVariable().toLong()));
        }
        return oidMap;
    }

    public Map<String, RawSnmpUsage> getSnmpUsage() throws StingraySnmpSetupException, StingraySnmpRetryExceededException, StingraySnmpGeneralException {
        vlog.printf("in call to getSnmpUsage()");
        Map<String, RawSnmpUsage> rawSnmpMap = new HashMap<String, RawSnmpUsage>();
        List<VariableBinding> bindings;

        // Fetch Current Connections
        bindings = getBulkOidBindingList(OIDConstants.VS_CURRENT_CONNECTIONS);
        for (VariableBinding vb : bindings) {
            String vsName = getVirtualServerNameFromOid(OIDConstants.VS_CURRENT_CONNECTIONS, vb.getOid().toString());
            if (!rawSnmpMap.containsKey(vsName)) {
                RawSnmpUsage entry = new RawSnmpUsage();
                entry.setVsName(vsName);
                rawSnmpMap.put(vsName, entry);
            }
            rawSnmpMap.get(vsName).setConcurrentConnections(vb.getVariable().toLong());
        }

        // Fetch BytesIn In
        bindings = getBulkOidBindingList(OIDConstants.VS_BYTES_IN);
        for (VariableBinding vb : bindings) {
            String vsName = getVirtualServerNameFromOid(OIDConstants.VS_BYTES_IN, vb.getOid().toString());
            if (!rawSnmpMap.containsKey(vsName)) {
                RawSnmpUsage entry = new RawSnmpUsage();
                entry.setVsName(vsName);
                rawSnmpMap.put(vsName, entry);
            }
            rawSnmpMap.get(vsName).setBytesIn(vb.getVariable().toLong());
        }

        // Fetch Bytes out
        bindings = getBulkOidBindingList(OIDConstants.VS_BYTES_OUT);
        for (VariableBinding vb : bindings) {
            String vsName = getVirtualServerNameFromOid(OIDConstants.VS_BYTES_OUT, vb.getOid().toString());
            if (!rawSnmpMap.containsKey(vsName)) {
                RawSnmpUsage entry = new RawSnmpUsage();
                entry.setVsName(vsName);
                rawSnmpMap.put(vsName, entry);
            }
            rawSnmpMap.get(vsName).setBytesOut(vb.getVariable().toLong());
        }
        return rawSnmpMap;
    }

    public long getBytesIn(String vsName, boolean zeroOnNotFound, boolean negativeOneOnNotFoundException) throws StingraySnmpSetupException, StingraySnmpObjectNotFoundException, StingraySnmpGeneralException {
        return getLongValueForVirtualServer(vsName, OIDConstants.VS_BYTES_IN, zeroOnNotFound, negativeOneOnNotFoundException);
    }

    public long getBytesOut(String vsName, boolean zeroOnNotFound, boolean negativeOneOnNotFoundException) throws StingraySnmpSetupException, StingraySnmpObjectNotFoundException, StingraySnmpGeneralException {
        return getLongValueForVirtualServer(vsName, OIDConstants.VS_BYTES_OUT, zeroOnNotFound, negativeOneOnNotFoundException);
    }

    public long getConcurrentConnections(String vsName, boolean zeroOnNotFound, boolean negativeOneOnNotFoundException) throws StingraySnmpSetupException, StingraySnmpObjectNotFoundException, StingraySnmpGeneralException {
        return getLongValueForVirtualServer(vsName, OIDConstants.VS_CURRENT_CONNECTIONS, zeroOnNotFound, negativeOneOnNotFoundException);
    }

    public long getLongValueForVirtualServer(String vsName, String baseOid, boolean zeroOnNotFoundException, boolean negativeOneOnNotFoundException) throws StingraySnmpSetupException, StingraySnmpObjectNotFoundException, StingraySnmpGeneralException {

        String searchOid = getOidFromVirtualServerName(baseOid, vsName);
        PDU req = new PDU();
        req.add(new VariableBinding(new OID(searchOid)));
        req.setType(PDU.GET);
        req.setRequestID(new Integer32(incRequestId()));
        UdpAddress udpAddr = new UdpAddress(address + "/" + port);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setVersion(version);
        target.setAddress(udpAddr);
        target.setTimeout(timeout);
        TransportMapping transport;
        try {
            transport = new DefaultUdpTransportMapping();
        } catch (IOException ex) {
            String msg = String.format("Error setting up connection to %s/%s for SNMP connections for oid value %s for vs %s", address, port, baseOid, vsName);
            LOG.error(msg, ex);
            throw new StingraySnmpSetupException(msg, ex);
        }
        Snmp snmp = new Snmp(transport);
        try {
            transport.listen();
        } catch (IOException ex) {
            String msg = String.format("Error listening on udp port %s/%s for SNMP connections for oid value %s for vs %s", address, port, baseOid, vsName);
            LOG.error(msg, ex);
            closeConnection(snmp, null);
            throw new StingraySnmpSetupException(msg, ex);
        }
        VariableBinding vb = null;
        ResponseEvent respEvent = null;
        try {
            respEvent = snmp.get(req, target);
        } catch (IOException ex) {
            closeConnection(snmp, transport);
            String msg = String.format("Error getting OID value %s for vs %s at SNMP server %s/%s", baseOid, vsName, address, port);
            LOG.error(msg, ex);
            closeConnection(snmp, transport);
            throw new StingraySnmpGeneralException(msg, ex);
        }
        if (respEvent == null) {
            String msg = String.format("Error response for OID %s for vs %s was null on SNMP server %s/%s", baseOid, vsName, address, port);
            LOG.error(msg);
            closeConnection(snmp, transport);
        }
        PDU resp;
        resp = respEvent.getResponse();
        if (resp == null) {
            String msg = String.format("Error responseEvent for OID %s for vs %s was null on SNMP server %s/%s", baseOid, vsName, address, port);
            LOG.error(msg);
            closeConnection(snmp, transport);
            throw new StingraySnmpGeneralException(msg);
        }
        int respSize = resp.size();
        if (respSize < 1) {
            String msg = String.format("Error response binding size for for OID %s for vs %s was %d on SNMP server", baseOid, vsName, respSize, address, port);
            LOG.error(msg);
            closeConnection(snmp, transport);
            throw new StingraySnmpGeneralException(msg);
        }
        vb = resp.get(0);
        String vbOid = vb.getOid().toString();
        closeConnection(snmp, transport);
        Class vbClass = vb.getVariable().getClass();
        if (vbClass.equals(Null.class)) {
            if (zeroOnNotFoundException) {
                return 0L;
            }
            if (negativeOneOnNotFoundException) {
                return -1L;
            }
            throw new StingraySnmpObjectNotFoundException();
        }
        long val = vb.getVariable().toLong();
        return val;
    }

    public List<VariableBinding> getBulkOidBindingList(String oid) throws StingraySnmpSetupException, StingraySnmpGeneralException {
        vlog.printf("in call getBulkOidBindingList(%s) for %s", oid, getConnectionName());
        List<VariableBinding> bindings = new ArrayList<VariableBinding>();
        String startOID = oid;
        String currOID = startOID;
        int totalItems = 0;
        int matchedItems = 0;
        double currMaxReps = maxRepetitions;
        boolean finished = false;
        while (!finished) {
            vlog.printf("total items = %d matched items= %d from  CONTINUING", totalItems, matchedItems, getConnectionName(oid));
            PDU req = new PDU();
            req.add(new VariableBinding(new OID(currOID)));
            req.setType(PDU.GETBULK);
            req.setNonRepeaters(nonRepeaters);
            req.setMaxRepetitions((int) currMaxReps);
            req.setRequestID(new Integer32(incRequestId()));
            UdpAddress udpAddr;
            try {
                udpAddr = new UdpAddress(address + "/" + port);
            } catch (Exception ex) {
                String msg = String.format("Invalid udpAddress specification %s/%s", address, port);
                LOG.error(msg, ex);
                throw new StingraySnmpSetupException(msg, ex);
            }
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setVersion(version);
            target.setTimeout(timeout);
            target.setAddress(udpAddr);
            TransportMapping transport;
            try {
                transport = new DefaultUdpTransportMapping();
            } catch (IOException ex) {
                String msg = String.format("Error setting up snmp connection to %s", getConnectionName());
                LOG.error(msg, ex);
                throw new StingraySnmpSetupException(msg, ex);
            }
            Snmp snmp = new Snmp(transport);
            try {
                transport.listen();
            } catch (IOException ex) {
                String msg = String.format("Error listening on local udp port for snmp connection %s/%s", address, port);
                LOG.error(msg, ex);
                closeConnection(snmp, transport);
                throw new StingraySnmpSetupException(msg, ex);
            }
            VariableBinding vb = null;
            ResponseEvent respEvent = null;
            try {
                respEvent = snmp.getBulk(req, target);
            } catch (IOException ex) {
                String msg = String.format("Error getting bulk request from snmp server %s", getConnectionName(oid));
                LOG.error(msg, ex);
                closeConnection(snmp, transport);
                throw new StingraySnmpGeneralException(msg, ex);
            }
            PDU respPdu = respEvent.getResponse();
            if (respPdu == null) {
                String msg = String.format("Error fetching bulk response reducing maxRepetitions from %d to %d for %s", (int) currMaxReps, (int) (currMaxReps * 0.75), getConnectionName(oid));
                currMaxReps *= 0.75;
                LOG.warn(msg);
                if (currMaxReps <= 1.0) {
                    String exMsg = String.format("Error maxRepetitions was shrunk to 1 to snmp server %s", getConnectionName(oid));
                    LOG.error(exMsg);
                    closeConnection(snmp, transport);
                    throw new StingraySnmpRetryExceededException(exMsg);
                }
                closeConnection(snmp, transport);
                continue;
            }
            int respSize = respPdu.size();
            for (int i = 0; i < respSize; i++) {
                totalItems++;
                vb = respPdu.get(i);
                String vbOid = vb.getOid().toString();
                if (!vbOid.startsWith(oid + ".")) {
                    finished = true;
                    continue;
                }
                matchedItems++;
                bindings.add(vb);
                currOID = vbOid;
            }
            closeConnection(snmp, transport);
            if (respSize < currMaxReps) {
                break; // This was the last set of entries.
            }
        }
        vlog.printf("total items = %d matched items= %d for %s FINISHED", totalItems, matchedItems, getConnectionName(oid));
        return bindings;
    }

    private void closeConnection(Snmp snmp, TransportMapping transport) {
        try {
            snmp.close();
        } catch (Exception ex) {
            LOG.warn(String.format("Warning unable to close snmp connection on %s", getConnectionName()));
        }
        try {
            transport.close();
        } catch (Exception ex) {
            LOG.warn(String.format("Warning unable to close transport on %s", getConnectionName()));
        }
    }

    public static String getOidFromVirtualServerName(String baseOid, String vsName) {
        StringBuilder sb = new StringBuilder();
        char[] vsChars = vsName.toCharArray();
        sb.append(baseOid);
        sb.append(".").append(vsChars.length);
        for (int i = 0; i < vsChars.length; i++) {
            sb.append(".").append((int) vsChars[i]);
        }
        return sb.toString();
    }

    public static String getVirtualServerNameFromOid(String baseOid, String oid) {
        StringBuilder sb = new StringBuilder();
        String[] baseNums = dotSplitter.split(baseOid);
        String[] nums = dotSplitter.split(oid);

        for (int i = baseNums.length + 1; i < nums.length; i++) {
            sb.append((char) Integer.parseInt(nums[i]));
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StingraySnmpClient)) {
            return false;
        }

        StingraySnmpClient that = (StingraySnmpClient) o;

        if (maxRepetitions != that.maxRepetitions) {
            return false;
        }
        if (maxRetrys != that.maxRetrys) {
            return false;
        }
        if (nonRepeaters != that.nonRepeaters) {
            return false;
        }
        if (reportUdpCountEveryNMilliSeconds != that.reportUdpCountEveryNMilliSeconds) {
            return false;
        }
        if (version != that.version) {
            return false;
        }
        if (address != null ? !address.equals(that.address) : that.address != null) {
            return false;
        }
        if (community != null ? !community.equals(that.community) : that.community != null) {
            return false;
        }
        if (port != null ? !port.equals(that.port) : that.port != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = nonRepeaters;
        result = 31 * result + maxRepetitions;
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (port != null ? port.hashCode() : 0);
        result = 31 * result + (community != null ? community.hashCode() : 0);
        result = 31 * result + (int) (reportUdpCountEveryNMilliSeconds ^ (reportUdpCountEveryNMilliSeconds >>> 32));
        result = 31 * result + maxRetrys;
        result = 31 * result + version;
        return result;
    }

    public String getAddress() {
        return address;
    }

    public String getPort() {
        return port;
    }

    public String getCommunity() {
        return community;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public static void nop() {
    }

    public static int getRequestId() {
        return requestId;
    }

    public int getMaxRetrys() {
        return maxRetrys;
    }

    public void setMaxRetrys(int maxRetrys) {
        this.maxRetrys = maxRetrys;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public synchronized static void setRequestId(int aRequestId) {
        requestId = aRequestId;
    }

    public long getReportUdpCountEveryNMilliSeconds() {
        return reportUdpCountEveryNMilliSeconds;
    }

    public void setReportUdpCountEveryNMilliSeconds(long reportUdpCountEveryNMilliSeconds) {
        this.reportUdpCountEveryNMilliSeconds = reportUdpCountEveryNMilliSeconds;
    }

    public int getNonRepeaters() {
        return nonRepeaters;
    }

    public void setNonRepeaters(int nonRepeaters) {
        this.nonRepeaters = nonRepeaters;
    }

    public int getMaxRepetitions() {
        return maxRepetitions;
    }

    public void setMaxRepetitions(int maxRepetitions) {
        this.maxRepetitions = maxRepetitions;
    }

    private String getConnectionName(String oid) {
        StringBuilder sb = new StringBuilder();
        sb.append(address).append("/").append(port);
        if (oid != null) {
            sb.append(" for oid ").append(oid);
        }
        return sb.toString();
    }

    private String getConnectionName() {
        return getConnectionName(null);
    }
}
