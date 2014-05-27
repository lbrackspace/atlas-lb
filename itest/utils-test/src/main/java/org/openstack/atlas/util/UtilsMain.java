package org.openstack.atlas.util;

import java.io.BufferedReader;
import java.util.Calendar;
import org.joda.time.DateTime;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public class UtilsMain {

    private static BufferedReader stdin = StaticFileUtils.inputStreamToBufferedReader(System.in);

    public static void main(String[] mainArgs) {
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
                    showHelp();
                } else if (cmd.equals("mem")) {
                    System.out.printf("Mem\n%s\n", Debug.showMem());
                } else if (cmd.equals("gc")) {
                    System.out.printf("incoking garbage collector\n");
                    Debug.gc();
                    System.out.printf("garbage collector done\n");
                } else if (cmd.equals("now")) {
                    Calendar now = Calendar.getInstance();
                    DateTime dt = StaticDateTimeUtils.toDateTime(now, false);
                    DateTime utcDt = StaticDateTimeUtils.toDateTime(now, true);
                    System.out.printf("Apachetime: %s\n", StaticDateTimeUtils.toApacheDateTime(dt));
                    System.out.printf("SqlTime:    %s\n", StaticDateTimeUtils.toSqlTime(dt));
                    System.out.printf("iso8601:    %s\n", StaticDateTimeUtils.toIso(dt));
                    System.out.printf("utc:        %s\n", StaticDateTimeUtils.toIso(utcDt));
                }
            } catch (Exception ex) {
                System.out.printf("Exception: %s\n", Debug.getExtendedStackTrace(ex));
            }

        }
    }

    private static void showHelp() {
        System.out.printf("\n");
        System.out.printf("    usage is\n");
        System.out.printf("    mem  #displays JVM memory\n");
        System.out.printf("    gc   #invoke garbage collector\n");
        System.out.printf("    now  #display time representations.\n");
        System.out.printf("\n");

    }
}
