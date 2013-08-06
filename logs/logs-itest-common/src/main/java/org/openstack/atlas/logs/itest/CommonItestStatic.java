package org.openstack.atlas.logs.itest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.joda.time.DateTime;
import org.openstack.atlas.service.domain.pojos.LoadBalancerIdAndName;
import org.openstack.atlas.util.itest.hibernate.HuApp;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;

public class CommonItestStatic {

    public static final String HDUNAME = "HADOOP_USER_NAME";

    public static boolean inputStream(BufferedReader stdin, String val) throws IOException {
        String[] resp = stripBlankArgs(stdin.readLine());
        return (resp.length > 0 && resp[0].equalsIgnoreCase(val));
    }

    public static String[] stripBlankArgs(String line) {
        int nargs = 0;
        int i;
        int j;
        String[] argsIn = line.replace("\r", "").replace("\n", "").split(" ");
        for (i = 0; i < argsIn.length; i++) {
            if (argsIn[i].length() > 0) {
                nargs++;
            }
        }
        String[] argsOut = new String[nargs];
        j = 0;
        for (i = 0; i < argsIn.length; i++) {
            if (argsIn[i].length() > 0) {
                argsOut[j] = argsIn[i];
                j++;
            }
        }
        return argsOut;
    }

    public static Map<String, String> argMapper(String[] args) {
        Map<String, String> argMap = new HashMap<String, String>();
        for (String arg : args) {
            String[] kwArg = arg.split("=");
            if (kwArg.length == 2) {
                argMap.put(kwArg[0], kwArg[1]);
            }
        }
        return argMap;
    }

    public static String[] stripKwArgs(String[] args) {
        String[] argsOut;
        List<String> filteredArgs = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.split("=").length >= 2) {
                continue;
            }
            filteredArgs.add(arg);
        }
        argsOut = filteredArgs.toArray(new String[filteredArgs.size()]);
        return argsOut;
    }

    public static Map<Integer, LoadBalancerIdAndName> getLbIdMap(HuApp huApp) {
        Map<Integer, LoadBalancerIdAndName> map = new HashMap<Integer, LoadBalancerIdAndName>();
        for (LoadBalancerIdAndName lb : getAllLoadbalancerIdsAndNames(huApp)) {
            int lbId = lb.getLoadbalancerId();
            map.put(lbId, lb);
        }
        return map;
    }

    public static List<LoadBalancerIdAndName> getAllLoadbalancerIdsAndNames(HuApp huApp) {
        List<LoadBalancerIdAndName> lbs = new ArrayList<LoadBalancerIdAndName>();
        String queryString = "select l.id,l.accountId,l.name from LoadBalancer l";
        huApp.begin();
        List rows = huApp.getList(queryString);
        huApp.commit();
        for (Object uncastedRowArrayObj : rows) {
            // Each element of rows is actually an Object[] whith each element representing a column
            Object[] row = (Object[]) uncastedRowArrayObj;
            LoadBalancerIdAndName lb = new LoadBalancerIdAndName();
            lb.setLoadbalancerId((Integer) row[0]);
            lb.setAccountId((Integer) row[1]);
            lb.setName((String) row[2]);
            lbs.add(lb);
        }
        return lbs;
    }

    public static Map<Integer, LoadBalancerIdAndName> filterLbIdMap(Map<Integer, LoadBalancerIdAndName> mapIn, Integer aid, Integer lid) {
        Map<Integer, LoadBalancerIdAndName> mapOut = new HashMap<Integer, LoadBalancerIdAndName>();
        for (Entry<Integer, LoadBalancerIdAndName> entry : mapIn.entrySet()) {
            Integer key = entry.getKey();
            LoadBalancerIdAndName val = entry.getValue();
            int vLid = val.getLoadbalancerId();
            int vAid = val.getAccountId();
            if (lid != null && vLid != lid) {
                continue; // Skip this entry as it didn't math the Lid
            }
            if (aid != null && vAid != aid) {
                continue; // Skip this entry as the it didn't match the Aid
            }
            mapOut.put(key, val);
        }
        return mapOut;
    }
}
