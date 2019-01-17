package org.openstack.atlas.service.domain.services.helpers;

import java.io.BufferedReader;
import javax.ws.rs.core.Response;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;
import org.openstack.atlas.restclients.dns.StaticDNSClientUtils;

public class RdnsMain {

    private static final String LINE = "----------------------------------\n";

    public static void main(String[] mainArgs) {
        BytesList wasteBytes = new BytesList();
        RdnsHelper rdns = RdnsHelper.newRdnsHelper(0);
        BufferedReader stdin = StaticFileUtils.inputStreamToBufferedReader(System.in);
        String msgFmt;
        String msg;
        while (true) {
            try {
                System.out.printf("\n");
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
                    System.out.printf("Usage is\n");
                    System.out.printf(LINE);
                    System.out.printf("mem               # Show memory usage\n");
                    System.out.printf("gc                # Run garbage collector\n");
                    System.out.printf("wb <nBytes>       # Waste nBytes to test garbage collector\n");
                    System.out.printf("fb                # Free bytes on the bytes list\n");
                    System.out.printf("init <aid>        # initialize rDNS client with accountid\n");
                    System.out.printf("showconfig        # Show rdns configuration\n");
                    System.out.printf("token             # get impersonation token for account id\n");
                    System.out.printf("user              # get username for accountID\n");
                    System.out.printf("delptr <lid> <ip> # Delete lid ptr ip address\n");
                    System.out.printf("exit              # Exits\n");
                    System.out.printf("\n");
                } else if (cmd.equals("mem")) {
                    msg = Debug.showMem();
                    System.out.printf("Memory Usage:\n");
                    System.out.printf(LINE);
                    System.out.printf("%s\n", msg);
                } else if (cmd.equals("gc")) {
                    msg = Debug.showMem();
                    System.out.printf("Memory before garbage collect:\n");
                    System.out.printf(LINE);
                    System.out.printf("%s\n", msg);
                    Debug.gc();
                    msg = Debug.showMem();
                    System.out.printf("Memory after garbage collect:\n%s\n%s\n", LINE, msg);
                } else if (cmd.equals("exit")) {
                    System.out.printf("Exiting\n");
                    return;
                } else if (cmd.equals("wb") && (args.length >= 2)) {
                    int nBytes = Integer.valueOf(args[1]).intValue();
                    System.out.printf("Adding %d bytes on bytes list\n", nBytes);
                    wasteBytes.addFilledBytes(nBytes);
                    long totalBytes = wasteBytes.length();
                    System.out.printf("Total bytes wasted is %d\n", totalBytes);
                } else if (cmd.equals("fb")) {
                    long totalBytes = wasteBytes.length();
                    System.out.printf("Clearing %d bytes off bytes list\n", totalBytes);
                    wasteBytes.clear();
                } else if (cmd.equals("init") && (args.length >= 2)) {
                    int aid = Integer.parseInt(args[1]);
                    rdns = RdnsHelper.newRdnsHelper(aid);
                    System.out.printf("Initialized rdns client with aid %d\n", aid);

                } else if (cmd.equals("showconfig")) {
                    System.out.printf("Config\n");
                    System.out.printf(LINE);
                    System.out.printf("%s\n", rdns.toString());

                } else if (cmd.equals("token")) {
                    int aid = rdns.getAccountId();
                    System.out.printf("Fetching token for account %d\n", aid);
                    String token = rdns.getImpersanatedUserToken();
                    System.out.printf("Impersonation token for aid %d is %s\n", aid, token);
                } else if (cmd.equals("user")) {
                    String user = rdns.getUserName();
                    System.out.printf("user name is: %s\n", user);
                } else if (cmd.equals("delptr") && (args.length >= 3)) {
                    int lid = Integer.parseInt(args[1]);
                    String ip = args[2];
                    Response resp = rdns.delPtrPubRecord(lid, ip);
                    int status = resp.getStatus();
                    System.out.printf("Statis: %d\n", status);
                    String respClassName = resp.getEntity().getClass().getName();
                    System.out.printf("Response class name: %s\n", respClassName);
                    String respString = StaticDNSClientUtils.clientResponseToString(resp);
                    System.out.printf("resp:\n");
                    System.out.printf(LINE);
                    System.out.printf("%s\n", respString);
                } else {
                    System.out.printf("Unknown Command\n");
                }
            } catch (Exception ex) {
                String exception_msg = Debug.getExtendedStackTrace(ex);
                System.out.printf("Exception: %s\n", exception_msg);
            }
        }
    }
}
