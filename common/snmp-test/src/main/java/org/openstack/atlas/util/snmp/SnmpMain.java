package org.openstack.atlas.util.snmp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.log4j.BasicConfigurator;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpGeneralException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpRetryExceededException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpSetupException;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public class SnmpMain {

    public static void main(String[] args) {
        BasicConfigurator.configure();

        if (args.length < 2) {
            System.out.printf("usage is <zxtmIp> <zxtmPort> [community] [maxRetries]\n");
            System.out.printf("\n");
            System.out.printf("Collect snmp usage info from the specified ZxtmHost\n");
            return;
        }

        String ip = args[0];
        String port = args[1];
        StingraySnmpClient client = new StingraySnmpClient(ip, port);

        if (args.length >= 3) {
            client.setCommunity(args[2]);
        }
        if (args.length >= 4) {
            client.setMaxRetrys(Integer.parseInt(args[3]));
        }


        System.out.printf("useing client = %s\n", client.toString());

        Map<String, RawSnmpUsage> snmpUsageMap;
        try {
            snmpUsageMap = client.getSnmpUsage();
        } catch (StingraySnmpGeneralException ex) {
            System.out.printf("Exception: %s\n", StaticStringUtils.getExtendedStackTrace(ex));
            return;
        }
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
