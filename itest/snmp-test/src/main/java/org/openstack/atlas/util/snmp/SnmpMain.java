package org.openstack.atlas.util.snmp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import org.openstack.atlas.util.common.SetUtil;
import org.openstack.atlas.util.ip.IPUtils;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.snmp4j.smi.VariableBinding;

public class SnmpMain {

    private static BufferedReader stdin = StaticFileUtils.inputStreamToBufferedReader(System.in);

    public static void main(String[] MainArgs) throws IOException {
        BasicConfigurator.configure();
        Map<String, StingraySnmpClient> clients = new HashMap<String, StingraySnmpClient>();
        StingraySnmpClient defaultClient = new StingraySnmpClient();
        List<RawSnmpUsage> rawUsageList = new ArrayList<RawSnmpUsage>();
        List<SnmpUsage> snmpUsageList = new ArrayList<SnmpUsage>();
        Map<String, List<RawSnmpUsage>> rawUsageMap = new HashMap<String, List<RawSnmpUsage>>();
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
                    System.out.printf("    max_repetitions <num> #Set the max repetitions on the clients\n");
                    System.out.printf("    non_repeaters <num> #Set the number of non repeaters on the clients\n");
                    System.out.printf("    show_state  #Show the application state and variables\n");
                    System.out.printf("    set_comparator <bi|bo|cc|vs> #Set the comparator for to BandwidthIn BandwidthOut totalConnections or ConcurrentConnections respectivly\n");
                    System.out.printf("    run_bulk <oid> [output_file] #Get usage from the default host useing the bulk on the given oid\n");
                    System.out.printf("    run_default [clientKey]#Get usage from the default host and add it to the current usage list\n");
                    System.out.printf("    clear_usage #Clear the current usageList as well as the jobs usage list\n");
                    System.out.printf("    display_usage #Display the current usage\n");
                    System.out.printf("    display_usage_diff <common|all|missing|contains>,#Checks to see which host is missing which VS names\n");
                    System.out.printf("    display_snmp_usage_rollup #Display usage rolledup map\n");
                    System.out.printf("    display_snmp_usage_list #Display jobs snmp usage list\n");
                    System.out.printf("    display_usage_map [vs_filter]#display the usage as a map of Clients\n");
                    System.out.printf("    set_retrys <num> #Sets the maximum retries\n");
                    System.out.printf("    lookup <oid> <vsName> #Lookup the given OID for the specified virtual server on the default zxtm host\n");
                    System.out.printf("    lookup_loop <oid> <vsName> #Lookup the given OID for the specified virtual server on all zxtm host, then sum them up\n");
                    System.out.printf("    client <clientKey> #Set the clientKey for the default run\n");
                    System.out.printf("    run_jobs #Run threaded jobs client for itest\n");
                    System.out.printf("    run_all #Run stats for all zxtm hosts\n");
                    System.out.printf("    run_threads #run threads for all zxtm hosts\n");
                    System.out.printf("    run_job <hostId> <aid> <lid> #get usage for loadbalancer id on host\n");
                    System.out.printf("    add_comparator <lid|hid|cc|ccssl|bi|bo|bissl|bossl> #Add comparator for jobs snmpUsage\n");
                    System.out.printf("    clear_comparators #Clear the comparators for snmpUsage\n");
                    System.out.printf("    show_threads #Show running thread stack traces\n");
                    System.out.printf("    show_config #Show current Configuration\n");
                    System.out.printf("    run_rollup #Run a single rollup with all new data\n");
                    System.out.printf("    wtf <line>  #Test candidates code for brain damage\n");
                    System.out.printf("    run_node_audit #Run node stats via threading\n");
                    System.out.printf("    exit #Exits\n");
                    System.out.printf("\n");
                } else if (cmd.equals("show_threads")) {
                    System.out.printf("Running Threads\n");
                    System.out.printf("%s\n", Debug.getThreadStacksString());
                } else if (cmd.equals("show_config")) {
                    System.out.printf("Configs\n");
                    System.out.printf("useing config = %s\n", conf.toString());
                    List<String> clientKeys = new ArrayList<String>(clients.keySet());
                    Collections.sort(clientKeys);
                    for (String clientKey : clientKeys) {
                        StingraySnmpClient client = clients.get(clientKey);
                        System.out.printf("Client[%s]=%s\n", clientKey, client.toString());
                    }
                    System.out.printf("Mem\n%s\n", Debug.showMem());
                    System.out.printf("requestId=%d\n", StingraySnmpClient.incRequestId());

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


                } else if (cmd.equals("display_usage_diff") && args.length >= 2) {
                    System.out.printf("Computing diffs\n");
                    Set<String> allVsNames = new HashSet<String>();
                    Map<String, Set<String>> clientVsNames = new HashMap<String, Set<String>>();
                    System.out.printf("deltas\n");
                    List<String> clientKeys = new ArrayList<String>(rawUsageMap.keySet());
                    Collections.sort(clientKeys);
                    for (String clientKey : clientKeys) {
                        List<RawSnmpUsage> usageList = rawUsageMap.get(clientKey);
                        for (RawSnmpUsage usage : usageList) {
                            allVsNames.add(usage.getVsName());
                            if (!clientVsNames.containsKey(clientKey)) {
                                clientVsNames.put(clientKey, new HashSet<String>());
                            }
                            clientVsNames.get(clientKey).add(usage.getVsName());
                        }
                    }


                    if (args[1].equals("common")) {
                        System.out.printf("Common VsNames:\n");
                        Set<String> common = new HashSet<String>(allVsNames);

                        for (String clientKey : clientKeys) {
                            common.retainAll(clientVsNames.get(clientKey));
                        }
                        for (String vsName : SetUtil.toSortedList(common)) {
                            System.out.printf("    %s\n", vsName);
                        }
                    } else if (args[1].equals("missing")) {
                        System.out.printf("missing vsNames:\n");
                        for (String clientKey : clientKeys) {
                            Set<String> currSet = clientVsNames.get(clientKey);
                            Set<String> missing = SetUtil.subtractSet(allVsNames, currSet);
                            for (String vsName : SetUtil.toSortedList(missing)) {
                                System.out.printf("    Client[%8s] %s\n", clientKey, vsName);
                            }
                            System.out.printf("\n");
                        }
                    } else if (args[1].equals("all")) {
                        System.out.printf("allVs vsNames:\n");
                        for (String vsName : SetUtil.toSortedList(allVsNames)) {
                            System.out.printf("   %s\n", vsName);
                        }

                    } else if (args[1].equals("contains")) {
                        System.out.printf("contained VsNames:\n");
                        for (String clientKey : clientKeys) {
                            for (String vsName : SetUtil.toSortedList(clientVsNames.get(clientKey))) {
                                System.out.printf("    Client[%8s] %s:\n", clientKey, vsName);
                            }
                        }
                        System.out.printf("\n");
                    } else {
                        System.out.printf(" command requires missing or common as second parameter\n");
                    }
                } else if (cmd.equals("display_usage_map")) {
                    String vsFilter = (args.length >= 2) ? args[1] : null;
                    System.out.printf("display usage map:\n");

                    // Build the map
                    Map<String, Map<String, RawSnmpUsage>> vsToHostMap = new HashMap<String, Map<String, RawSnmpUsage>>();
                    for (String clientKey : SetUtil.toSortedList(rawUsageMap.keySet())) {
                        List<RawSnmpUsage> usageList = rawUsageMap.get(clientKey);
                        for (RawSnmpUsage usage : usageList) {
                            String vsName = usage.getVsName();
                            if (!vsToHostMap.containsKey(vsName)) {
                                vsToHostMap.put(vsName, new HashMap<String, RawSnmpUsage>());
                            }
                            vsToHostMap.get(vsName).put(clientKey, usage);
                        }
                    }

                    if (vsFilter != null) {
                        if (vsToHostMap.containsKey(vsFilter)) {
                            printVsMap(vsFilter, vsToHostMap);
                        } else {
                            System.out.printf("pool %s was not in the usage map\n", vsFilter);
                        }
                    } else {

                        // Iterate through the map
                        for (String vsName : SetUtil.toSortedList(vsToHostMap.keySet())) {
                            printVsMap(vsName, vsToHostMap);
                        }
                    }

                } else if (cmd.equals("non_repeaters") && args.length >= 2) {
                    System.out.printf("Setting non repeater to: ");
                    int nonRepeaters = Integer.parseInt(args[1]);
                    System.out.printf(" %d\n", nonRepeaters);
                    for (String clientKey : clients.keySet()) {
                        clients.get(clientKey).setNonRepeaters(nonRepeaters);
                    }
                } else if (cmd.equals("max_repetitions")) {
                    System.out.printf("Setting max repetitions to: ");
                    int maxRepetitions = Integer.parseInt(args[1]);
                    System.out.printf(" %d\n", maxRepetitions);
                    for (String clientKey : clients.keySet()) {
                        clients.get(clientKey).setMaxRepetitions(maxRepetitions);
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
                } else if (cmd.equals("run_rollup")) {
                    System.out.printf("Running roll_up job,\n");
                    Map<Integer, SnmpUsage> usageReport = run_rollup(clients, jobClient);
                    System.out.printf("");
                    List<Integer> lbIds = new ArrayList<Integer>(usageReport.keySet());
                    Collections.sort(lbIds);
                    System.out.printf("found %d loadbalancers\n", lbIds.size());
                    for (Integer lbId : lbIds) {
                        System.out.printf("%s\n", usageReport.get(lbId).toString());
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
                    rawUsageMap = new HashMap<String, List<RawSnmpUsage>>();
                    Collections.sort(clientKeys);
                    for (String clientKey : clientKeys) {
                        StingraySnmpClient client = clients.get(clientKey);
                        SnmpClientThread clientThread = new SnmpClientThread();
                        clientThread.setClientName(clientKey);
                        clientThread.setClient(client);
                        clientThread.setException(null);
                        threads.add(clientThread);
                    }

                    // Start the threads
                    for (SnmpClientThread thread : threads) {
                        thread.start();
                    }

                    System.out.printf("Threads started\n");

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
                            List<RawSnmpUsage> usageList = new ArrayList<RawSnmpUsage>(thread.getUsage().values());
                            rawUsageList.addAll(usageList);
                            rawUsageMap.put(thread.getClientName(), new ArrayList<RawSnmpUsage>(usageList));
                        }
                    }

                } else if (cmd.equals("client") && args.length >= 2) {
                    String clientKey = args[1];
                    defaultClient = clients.get(clientKey);
                    System.out.printf("Client set to %s -> %s\n", clientKey, defaultClient.toString());
                } else if (cmd.equals("lookup") && args.length >= 3) {
                    String oid = args[1];
                    String vsName = args[2];
                    long val = defaultClient.getValueForVirtualServer(vsName, oid, false, false).toLong();
                    System.out.printf("%s for %s = %d\n", oid, vsName, val);
                } else if (cmd.equals("lookup_loop") && args.length >= 3) {
                    String oid = args[1];
                    String vsName = args[2];
                    long sum = 0l;
                    for (String clientKey : clients.keySet()) {
                        StingraySnmpClient client = clients.get(clientKey);
                        long val = client.getValueForVirtualServer(vsName, oid, false, false).toLong();
                        sum += val;
                    }
                    System.out.printf("%s for %s = %d\n", oid, vsName, sum);

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
                    rawUsageMap = new HashMap<String, List<RawSnmpUsage>>();
                } else if (cmd.equals("display_usage")) {
                    System.out.printf("Pringint usage\n");
                    Collections.sort(rawUsageList, orderBy);
                    for (int i = 0; i < rawUsageList.size(); i++) {
                        System.out.printf("entry[%d]=%s\n", i, rawUsageList.get(i).toString());
                    }
                } else if (cmd.equals("run_node_audit")) {
                    List<SnmpNodeStatus> ipv4_nodes = new ArrayList<SnmpNodeStatus>();
                    List<SnmpNodeStatus> ipv6_nodes = new ArrayList<SnmpNodeStatus>();
                    List<SnmpNodeStatus> host_nodes = new ArrayList<SnmpNodeStatus>();
                    List<Map<SnmpNodeKey, SnmpNodeStatus>> allHostsStatsMap = new ArrayList<Map<SnmpNodeKey, SnmpNodeStatus>>();
                    int ipv4_count = 0;
                    int ipv6_count = 0;
                    int expected_host_node_count = 0;
                    int actual_host_node_count = 0;
                    int wtf = 0;
                    int mangles = 0;
                    int identicles = 0;
                    List<String> clientKeys = new ArrayList<String>(clients.keySet());
                    List<SnmpNodeStatusThread> threads = new ArrayList<SnmpNodeStatusThread>();
                    double sum_of_elapsed_time = 0.0;
                    ;
                    int nThreads = 0;
                    double avgElapsed;
                    for (String clientKey : clientKeys) {
                        StingraySnmpClient client = clients.get(clientKey);
                        SnmpNodeStatusThread thread = new SnmpNodeStatusThread();
                        threads.add(thread);
                        thread.setClient(client);
                        System.out.printf("init thread %s\n", clientKey);
                        nThreads++;
                    }
                    double startTime = StaticDateTimeUtils.getEpochSeconds();
                    for (SnmpNodeStatusThread thread : threads) {
                        thread.start();
                    }
                    System.out.printf("Joining Threads\n");
                    for (SnmpNodeStatusThread thread : threads) {
                        thread.join();
                        String clientKey = thread.getClientKey();
                        double elapsed = thread.getElapsedTime();
                        System.out.printf("Thread %s joined in %f seconds\n", clientKey, elapsed);
                        sum_of_elapsed_time += elapsed;
                        allHostsStatsMap.add(thread.getStatusMap());
                    }
                    double endTime = StaticDateTimeUtils.getEpochSeconds();
                    double userTime = endTime - startTime;
                    avgElapsed = sum_of_elapsed_time / nThreads;
                    System.out.printf("total aggregate from threads was %f seconds", sum_of_elapsed_time);
                    System.out.printf("Avg time was %f seconds\n", avgElapsed);
                    System.out.printf("actual time for user was %f seconds\n", userTime);
                    System.out.printf("speed up was %f", sum_of_elapsed_time / userTime);
                    Map<SnmpNodeKey, SnmpNodeStatus> cmpMap = new HashMap<SnmpNodeKey, SnmpNodeStatus>();
                    for (Map<SnmpNodeKey, SnmpNodeStatus> nodeMap : allHostsStatsMap) {
                        for (Map.Entry<SnmpNodeKey, SnmpNodeStatus> entry : nodeMap.entrySet()) {
                            if (!cmpMap.containsKey(entry.getKey())) {
                                cmpMap.put(entry.getKey(), entry.getValue());
                            } else {
                                SnmpNodeStatus old = cmpMap.get(entry.getKey());
                                SnmpNodeStatus curr = entry.getValue();
                                if (old.equals(curr)) {
                                    identicles++;
                                } else {
                                    mangles++;
                                }
                            }
                            System.out.printf("%s -> %s\n", entry.getKey().toString(), entry.getValue().toString());
                            if (entry.getValue().getHostName() != null) {
                                actual_host_node_count++;
                            }
                            switch (entry.getValue().getIpType()) {
                                case IPUtils.IPv4:
                                    ipv4_nodes.add(entry.getValue());
                                    ipv4_count++;
                                    break;
                                case IPUtils.IPv6:
                                    ipv6_nodes.add(entry.getValue());
                                    ipv6_count++;
                                    break;
                                case IPUtils.HOST_NAME:
                                    expected_host_node_count++;
                                    host_nodes.add(entry.getValue());
                                    break;
                                default:
                                    wtf++;
                                    break;
                            }
                        }
                    }
                    String fmt = "expected ipv4[%d], ipv6[%d], host[%d] actualhosts[%d] wtf[%d] identicles[%d] mangles[%d]";
                    System.out.printf(fmt, ipv4_count, ipv6_count, expected_host_node_count, actual_host_node_count, wtf, identicles, mangles);
                    nop();
                } else if (cmd.equals("run_bulk") && args.length >= 2) {
                    String msg;
                    PrintWriter fpout = null;
                    String oid = args[1];
                    if (args.length >= 3) {
                        fpout = StaticFileUtils.openFilePrintWriter(args[2], false, false);
                    }
                    List<String> clientKeys = new ArrayList<String>(clients.keySet());
                    Collections.sort(clientKeys);
                    for (String clientKey : clientKeys) {
                        int count = 0;
                        msg = String.format("Running bulk on oid %s\n", oid);
                        System.out.printf(msg);
                        if (fpout != null) {
                            fpout.printf(msg);
                        }
                        StingraySnmpClient client = clients.get(clientKey);
                        List<VariableBinding> bindings = client.getBulkOidBindingList(oid);
                        for (VariableBinding vb : bindings) {
                            count++;
                            String vbOid = vb.getOid().toString();
                            String syntax = vb.getVariable().getSyntaxString();
                            String val = vb.getVariable().toString();
                            msg = String.format("%s %s = (%s) %s\n", clientKey, vbOid, syntax, val);
                            System.out.printf(msg);
                            if (fpout != null) {
                                fpout.printf(msg);
                            }
                        }
                        msg = String.format("Total variabls returned = %d\n", count);
                        System.out.printf(msg);
                        if (fpout != null) {
                            fpout.printf(msg);
                        }
                    }
                    if (fpout != null) {
                        fpout.flush();
                        fpout.close();
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
                } else if (cmd.equals("wtf") && args.length >= 2) {
                    StringBuilder sb = new StringBuilder();
                    int i;
                    for (i = 1; i < args.length - 1; i++) {
                        sb.append(args[i]).append(" ");
                    }
                    sb.append(args[i]);
                    String inputLine = sb.toString();
                    wtf(inputLine);

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
                        if (conf.getMaxRepetitions() != null) {
                            client.setNonRepeaters(conf.getMaxRepetitions());

                        }
                        if (conf.getNonRepeaters() != null) {
                            client.setNonRepeaters(conf.getNonRepeaters());

                        }
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

    public static void printVsMap(String vsName, Map<String, Map<String, RawSnmpUsage>> vsToHostMap) {
        for (String clientKey : SetUtil.toSortedList(vsToHostMap.get(vsName).keySet())) {
            RawSnmpUsage rawUsage = vsToHostMap.get(vsName).get(clientKey);
            System.out.printf("client[%8s]=%s\n", clientKey, rawUsage.toString());
        }
    }

    public static SnmpUsage newSnmpUsage(int loadbalancerId) {
        SnmpUsage usage = new SnmpUsage();
        usage.setLoadbalancerId(loadbalancerId);
        usage.setHostId(0);
        usage.setBytesIn(0);
        usage.setBytesOut(0);
        usage.setConcurrentConnections(0);
        usage.setConcurrentConnectionsSsl(0);
        usage.setBytesInSsl(0);
        usage.setBytesOutSsl(0);
        usage.setTotalConnections(0);
        usage.setTotalConnectionsSsl(0);
        return usage;
    }

    public static void accumulateUsage(SnmpUsage accumulator, SnmpUsage operand) {
        long abi = accumulator.getBytesIn();
        long abis = accumulator.getBytesInSsl();
        long abo = accumulator.getBytesOut();
        long abos = accumulator.getBytesOutSsl();
        int acc = accumulator.getConcurrentConnections();
        int accs = accumulator.getConcurrentConnectionsSsl();
        long atc = accumulator.getTotalConnections();
        long atcs = accumulator.getTotalConnectionsSsl();

        long obi = operand.getBytesIn();
        long obis = operand.getBytesInSsl();
        long obo = operand.getBytesOut();
        long obos = operand.getBytesOutSsl();
        int occ = operand.getConcurrentConnections();
        int occs = operand.getConcurrentConnectionsSsl();
        long otc = operand.getTotalConnections();
        long otcs = operand.getConcurrentConnectionsSsl();

        if (obi > 0) {
            accumulator.setBytesIn(abi + obi);
        }
        if (obis > 0) {
            accumulator.setBytesInSsl(abis + obis);
        }
        if (obo > 0) {
            accumulator.setBytesOut(abo + obo);
        }
        if (obos > 0) {
            accumulator.setBytesOutSsl(abos + obos);
        }
        if (occ > 0) {
            accumulator.setConcurrentConnections(acc + occ);
        }
        if (occs > 0) {
            accumulator.setConcurrentConnectionsSsl(accs + occs);
        }
        if (otc > 0) {
            accumulator.setTotalConnections(atc + otc);
        }
        if (otcs > 0) {
            accumulator.setTotalConnectionsSsl(atcs + otcs);
        }

    }

    public static Map<Integer, SnmpUsage> run_rollup(Map<String, StingraySnmpClient> clients, StingrayUsageClient jobClient) throws InterruptedException {
        Map<Integer, SnmpUsage> snmpUsage = new HashMap<Integer, SnmpUsage>();
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
                for (Map.Entry<Integer, SnmpUsage> currUsage : thread.getUsage().entrySet()) {
                    Integer lid = currUsage.getKey();
                    SnmpUsage usage = currUsage.getValue();
                    if (!snmpUsage.containsKey(lid)) {
                        snmpUsage.put(lid, newSnmpUsage(lid));
                    }
                    accumulateUsage(snmpUsage.get(lid), usage);
                }
            }
        }
        return snmpUsage;
    }

