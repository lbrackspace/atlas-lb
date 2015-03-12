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
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public class CommonItestStatic {

    public static final String HDUNAME = "HADOOP_USER_NAME";

    public static boolean inputStream(BufferedReader stdin, String val) throws IOException {
        String[] resp = StaticStringUtils.stripBlankArgs(stdin.readLine());
        return (resp.length > 0 && resp[0].equalsIgnoreCase(val));
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
