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
import org.apache.log4j.BasicConfigurator;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.snmp.StingrayUsageClient;
import org.openstack.atlas.usagerefactor.snmp.StingrayUsageClientImpl;
import org.openstack.atlas.usagerefactor.helpers.SnmpUsageComparator;
import org.openstack.atlas.usagerefactor.helpers.SnmpUsageComparatorType;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.snmp.comparators.BandwidthInComparator;
import org.openstack.atlas.util.snmp.comparators.BandwidthoutComparator;
import org.openstack.atlas.util.snmp.comparators.ConcurrentConnectionsComparator;
import org.openstack.atlas.util.snmp.comparators.VsNameComparator;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;
import org.snmp4j.smi.VariableBinding;

public class SnmpMain {

    private static BufferedReader stdin = StaticFileUtils.inputStreamToBufferedReader(System.in);

    public static void main(String[] MainArgs) throws IOException {
        BasicConfigurator.configure();
        Map<String, StingraySnmpClient> clients = new HashMap<String, StingraySnmpClient>();
        StingraySnmpClient defaultClient = new StingraySnmpClient();
        List<RawSnmpUsage> rawUsageList = new ArrayList<RawSnmpUsage>();
        List<SnmpUsage> snmpUsageList = new ArrayList<SnmpUsage>();
        Comparator<RawSnmpUsage> orderBy = new ConcurrentConnectionsComparator();
        SnmpUsageComparator usageComparator = new SnmpUsageComparator();
        StingrayUsageClient jobClient = new StingrayUsageClientImpl();

        if (MainArgs.length < 1) {
            System.out.printf("Usage is <configJsonFile>\n");
            System.out.printf("\n");
            System.out.printf("runs the snmpCommandline tester using the jsonConfig from the\n");
            System.out.printf("specified file. Example oargsf json configuration\n%s\n", SnmpJsonConfig.exampleJson);
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
                    System.out.printf("    set_comparator <bi|bo|cc|vs> #Set the comparator for to BandwidthIn BandwidthOut totalConnections or ConcurrentConnections respectivly\n");
                    System.out.printf("    run_bulk <oid> #Get usage from the default host useing the bulk on the given oid\n");
                    System.out.printf("    run_default [clientKey]#Get usage from the default host and add it to the current usage list\n");
                    System.out.printf("    clear_usage #Clear the current usageList as well as the jobs usage list\n");
                    System.out.printf("    display_usage #Display the current usage\n");
                    System.out.printf("    display_snmp_usage_rollup #Display usage rolledup map\n");
                    System.out.printf("    display_snmp_usage_list #Display jobs snmp usage list\n");
                    System.out.printf("    set_retrys <num> #Sets the maximum retries\n");
                    System.out.printf("    lookup <oid> <vsName> #Lookup the given OID for the specified virtual server on the default zxtm host\n");
                    System.out.printf("    client <clientKey> #Set the clientKey for the default run\n");
                    System.out.printf("    run_jobs #Run threaded jobs client for itest\n");
                    System.out.printf("    run_all #Run stats for all zxtm hosts\n");
                    System.out.printf("    run_threads #run threads for all zxtm hosts\n");
                    System.out.printf("    run_job <hostId> <aid> <lid> #get usage for loadbalancer id on host\n");
                    System.out.printf("    add_comparator <lid|hid|cc|ccssl|bi|bo|bissl|bossl> #Add comparator for jobs snmpUsage\n");
                    System.out.printf("    clear_comparators #Clear the comparators for snmpUsage\n");
                    System.out.printf("    exit #Exits\n");
                    System.out.printf("\n");
                } else if (cmd.equals("run_job") && args.length >= 4) {
                    int hid = Integer.parseInt(args[1]);
                    int aid = Integer.parseInt(args[2]);
                    int lid = Integer.parseInt(args[3]);
                    List<String> clientKeys = new ArrayList<String>(clients.keySet());
                    Host host = new Host();
                    host.setId(hid);
                    host.setManagementIp(clients.get(clientKeys.get(hid)).getAddress());
                    LoadBalancer lb = new LoadBalancer();
                    lb.setId(lid);
                    lb.setAccountId(aid);
                    SnmpUsage usageItem = jobClient.getVirtualServerUsage(host, lb);
                    System.out.printf(usageItem.toString());
                } else if (cmd.equals("clear_comparators")) {
                    usageComparator = new SnmpUsageComparator();
                } else if (cmd.equals("add_comparator") && args.length >= 2) {
                    String compStr = args[1];
                    if (compStr.equals("lid")) {
                        usageComparator.getComparatorTypes().add(SnmpUsageComparatorType.LOADBALANCER_ID);
                    } else if (compStr.equals("hid")) {
                        usageComparator.getComparatorTypes().add(SnmpUsageComparatorType.HOST_ID);
                    } else if (compStr.equals("cc")) {
                        usageComparator.getComparatorTypes().add(SnmpUsageComparatorType.CONCURRENT_CONNECTIONS);
                    } else if (compStr.equals("ccssl")) {
                        usageComparator.getComparatorTypes().add(SnmpUsageComparatorType.CONCURRENT_SSL_CONNECTIONS);
                    } else if (compStr.equals("bi")) {
                        usageComparator.getComparatorTypes().add(SnmpUsageComparatorType.BYTES_IN);
                    } else if (compStr.equals("bo")) {
                        usageComparator.getComparatorTypes().add(SnmpUsageComparatorType.BYTES_OUT);
                    } else if (compStr.equals("bissl")) {
                        usageComparator.getComparatorTypes().add(SnmpUsageComparatorType.BYTES_SSL_IN);
                    } else if (compStr.equals("bossl")) {
                        usageComparator.getComparatorTypes().add(SnmpUsageComparatorType.BYTES_SSL_OUT);
                    } else {
                        System.out.printf("Un recognized comparator %s\n", compStr);
                    }


                } else if (cmd.equals("run_jobs")) {
                    List<String> clientKeys = new ArrayList<String>(clients.keySet());
                    List<Host> zxtmHosts = new ArrayList<Host>();
                    List<SnmpJobThread> threads = new ArrayList<SnmpJobThread>();
                    Collections.sort(clientKeys);
                    int hostId = 0;
                    for (String clientKey : clientKeys) {
                        String zxtmHostIp = clients.get(clientKey).getAddress();
                        Host zxtmHost = new Host();
                        zxtmHost.setManagementIp(zxtmHostIp);
                        zxtmHost.setId(hostId++);
                        zxtmHosts.add(zxtmHost);
                        SnmpJobThread jobThread = new SnmpJobThread();
                        jobThread.setClient(jobClient);
                        jobThread.setHost(zxtmHost);
                        threads.add(jobThread);
                    }
                    // start the threads
                    for (SnmpJobThread thread : threads) {
                        thread.start();
                    }

                    // Join the threads
                    for (SnmpJobThread thread : threads) {
                        thread.join();
                    }

                    // Gran all the results
                    for (SnmpJobThread thread : threads) {
                        System.out.printf("reading snmpUsage from thread for host %s: in %f(secs)\n", thread.getHost().getManagementIp(), thread.getElapsedTime());
                        Exception ex = thread.getException();
                        if (ex != null) {
                            System.out.printf("%s\n", StaticStringUtils.getExtendedStackTrace(ex));
                        } else {
                            snmpUsageList.addAll(thread.getUsage().values());
                        }
                    }
                } else if (cmd.equals("display_snmp_usage_list")) {
                    Collections.sort(snmpUsageList, usageComparator);
                    for (int i = 0; i < snmpUsageList.size(); i++) {
                        System.out.printf("Entry[%d]=%s\n", i, snmpUsageList.get(i).toString());
                    }
                } else if (cmd.equals("display_snmp_usage_rollup")) {
                    Map<Integer, SnmpUsage> usageMap = new HashMap<Integer, SnmpUsage>();
                    for (SnmpUsage snmpUsageEntry : snmpUsageList) {
                        int lid = snmpUsageEntry.getLoadbalancerId();
                        if (!usageMap.containsKey(lid)) {
                            usageMap.put(lid, new SnmpUsage(snmpUsageEntry));
                        } else {
                            SnmpUsage oldSnmpUsage = usageMap.get(lid);
                            usageMap.put(lid, SnmpUsage.add(oldSnmpUsage, snmpUsageEntry));
                        }
                    }
                    List<SnmpUsage> sortedUsageList = new ArrayList<SnmpUsage>(usageMap.values());
                    Collections.sort(sortedUsageList, usageComparator);
                    for (int i = 0; i < sortedUsageList.size(); i++) {
                        SnmpUsage usageItem = sortedUsageList.get(i);
                        System.out.printf("usage[%d]=%s\n", i, usageItem.toString());
                    }
                } else if (cmd.equals("run_threads")) {
                    List<String> clientKeys = new ArrayList<String>(clients.keySet());
                    List<SnmpClientThread> threads = new ArrayList<SnmpClientThread>();
                    Collections.sort(clientKeys);
                    for (String clientKey : clientKeys) {
                        StingraySnmpClient client = clients.get(clientKey);
                        SnmpClientThread clientThread = new SnmpClientThread();
                        clientThread.setClient(client);
                        clientThread.setException(null);
                        threads.add(clientThread);
                    }

                    // Start the threads
                    for (SnmpClientThread thread : threads) {
                        thread.start();
                    }

                    // Join them all
                    for (SnmpClientThread thread : threads) {
                        System.out.printf("Joining thread for client %s\n", thread.getClient().toString());
                        thread.join();
                        System.out.printf("thread for %s joined\n", thread.getClient().toString());
                    }

                    // Grab all the results
                    for (SnmpClientThread thread : threads) {
                        System.out.printf("reading rawUsage from thread for client %s\n", thread.getClient().toString());
                        Exception ex = thread.getException();
                        if (ex != null) {
                            System.out.printf("%s\n", StaticStringUtils.getExtendedStackTrace(ex));
                        } else {
                            rawUsageList.addAll(thread.getUsage().values());
                        }
                    }

                } else if (cmd.equals("client") && args.length >= 2) {
                    String clientKey = args[1];
                    defaultClient = clients.get(clientKey);
                    System.out.printf("Client set to %s -> %s\n", clientKey, defaultClient.toString());
                } else if (cmd.equals("lookup") && args.length >= 3) {
                    String oid = args[1];
                    String vsName = args[2];
                    long val = defaultClient.getLongValueForVirtualServer(vsName, oid, true);
                    System.out.printf("%s for %s = %d\n", oid, vsName, val);
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
                    rawUsageList = new ArrayList<RawSnmpUsage>();
                    snmpUsageList = new ArrayList<SnmpUsage>();
                } else if (cmd.equals("display_usage")) {
                    System.out.printf("Pringint usage\n");
                    Collections.sort(rawUsageList, orderBy);
                    for (int i = 0; i < rawUsageList.size(); i++) {
                        System.out.printf("entry[%d]=%s\n", i, rawUsageList.get(i).toString());
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
                } else if (cmd.equals("run_all")) {
                    System.out.printf("Running stats on all lbs\n");
                    List<String> clientKeys = new ArrayList<String>(clients.keySet());
                    Collections.sort(clientKeys);
                    for (String clientKey : clientKeys) {
                        StingraySnmpClient client = clients.get(clientKey);
                        System.out.printf("Gathering info for zxtm %s -> %s\n", clientKey, client.toString());
                        rawUsageList.addAll(client.getSnmpUsage().values());
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
                    rawUsageList.addAll(client.getSnmpUsage().values());
                } else if (cmd.equals("set_comparator") && args.length >= 2) {
                    String compArg = args[1].toLowerCase();
                    if (compArg.equals("bi")) {
                        orderBy = new BandwidthInComparator();
                    } else if (compArg.equals("bo")) {
                        orderBy = new BandwidthoutComparator();
                    } else if (compArg.equals("cc")) {
                        orderBy = new ConcurrentConnectionsComparator();
                    } else if (compArg.equals("vs")) {
                        orderBy = new VsNameComparator();
                    } else {
                        System.out.printf("unknown Comparator\n");
                    }
                } else if (cmd.equals("show_state")) {
                    System.out.printf("%s\n", Debug.showMem());
                    System.out.printf("ClientMap:\n");
                    List<String> clientKeys = new ArrayList<String>(clients.keySet());
                    Collections.sort(clientKeys);
                    for (int i = 0; i < clientKeys.size(); i++) {
                        String clientKey = clientKeys.get(i);
                        System.out.printf("host[%d]=%s->%s\n", i, clientKey, clients.get(clientKey).toString());
                    }
                    System.out.printf("defaultClient:\n%s\n", defaultClient.toString());
                    System.out.printf("usageList contains %d entries\n", rawUsageList.size());
                    System.out.printf("Client reqId = %d\n", StingraySnmpClient.getRequestId());
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
