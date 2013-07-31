package org.openstack.atlas.logs.cachefaker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.config.HadoopLogsConfigs;
import org.openstack.atlas.logs.hadoop.comparators.LbLidAidNameContainerComparator;
import org.openstack.atlas.service.domain.pojos.LoadBalancerIdAndName;
import org.openstack.atlas.util.LbLidAidNameContainer;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.itest.hibernate.HibernateDbConf;
import org.openstack.atlas.util.itest.hibernate.HuApp;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class CacheFakerMain {

    public static final Random rnd = new Random();

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

    // Copyed from LoadbalancerRepository but without Spring
    public static List<LoadBalancerIdAndName> getActiveLoadbalancerIdsAndNames(HuApp huApp) {
        List<LoadBalancerIdAndName> lbs = new ArrayList<LoadBalancerIdAndName>();
        String queryString = "select l.id,l.accountId,l.name from LoadBalancer l where l.status = 'ACTIVE'";
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

    public static Map<Integer, LbLidAidNameContainer> getLbIdMap(HuApp huApp) {
        Map<Integer, LbLidAidNameContainer> map = new HashMap<Integer, LbLidAidNameContainer>();
        for (LoadBalancerIdAndName lb : getActiveLoadbalancerIdsAndNames(huApp)) {
            LbLidAidNameContainer hlb = new LbLidAidNameContainer();
            hlb.setName(lb.getName());
            hlb.setLoadbalancerId(lb.getLoadbalancerId());
            hlb.setAccountId(lb.getAccountId());
            map.put(hlb.getLoadbalancerId(), hlb);
        }
        return map;
    }

    public static Map<Integer, LbLidAidNameContainer> filterLbIdMap(Map<Integer, LbLidAidNameContainer> mapIn, Integer aid, Integer lid) {
        Map<Integer, LbLidAidNameContainer> mapOut = new HashMap<Integer, LbLidAidNameContainer>();
        for (Entry<Integer, LbLidAidNameContainer> entry : mapIn.entrySet()) {
            Integer key = entry.getKey();
            LbLidAidNameContainer val = entry.getValue();
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

    public static void main(String[] args) throws ParseException, UnsupportedEncodingException, FileNotFoundException, IOException {
        if (args.length < 3) {
            System.out.printf("Usage is <configFile> <startHour> <stopHour> [outdir=someDir] ");
            System.out.printf("[aid=SomeAid] [lid=SomeLid] [nrandom=nentries]\n");
            System.out.printf("\n");
            System.out.printf("Create fake zip files for testing the cache reuploader\n");
            System.out.printf("Use the keyword args to limit zips to only the specified account or loadbalancers\n");
            System.out.printf("%s\n", HibernateDbConf.exampleJson);
            return;
        }
        String jsonConfFileName = StaticFileUtils.expandUser(args[0]);
        int startHour = Integer.parseInt(args[1]);
        int stopHourt = Integer.parseInt(args[2]);
        Map<String, String> kwArgs = argMapper(args);
        HuApp huApp = new HuApp();
        HibernateDbConf hConf = HibernateDbConf.newHibernateConf(jsonConfFileName);
        System.out.printf("Useing db config %s\n", hConf.toString());
        huApp.setDbMap(hConf);
        System.out.printf("Reading LoadBalancers from databases\n");
        double startTime = Debug.getEpochSeconds();
        Map<Integer, LbLidAidNameContainer> lbMap = getLbIdMap(huApp);
        double stopTime = Debug.getEpochSeconds();
        int nLbsFound = lbMap.size();
        double deltaTime = stopTime - startTime;
        System.out.printf("Took %f seconds to produce read %d records\n", deltaTime, nLbsFound);
        String outdir = HadoopLogsConfigs.getCacheDir();
        if (kwArgs.containsKey("outdir")) {
            outdir = kwArgs.get("outdir");
        }
        Integer aid = (kwArgs.containsKey("aid")) ? Integer.parseInt(kwArgs.get("aid")) : null;
        Integer lid = (kwArgs.containsKey("lid")) ? Integer.parseInt(kwArgs.get("lid")) : null;
        lbMap = filterLbIdMap(lbMap, aid, lid);
        List<LbLidAidNameContainer> lbs = new ArrayList<LbLidAidNameContainer>(lbMap.values());
        Collections.sort(lbs, new LbLidAidNameContainerComparator());
        for (LbLidAidNameContainer lb : lbs) {
            System.out.printf("%d %d %s\n", lb.getAccountId(), lb.getLoadbalancerId(), lb.getName());
        }
    }
}
