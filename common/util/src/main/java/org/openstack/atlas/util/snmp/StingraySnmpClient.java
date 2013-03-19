package org.openstack.atlas.util.snmp;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class StingraySnmpClient {
    private Pattern dotSplitter = Pattern.compile("\\.");
    private enum numberOids {
        ALL_VS_CURRENT_CONNECTIONS("1.3.6.1.4.1.7146.1.2.2.2.1.9"),
        ALL_VS_TOTAL_BYTES_IN(""),
        ALL_VS_TOTAL_BYTES_OUT(""),
        ONE_VS_CURRENT_CONNECTIONS(""),
        ONE_VS_TOTAL_BYTES_IN(""),
        ONE_VS_TOTAL_BYTES_OUT("");

        numberOids(String oid){}
    }
    private String vsCurrentConnectionsOID = "1.3.6.1.4.1.7146.1.2.2.2.1.9";
    String address;
    String port;
    String community;
    int maxRepetitions;
    int nonRepeaters;

    public StingraySnmpClient(String address, String port) {
        this.address = address;
        this.port = port;
    }

    public StingraySnmpClient(String address, String port, String community, int maxRepetitions, int nonRepeaters) {
        this.address = address;
        this.port = port;
        this.community = community;

        this.maxRepetitions = maxRepetitions;
        this.nonRepeaters = nonRepeaters;
    }

    public Map<String, Integer> getWalkRequest(String oid) {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        OID targetOID = new OID(vsCurrentConnectionsOID);

        PDU requestPDU = new PDU();
        requestPDU.add(new VariableBinding(targetOID));
        requestPDU.setType(PDU.GETNEXT);

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(new UdpAddress(address + "/" + port));
        target.setVersion(SnmpConstants.version2c);

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
                } else if (vb.getOid().size() < targetOID.size()) {
//                    System.out.println("vb.getOid().size() < targetOID.size()");
                    finished = true;
                } else if (targetOID.leftMostCompare(targetOID.size(),
                        vb.getOid()) != 0) {
//                    System.out.println("targetOID.leftMostCompare() != 0)");
                    finished = true;
                } else if (Null.isExceptionSyntax(vb.getVariable().getSyntax())) {
//                    System.out.println(
//                            "Null.isExceptionSyntax(vb.getVariable().getSyntax())");
                    finished = true;
                } else if (vb.getOid().compareTo(targetOID) <= 0) {
//                    System.out.println("Variable received is not "
//                            + "lexicographic successor of requested "
//                            + "one:");
//                    System.out.println(vb.toString() + " <= " + targetOID);
                    finished = true;
                } else {
                    // Dump response.
                    map.put(getVirtualServerName(vb.getOid().toString()), vb.getVariable().toInt());

                    // Set up the variable binding for the next entry.
                    requestPDU.setRequestID(new Integer32(0));
                    requestPDU.set(0, vb);
                }
            }
            snmp.close();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
        return map;
    }

    public String getVirtualServerName(String oid) {
        StringBuilder sb = new StringBuilder();
        if (!oid.startsWith(vsCurrentConnectionsOID + ".")) {
            return "I don't know";
        }
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

    public int getMaxRepetitions() {
        return maxRepetitions;
    }

    public int getNonRepeaters() {
        return nonRepeaters;
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

    public void setMaxRepetitions(int maxRepetitions) {
        this.maxRepetitions = maxRepetitions;
    }

    public void setNonRepeaters(int nonRepeaters) {
        this.nonRepeaters = nonRepeaters;
    }
}