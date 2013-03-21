package org.openstack.atlas.util.snmp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SnmpMain {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.printf("usage is <zxtmIp> <zxtmPort> <community>\n");
            System.out.printf("\n");
            System.out.printf("Collect snmp usage info from the specified ZxtmHost\n");
            return;
        }
        String ip = args[0];
        String port = args[1];
        String community = args[2];

        StingraySnmpClient client = new StingraySnmpClient(ip, port, community);
        Map<String, RawSnmpUsage> snmpUsageMap = client.getSnmpUsage();
        List<RawSnmpUsage> snmpUsageList = new ArrayList<RawSnmpUsage>();
        for (RawSnmpUsage usageValue : snmpUsageMap.values()) {
            snmpUsageList.add(usageValue);
        }
        Collections.sort(snmpUsageList); // Sorts on bytes in.
        int i = 0;
        for (RawSnmpUsage entry : snmpUsageList) {
            System.out.printf("#%d: %s\n", i++, entry.toString());
        }
    }
}