    public static void wtf(String s) {

        boolean failed = false;

        String[] parts = s.split(" ");
        int maxExpandLength = parts[1].length() - parts[0].length() + 1;
        if (maxExpandLength <= 0) {
            System.out.println("No");
            failed = true;
        } else {

            // We will stash our set of working codes here
            List<String> codes = new ArrayList<String>();
            codes.add("");
            boolean initialLoop = true;

            // Loop thru all numbers
            for (int i = 0; i < parts[0].length(); i++) {
                String number = parts[0].substring(i, i + 1);

                // Add all possible combos to all values in codes list.
                // Check to see if still valid as we go.  Eliminate those that are not.
                if ((number.equals("0")) || (number.equals("1"))) {
                    // Generate all possible codes with existing codes prepended.
                    // Cull out those that do not work anymore and add those that do.
                    // Accumulate all new codes in addTheseCodes.  Them move to codes.
                    List<String> addTheseCodes = new ArrayList<String>();
                    for (int len = 0; len < maxExpandLength; len++) {
                        // Generate a set of sequences, A AA, AAA up to maxExpandLength.
                        StringBuffer sb = new StringBuffer();
                        for (int cc = 0; cc <= len; cc++) {
                            sb.append("A");
                        }
                        addTheseCodes.add(sb.toString());
                    }
                    // Add a list of Bs if we are a 1
                    if (number.equals("1")) {
                        for (int len = 0; len < maxExpandLength; len++) {
                            // Generate a set of sequences, A AA, AAA up to maxExpandLength.
                            StringBuffer sb = new StringBuffer();
                            for (int cc = 0; cc <= len; cc++) {
                                sb.append("B");
                            }
                            addTheseCodes.add(sb.toString());
                        }
                    }
                    // For each sequence, prepend prior codes and see of
                    // each code still works.  If it does, add it to a new list because.
                    // you can't iterrate thru codes and modify it.
                    List<String> theseCodesWork = new ArrayList<String>();
                    for (String prepend : codes) {
                        for (String newCodes : addTheseCodes) {
                            String newCode = prepend + newCodes;
                            int tl = newCode.length();
                            if (tl <= parts[1].length()) {
                                if (newCode.equalsIgnoreCase(parts[1].substring(0, tl))) {
                                    theseCodesWork.add(newCode);
                                }
                            }
                        }
                    }
                    // Delete our initial run flag
                    if ((initialLoop == true) && (codes.size() == 1)) {
                        String ss = codes.get(0);
                        if (ss.length() == 0) {
                            codes.clear();
                        }
                        initialLoop = false;
                    }

                    // Finally, add any working codes back to the codes list
                    for (String addMe : theseCodesWork) {
                        codes.add(addMe);
                    }

                    // Finally, at this point, if codes is ever empty, then
                    // we know we don't code for the result sting
                    if (codes.isEmpty()) {
                        System.out.println("No");
                        failed = true;
                    }
                }
            } // end numbers loop
            // Alghout we've colled, some of the codes that remian may be too short
            // We now need to see if any codes that remain in the marching codes list
            // match the length of the letter string
            if (failed == false) {
                int len = parts[1].length();
                boolean lemMatches = false;
                for (String ss : codes) {
                    if (ss.length() == len) {
                        lemMatches = true;
                    }
                }
                if (lemMatches == false) {
                    failed = true;
                    System.out.println("No");
                }
            }
        }
        // If we made it this far.
        if (failed == false) {
            System.out.println("Yes");
        }
    }

    public static void nop() {
    }
}
