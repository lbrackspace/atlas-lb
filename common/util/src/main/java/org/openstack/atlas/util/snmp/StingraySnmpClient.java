package org.openstack.atlas.util.snmp;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.util.common.VerboseLogger;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpGeneralException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpRetryExceededException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpSetupException;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import org.snmp4j.transport.UdpTransportMapping;

public class StingraySnmpClient {

    private int nonRepeaters = 0;
    private int maxRepetitions = 1000;
    private static final Pattern dotSplitter = Pattern.compile("\\.");
    private static final VerboseLogger vlog = new VerboseLogger(StingraySnmpClient.class);
    private static final Log LOG = LogFactory.getLog(StingraySnmpClient.class);
    private String address;
    private String port = StingraySnmpConstants.PORT;
    private String community = StingraySnmpConstants.COMMUNITY;
    private long reportUdpCountEveryNMilliSeconds = 1000;
    private int maxRetrys = 13;
    private static final Random rnd = new Random();
    private static int requestId = 0;
    private int version = SnmpConstants.version2c;

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
                + ", curRequestId=" + getRequestId()
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

    public long getBytesIn(String vsName) throws StingraySnmpSetupException, StingraySnmpGeneralException {
        return getLongValueForVirtualServer(vsName, OIDConstants.VS_BYTES_IN);
    }

    public long getBytesOut(String vsName) throws StingraySnmpSetupException, StingraySnmpGeneralException {
        return getLongValueForVirtualServer(vsName, OIDConstants.VS_BYTES_OUT);
    }

    public long getConcurrentConnections(String vsName) throws StingraySnmpSetupException, StingraySnmpGeneralException {
        return getLongValueForVirtualServer(vsName, OIDConstants.VS_CURRENT_CONNECTIONS);
    }

    public long getLongValueForVirtualServer(String vsName, String baseOid) throws StingraySnmpSetupException, StingraySnmpGeneralException {

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
        TransportMapping transport;
        try {
            transport = new DefaultUdpTransportMapping();
        } catch (IOException ex) {
            String msg = String.format("Error setting up udp port for SNMP connections for oid value %s for vs %s", baseOid, vsName);
            LOG.error(msg, ex);
            throw new StingraySnmpSetupException(msg, ex);
        }
        Snmp snmp = new Snmp(transport);
        try {
            transport.listen();
        } catch (IOException ex) {
            String msg = String.format("Error listening on udp port for SNMP connections for oid value %s for vs %s", baseOid, vsName);
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
            String msg = String.format("Error getting OID value %s for vs %s", baseOid, vsName);
            LOG.error(msg, ex);
            closeConnection(snmp, transport);
            throw new StingraySnmpGeneralException(msg, ex);
        }
        if (respEvent == null) {
            String msg = String.format("Error responseEvent for OID %s for vs %s was null", baseOid, vsName);
            LOG.error(msg);
            closeConnection(snmp, transport);
        }
        PDU resp;
        resp = respEvent.getResponse();
        if (resp == null) {
            String msg = String.format("Error response for OID %s for vs %s was null", baseOid, vsName);
            LOG.error(msg);
            closeConnection(snmp, transport);
            throw new StingraySnmpGeneralException(msg);
        }
        int respSize = resp.size();
        if (respSize < 1) {
            String msg = String.format("Error response binding size for for OID %s for vs %s was %d", baseOid, vsName, respSize);
            LOG.error(msg);
            closeConnection(snmp, transport);
            throw new StingraySnmpGeneralException(msg);
        }
        vb = resp.get(0);
        String vbOid = vb.getOid().toString();
        closeConnection(snmp, transport);
        long val = vb.getVariable().toLong();
        return val;
    }

