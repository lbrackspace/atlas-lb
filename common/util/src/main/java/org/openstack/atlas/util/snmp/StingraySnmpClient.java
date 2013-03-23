package org.openstack.atlas.util.snmp;

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
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class StingraySnmpClient {

    private static final Pattern dotSplitter = Pattern.compile("\\.");
    private static final VerboseLogger vlog = new VerboseLogger(StingraySnmpClient.class);
    private static final Log LOG = LogFactory.getLog(StingraySnmpClient.class);
    private String address;
    private String port;
    private String community = "public"; // Sounds like a good default
    private long reportUdpCountEveryNMilliSeconds = 1000;
    private int maxRetrys = 13;
    private static final Random rnd = new Random();
    private static int requestId = 0;

    public static Random getRnd() {
        return rnd;
    }

    public StingraySnmpClient() {
    }

    public StingraySnmpClient(String address, String port) {
        this.address = address;
        this.port = port;
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

    public Map<String, Long> getLongOidVals(String oid) throws StingraySnmpSetupException, StingraySnmpRetryExceededException, StingraySnmpGeneralException {
        Map<String, Long> oidMap = new HashMap<String, Long>();
        List<VariableBinding> bindings = getWalkOidBindingList(oid);
        for (VariableBinding vb : bindings) {
            String vsName = getVirtualServerNameFromOid(vb.getOid().toString());
            oidMap.put(vsName, new Long(vb.getVariable().toLong()));
        }
        return oidMap;
    }

    public Map<String, Long> getAllBandWidthIn() throws StingraySnmpRetryExceededException, StingraySnmpSetupException, StingraySnmpGeneralException {
        Map<String, Long> bytesIn64bit = new HashMap<String, Long>();

        Map<String, Long> bytesInHi = getLongOidVals(OIDConstants.VS_BYTES_IN_HI);
        Map<String, Long> bytesInLo = getLongOidVals(OIDConstants.VS_BYTES_IN_LO);
        Set<String> vsNames = new HashSet<String>();
        vsNames.addAll(bytesInHi.keySet());
        vsNames.addAll(bytesInLo.keySet());
        for (String vsName : vsNames) {
            if (!bytesInHi.containsKey(vsName) || !bytesInLo.containsKey(vsName)) {
                continue; // either the high or low byte was missing so skip this entry
            }
            long hi = bytesInHi.get(vsName);
            long lo = bytesInLo.get(vsName);
            bytesIn64bit.put(vsName, (hi << 32) + lo);
        }
        return bytesIn64bit;
    }

    public Map<String, Long> getAllBandWidthOut() throws StingraySnmpRetryExceededException, StingraySnmpSetupException, StingraySnmpGeneralException {
        Map<String, Long> bytesOut64bit = new HashMap<String, Long>();
        Map<String, Long> bytesOutHi = getLongOidVals(OIDConstants.VS_BYTES_OUT_HI);
        Map<String, Long> bytesOutLo = getLongOidVals(OIDConstants.VS_BYTES_OUT_LO);
        Set<String> vsNames = new HashSet<String>();
        vsNames.addAll(bytesOutHi.keySet());
        vsNames.addAll(bytesOutLo.keySet());
        for (String vsName : vsNames) {
            if (!bytesOutHi.containsKey(vsName) || !bytesOutLo.containsKey(vsName)) {
                continue; // either the high or low byte was missing so skip this entry
            }
            long hi = bytesOutHi.get(vsName);
            long lo = bytesOutLo.get(vsName);
            bytesOut64bit.put(vsName, (hi << 32) | lo);
        }
        return bytesOut64bit;
    }

    public Map<String, Long> getAllTotalConnections() throws StingraySnmpSetupException, StingraySnmpRetryExceededException, StingraySnmpGeneralException {
        return getLongOidVals(OIDConstants.VS_TOTAL_CONNECTIONS);
    }

    public Map<String, Long> getAllConcurrentConnections() throws StingraySnmpSetupException, StingraySnmpRetryExceededException, StingraySnmpGeneralException {
        return getLongOidVals(OIDConstants.VS_CURRENT_CONNECTIONS);
    }

    public Map<String, RawSnmpUsage> getSnmpUsage() throws StingraySnmpSetupException, StingraySnmpRetryExceededException, StingraySnmpGeneralException {
        vlog.printf("in call to getSnmpUsage()");
        Map<String, RawSnmpUsage> rawSnmpMap = new HashMap<String, RawSnmpUsage>();
        List<VariableBinding> bindings;

        // Fetch Current Connections
        bindings = getWalkOidBindingList(OIDConstants.VS_CURRENT_CONNECTIONS);
        for (VariableBinding vb : bindings) {
            String vsName = getVirtualServerNameFromOid(vb.getOid().toString());
            if (!rawSnmpMap.containsKey(vsName)) {
                RawSnmpUsage entry = new RawSnmpUsage();
                entry.setVsName(vsName);
                rawSnmpMap.put(vsName, entry);
            }
            rawSnmpMap.get(vsName).setConcurrentConnections(vb.getVariable().toLong());
        }

        // Fetch Total Connections
        bindings = getWalkOidBindingList(OIDConstants.VS_TOTAL_CONNECTIONS);
        for (VariableBinding vb : bindings) {
            String vsName = getVirtualServerNameFromOid(vb.getOid().toString());
            if (!rawSnmpMap.containsKey(vsName)) {
                RawSnmpUsage entry = new RawSnmpUsage();
                entry.setVsName(vsName);
                rawSnmpMap.put(vsName, entry);
            }
            rawSnmpMap.get(vsName).setTotalConnections(vb.getVariable().toLong());
        }

        // Fetch BytesIn hi bytes
        bindings = getWalkOidBindingList(OIDConstants.VS_BYTES_IN_HI);
        for (VariableBinding vb : bindings) {
            String vsName = getVirtualServerNameFromOid(vb.getOid().toString());
            if (!rawSnmpMap.containsKey(vsName)) {
                RawSnmpUsage entry = new RawSnmpUsage();
                entry.setVsName(vsName);
                rawSnmpMap.put(vsName, entry);
            }
            rawSnmpMap.get(vsName).setBytesInHi(vb.getVariable().toLong());
        }

        // Fetch Bytes In Lo
        bindings = getWalkOidBindingList(OIDConstants.VS_BYTES_IN_LO);
        for (VariableBinding vb : bindings) {
            String vsName = getVirtualServerNameFromOid(vb.getOid().toString());
            if (!rawSnmpMap.containsKey(vsName)) {
                RawSnmpUsage entry = new RawSnmpUsage();
                entry.setVsName(vsName);
                rawSnmpMap.put(vsName, entry);
            }
            rawSnmpMap.get(vsName).setBytesInLo(vb.getVariable().toLong());
        }

        // Fetch Bytes out Hi
        bindings = getWalkOidBindingList(OIDConstants.VS_BYTES_OUT_HI);
        for (VariableBinding vb : bindings) {
            String vsName = getVirtualServerNameFromOid(vb.getOid().toString());
            if (!rawSnmpMap.containsKey(vsName)) {
                RawSnmpUsage entry = new RawSnmpUsage();
                entry.setVsName(vsName);
                rawSnmpMap.put(vsName, entry);
            }
            rawSnmpMap.get(vsName).setBytesOutHi(vb.getVariable().toLong());
        }

        // Fetch Bytes Out Lo
        bindings = getWalkOidBindingList(OIDConstants.VS_BYTES_OUT_LO);
        for (VariableBinding vb : bindings) {
            String vsName = getVirtualServerNameFromOid(vb.getOid().toString());
            if (!rawSnmpMap.containsKey(vsName)) {
                RawSnmpUsage entry = new RawSnmpUsage();
                entry.setVsName(vsName);
                rawSnmpMap.put(vsName, entry);
            }
            rawSnmpMap.get(vsName).setBytesOutLo(vb.getVariable().toLong());
        }
        return rawSnmpMap;
    }

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
            target.setVersion(SnmpConstants.version1);
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

    public static String getVirtualServerNameFromOid(String oid) {
        StringBuilder sb = new StringBuilder();
        String[] nums = dotSplitter.split(oid);
        for (int i = 14; i < nums.length; i++) {
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

    public synchronized static void setRequestId(int aRequestId) {
        requestId = aRequestId;
    }

    public long getReportUdpCountEveryNMilliSeconds() {
        return reportUdpCountEveryNMilliSeconds;
    }

    public void setReportUdpCountEveryNMilliSeconds(long reportUdpCountEveryNMilliSeconds) {
        this.reportUdpCountEveryNMilliSeconds = reportUdpCountEveryNMilliSeconds;
    }
}
