package org.openstack.atlas.util.snmp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.log4j.BasicConfigurator;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.snmp.comparators.BandwidthInComparator;
import org.openstack.atlas.util.snmp.comparators.BandwidthoutComparator;
import org.openstack.atlas.util.snmp.comparators.ConcurrentConnectionsComparator;
import org.openstack.atlas.util.snmp.comparators.TotalConnectionsComparator;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpGeneralException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpRetryExceededException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpSetupException;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;
import org.snmp4j.smi.VariableBinding;

public class SnmpMain {

    private static BufferedReader stdin = StaticFileUtils.inputStreamToBufferedReader(System.in);

    public static void main(String[] MainArgs) throws IOException {
        BasicConfigurator.configure();
        Map<String, StingraySnmpClient> clients = new HashMap<String, StingraySnmpClient>();
        StingraySnmpClient defaultClient = new StingraySnmpClient();
        List<RawSnmpUsage> usageList = new ArrayList<RawSnmpUsage>();
        Comparator<RawSnmpUsage> orderBy = new ConcurrentConnectionsComparator();
        if (MainArgs.length < 1) {
            System.out.printf("Usage is <configJsonFile>\n");
            System.out.printf("\n");
            System.out.printf("runs the snmpCommandline tester using the jsonConfig from the\n");
            System.out.printf("specified file. Example of json configuration\n%s\n", SnmpJsonConfig.exampleJson);
            return;
        }

        File jsonFile = new File(StaticFileUtils.expandUser(MainArgs[0]));
        SnmpJsonConfig conf = SnmpJsonConfig.readJsonConfig(jsonFile);
        System.out.printf("useing config = %s\n", conf.toString());
        while (true) {
            try {
                System.out.printf("> ");
                System.out.flush();
                String cmdLine = stdin.readLine();
                if (cmdLine == null) {
                    break;
                }
                String[] args = StaticStringUtils.stripBlankArgs(cmdLine);
                if (args.length < 1) {
                    System.out.printf("Type help for usage\n");
                    continue;
                }
                String cmd = args[0].toLowerCase();
                if (cmd.equals("help")) {
                    System.out.printf("\n");
                    System.out.printf("    usage is\n");
                    System.out.printf("    mem #show memory status\n");
                    System.out.printf("    gc  #Run garbage collector\n");
                    System.out.printf("    init_clients #Initialize the snmpClients\n");
                    System.out.printf("    show_state  #Show the application state and variables\n");
                    System.out.printf("    set_comparator <bi|bo|tc|cc> #Set the comparator for to BandwidthIn BandwidthOut totalConnections or ConcurrentConnections respectivly\n");
                    System.out.printf("    run_bulk <oid> #Get usage from the default host useing the bulk on the given oid\n");
                    System.out.printf("    run_default #Get usage from the default host and add it to the current usage list\n");
                    System.out.printf("    clear_usage #Clear the current usageList\n");
                    System.out.printf("    display_usage #Display the current usage\n");
                    System.out.printf("    set_retrys <num> #Sets the maximum retries\n");
                    System.out.printf("    exit #Exits\n");
                    System.out.printf("\n");
                } else if (cmd.equals("set_retrys") && args.length >= 2) {
                    System.out.printf("Setting retries to ");
                    System.out.flush();
                    int maxRetrys = Integer.parseInt(args[1]);
                    System.out.printf("%d\n", maxRetrys);
                    for (StingraySnmpClient client : clients.values()) {
                        client.setMaxRetrys(maxRetrys);
                    }
                    defaultClient.setMaxRetrys(maxRetrys);
                } else if (cmd.equals("clear_usage")) {
                    System.out.printf("Clearing usage\n");
                    usageList.clear();
                } else if (cmd.equals("display_usage")) {
                    System.out.printf("Pringint usage\n");
                    Collections.sort(usageList, orderBy);
                    for (int i = 0; i < usageList.size(); i++) {
                        System.out.printf("entry[%d]=%s\n", i, usageList.get(i).toString());
                    }
                } else if (cmd.equals("run_bulk") && args.length >= 2) {
                    String oid = args[1];
                    System.out.printf("Running bulk on oid %s\n", oid);
                    StingraySnmpClient client = defaultClient;
                    List<VariableBinding> bindings = client.getBulkOidBindingList(oid);
                    for (VariableBinding vb : bindings) {
                        String vbOid = vb.getOid().toString();
                        String vsName = StingraySnmpClient.getVirtualServerNameFromOid(oid, vbOid);
                        long val = vb.getVariable().toLong();
                        System.out.printf("%s %s=%d\n", vsName, vbOid, val);
                    }

                } else if (cmd.equals("run_default")) {
                    System.out.printf("Calling run for defaultLb\n");
                    StingraySnmpClient client;
                    if (args.length >= 2) {
                        client = clients.get(args[1]);
                    } else {
                        client = defaultClient;
                    }
                    System.out.printf("Useing client %s\n", client.toString());
                    usageList.addAll(client.getSnmpUsage().values());
                } else if (cmd.equals("set_comparator") && args.length >= 2) {
                    String compArg = args[1].toLowerCase();
                    if (compArg.equals("bi")) {
                        orderBy = new BandwidthInComparator();
                    } else if (compArg.equals("bo")) {
                        orderBy = new BandwidthoutComparator();
                    } else if (compArg.equals("tc")) {
                        orderBy = new TotalConnectionsComparator();
                    } else if (compArg.equals("cc")) {
                        orderBy = new ConcurrentConnectionsComparator();
                    } else {
                        System.out.printf("Ubnknown Comparator\n");
                    }
                } else if (cmd.equals("show_state")) {
                    System.out.printf("%s\n", Debug.showMem());
                    System.out.printf("ClientMap:\n%s\n", StaticStringUtils.mapToString(clients, "\n"));
                    System.out.printf("defaultClient:\n%s\n", defaultClient.toString());
                    System.out.printf("usageList contains %d entries\n", usageList.size());
                    System.out.printf("\n");
                } else if (cmd.equals("exit")) {
                    break;
                } else if (cmd.equals("gc")) {
                    Debug.gc();
                    System.out.printf("Called Garbage collector\n");
                } else if (cmd.equals("mem")) {
                    System.out.printf("%s\n", Debug.showMem());

                } else if (cmd.equals("init_clients")) {
                    System.out.printf("initializeing clients\n");
                    clients.clear();
                    for (Entry<String, String> ent : conf.getZxtmHosts().entrySet()) {
                        String clientKey = ent.getKey();
                        String hostAndPort = ent.getValue();
                        String[] split = hostAndPort.split("/");
                        String host = split[0];
                        String port = split[1];
                        StingraySnmpClient client = new StingraySnmpClient();
                        client.setAddress(host);
                        client.setPort(port);
                        clients.put(clientKey, client);
                        if (clientKey.equals(conf.getDefaultHostKey())) {
                            defaultClient = client;
                        }
                    }
                } else {
                    System.out.printf("Unknown Command\n");
                }
            } catch (Exception ex) {
                System.out.printf("Exception: %s\n", StaticStringUtils.getExtendedStackTrace(ex));
            }
        }

    }
}