    @Deprecated
    public List<VariableBinding> getWalkOidBindingList(String oid) throws StingraySnmpSetupException, StingraySnmpRetryExceededException, StingraySnmpGeneralException {
        int retryCount = maxRetrys;
        int udpsSent = 0;
        long delay = 1; // Start with a back off of 1 Milliseconds
        vlog.printf("int call getWalkIudBindingList(%s)", oid);
        List<VariableBinding> bindingsList = new ArrayList<VariableBinding>();
        OID targetOID = new OID(oid);
        PDU requestPDU = new PDU();
        requestPDU.add(new VariableBinding(targetOID));
        requestPDU.setType(PDU.GETNEXT);
        try {
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(new UdpAddress(address + "/" + port));
            target.setVersion(version);
            TransportMapping transport;
            try {
                transport = new DefaultUdpTransportMapping();
            } catch (IOException ex) {
                throw new StingraySnmpSetupException("Error setting up DefaultUdpTransportMapping for snmp client", ex);
            }
            Snmp snmp = new Snmp(transport);
            try {
                transport.listen();
            } catch (IOException ex) {
                String msg = "Unable to listen to address " + transport.getListenAddress().toString();
                throw new StingraySnmpSetupException(msg, ex);
            }

            boolean finished = false;
            long startMillis = System.currentTimeMillis();
            while (!finished) {
                long endMillis = System.currentTimeMillis();
                if (endMillis - startMillis > reportUdpCountEveryNMilliSeconds) {
                    vlog.printf("Sent %d udp packets ", udpsSent);
                    startMillis = endMillis;
                }

                VariableBinding vb = null;
                ResponseEvent event;
                try {
                    event = snmp.send(requestPDU, target);
                    udpsSent++;
                } catch (IOException ex) {
                    throw new StingraySnmpGeneralException("Error sending snmp request zxtm agent", ex);
                }
                PDU responsePDU = event.getResponse();
                if (responsePDU != null) {
                    vb = responsePDU.get(0);
                }

                if (responsePDU == null) {
                    if (retryCount <= 0) {
                        throw new StingraySnmpRetryExceededException("Exceeded maxRetries in snmp request to Zxtm agent after " + udpsSent + "udp packets sent");
                    }
                    retryCount--;
                    String msg = String.format("timeout waiting for UDP packet from snmp: waiting %d millis to try again. %d retries left: sent %d udps so far", delay, retryCount, udpsSent);
                    vlog.printf("%s", msg);
                    Thread.sleep(delay);
                    delay *= 2; // Use a stable Exponential backoff.
                } else if (responsePDU.getErrorStatus() != 0) {
                    finished = true;
                } else if (vb.getOid() == null) {
                    finished = true;
                } else if (Null.isExceptionSyntax(vb.getVariable().getSyntax())) {
                    finished = true;
                } else if (vb.getOid().size() < targetOID.size()) {
                    finished = true;
                } else if (targetOID.leftMostCompare(targetOID.size(),
                        vb.getOid()) != 0) {
                    finished = true;
                } else if (vb.getOid().compareTo(targetOID) <= 0) {
                    finished = true;
                } else {
                    bindingsList.add(vb);
                    String vbString = vb.toString();
                    requestPDU.setRequestID(new Integer32(incRequestId()));
                    requestPDU.set(0, vb);
                }
            }
            try {
                snmp.close();
            } catch (IOException ex) {
                throw new StingraySnmpGeneralException("Could not close low level snmp client", ex);
            }
        } catch (Exception ex) {
            // This is something unexpected
            throw new StingraySnmpGeneralException("Unhandled exception", ex);
        }

        return bindingsList;
    }

    public List<VariableBinding> getBulkOidBindingList(String oid) throws StingraySnmpSetupException, StingraySnmpGeneralException {
        vlog.printf("in call getBulkOidBindingList(%s)", oid);
        List<VariableBinding> bindings = new ArrayList<VariableBinding>();
        String startOID = oid;
        String currOID = startOID;
        int totalItems = 0;
        int matchedItems = 0;
        int currMaxReps = maxRepetitions;
        boolean finished = false;
        while (!finished) {
            vlog.printf("total items = %d matched items= %d", totalItems, matchedItems);
            PDU req = new PDU();
            req.add(new VariableBinding(new OID(currOID)));
            req.setType(PDU.GETBULK);
            req.setNonRepeaters(nonRepeaters);
            req.setMaxRepetitions(currMaxReps);
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
            target.setAddress(udpAddr);
            TransportMapping transport;
            try {
                transport = new DefaultUdpTransportMapping();
            } catch (IOException ex) {
                String msg = String.format("Error setting up snmp connection to %s:%s", address, port);
                LOG.error(msg, ex);
                throw new StingraySnmpSetupException(msg, ex);
            }
            Snmp snmp = new Snmp(transport);
            try {
                transport.listen();
            } catch (IOException ex) {
                String msg = String.format("Error listening on local udp port for snmp connection");
                LOG.error(msg, ex);
                closeConnection(snmp, transport);
                throw new StingraySnmpSetupException(msg, ex);
            }
            VariableBinding vb = null;
            ResponseEvent respEvent = null;
            try {
                respEvent = snmp.getBulk(req, target);
            } catch (IOException ex) {
                String msg = String.format("Error getting bulk request from snmp");
                LOG.error(msg, ex);
                closeConnection(snmp, transport);
                throw new StingraySnmpGeneralException(msg, ex);
            }
            PDU respPdu = respEvent.getResponse();
            if (respPdu == null) {
                String msg = String.format("Error fetching bulk response reducing maxRepetitions from %d to %d", currMaxReps, currMaxReps / 2);
                currMaxReps /= 2;
                LOG.warn(msg);
                if (currMaxReps <= 1) {
                    String exMsg = String.format("Error maxRepetitions was strunk to 1");
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
        vlog.printf("total items = %d matched items= %d", totalItems, matchedItems);
        return bindings;
    }

    private static void closeConnection(Snmp snmp, TransportMapping transport) {
        try {
            snmp.close();
        } catch (Exception ex) {
            LOG.warn("Warning unable to close snmp connection");
        }
        try {
            transport.close();
        } catch (Exception ex) {
            LOG.warn("Warning unable to close transport");
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
}
