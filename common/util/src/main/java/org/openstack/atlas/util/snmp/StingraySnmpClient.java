package org.openstack.atlas.util.snmp;

import java.util.ArrayList;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

public class StingraySnmpClient {

    private static final Pattern dotSplitter = Pattern.compile("\\.");
    private String address;
    private String port;
    private String community;

    public StingraySnmpClient(String address, String port) {
        this.address = address;
        this.port = port;
    }

    public StingraySnmpClient(String address, String port, String community) {
        this.address = address;
        this.port = port;
        this.community = community;

    }

    public Map<String, RawSnmpUsage> getSnmpUsage() {
        Map<String, RawSnmpUsage> rawSnmpMap = new HashMap<String, RawSnmpUsage>();
        List<VariableBinding> bindings;

        // Fetch Current Connections
        bindings = getWalkOidBindingList(OIDConstants.VS_CURRENT_CONNECTIONS);
        for (VariableBinding vb : bindings) {
            String vsName = getVirtualServerName(vb.getOid().toString());
            if (!rawSnmpMap.containsKey(vsName)) {
                RawSnmpUsage entry = new RawSnmpUsage();
                entry.setVsName(vsName);
                rawSnmpMap.put(vsName, entry);
            }
            rawSnmpMap.get(vsName).setConcurrentConnections(vb.getVariable().toInt());
        }

        // Fetch Total Connections
        bindings = getWalkOidBindingList(OIDConstants.VS_TOTAL_CONNECTIONS);
        for (VariableBinding vb : bindings) {
            String vsName = getVirtualServerName(vb.getOid().toString());
            if (!rawSnmpMap.containsKey(vsName)) {
                RawSnmpUsage entry = new RawSnmpUsage();
                entry.setVsName(vsName);
                rawSnmpMap.put(vsName, entry);
            }
            rawSnmpMap.get(vsName).setTotalConnections(vb.getVariable().toInt());
        }



        // Fetch BytesIn hi bytes
        bindings = getWalkOidBindingList(OIDConstants.VS_BYTES_IN_HI);
        for (VariableBinding vb : bindings) {
            String vsName = getVirtualServerName(vb.getOid().toString());
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
            String vsName = getVirtualServerName(vb.getOid().toString());
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
            String vsName = getVirtualServerName(vb.getOid().toString());
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
            String vsName = getVirtualServerName(vb.getOid().toString());
            if (!rawSnmpMap.containsKey(vsName)) {
                RawSnmpUsage entry = new RawSnmpUsage();
                entry.setVsName(vsName);
                rawSnmpMap.put(vsName, entry);
            }
            rawSnmpMap.get(vsName).setBytesOutLo(vb.getVariable().toLong());
        }
        return rawSnmpMap;
    }

    public List<VariableBinding> getWalkOidBindingList(String oid) {
        List<VariableBinding> bindingsList = new ArrayList<VariableBinding>();
        OID targetOID = new OID(oid);

        PDU requestPDU = new PDU();
        requestPDU.add(new VariableBinding(targetOID));
        requestPDU.setType(PDU.GETNEXT);

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(new UdpAddress(address + "/" + port));
        target.setVersion(SnmpConstants.version1);

        try {
            TransportMapping transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            transport.listen();

            boolean finished = false;

            while (!finished) {
                VariableBinding vb = null;
                ResponseEvent event = snmp.send(requestPDU, target);
                PDU responsePDU = event.getResponse();
                if (responsePDU != null) {
                    vb = responsePDU.get(0);
                }

                if (responsePDU == null) {
                    System.out.println("responsePDU == null");
                    finished = true;
                } else if (responsePDU.getErrorStatus() != 0) {
//                    System.out.println("responsePDU.getErrorStatus() != 0");
//                    System.out.println(responsePDU.getErrorStatusText());
                    finished = true;
                } else if (vb.getOid() == null) {
//                    System.out.println("vb.getOid() == null");
                    finished = true;
                } else if (Null.isExceptionSyntax(vb.getVariable().getSyntax())) {
//                    System.out.println(
//                            "Null.isExceptionSyntax(vb.getVariable().getSyntax())");
                    finished = true;
                } else if (vb.getOid().size() < targetOID.size()) {
//                    System.out.println("vb.getOid().size() < targetOID.size()");
                    finished = true;
                } else if (targetOID.leftMostCompare(targetOID.size(),
                        vb.getOid()) != 0) {
//                    System.out.println("targetOID.leftMostCompare() != 0)");
                    finished = true;
                } else if (vb.getOid().compareTo(targetOID) <= 0) {
//                    System.out.println("Variable received is not "
//                            + "lexicographic successor of requested "
//                            + "one:");
//                    System.out.println(vb.toString() + " <= " + targetOID);
                    finished = true;
                } else {
                    // Dump response.
                    bindingsList.add(vb);

                    // Set up the variable binding for the next entry.
                    requestPDU.setRequestID(new Integer32(0));
                    requestPDU.set(0, vb);
                }
            }
            snmp.close();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
        return bindingsList;
    }

    public static String getVirtualServerName(String oid) {
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
}
