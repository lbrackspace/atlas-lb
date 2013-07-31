package org.openstack.atlas.logs.cachefaker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.joda.time.DateTime;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.config.HadoopLogsConfigs;
import org.openstack.atlas.logs.hadoop.comparators.LbLidAidNameContainerComparator;
import org.openstack.atlas.service.domain.pojos.LoadBalancerIdAndName;
import org.openstack.atlas.util.HdfsUtils;
import org.openstack.atlas.util.LbLidAidNameContainer;
import org.openstack.atlas.util.LogFileNameBuilder;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.itest.hibernate.HibernateDbConf;
import org.openstack.atlas.util.itest.hibernate.HuApp;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.StaticLogUtils;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;

public class CacheFakerMain {

    public static final Random rnd = new Random();
    public static final int BUFFSIZE = 1024 * 32;

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

    public static List<Long> getHourKeysInRange(long beg, long end) {
        List<Long> hoursKeysL = new ArrayList<Long>();
        DateTime currDt = StaticDateTimeUtils.hourKeyToDateTime(beg, true);
        DateTime endDt = StaticDateTimeUtils.hourKeyToDateTime(end, true);
        while (true) {
            if (currDt.isAfter(endDt)) {
                break;
            }
            Long currHour = StaticDateTimeUtils.dateTimeToHourLong(currDt);
            hoursKeysL.add(currHour);
            currDt = currDt.plusHours(1);
        }
        return hoursKeysL;
    }

    public static void writeZipFile(String cacheDir, LbLidAidNameContainer lb, int fileHour) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        int accountId = lb.getAccountId();
        int loadbalancerId = lb.getLoadbalancerId();
        String lbName = lb.getName();
        String zipName = LogFileNameBuilder.getZipFileName(loadbalancerId, fileHour);
        String zipContentsName = LogFileNameBuilder.getZipContentsName(loadbalancerId, fileHour);
        String lidStr = Integer.toString(loadbalancerId);
        String aidStr = Integer.toString(accountId);
        String fileHourStr = Integer.toString(fileHour);
        String containerName = LogFileNameBuilder.getContainerName(lidStr, lbName, fileHourStr); // CloudFiles container name
        String cfName = LogFileNameBuilder.getRemoteFileName(lidStr, lbName, fileHourStr); // CloudFiles name

        String zipDir = StaticFileUtils.mergePathString(cacheDir, fileHourStr, aidStr);
        // Attempt to make the directory
        File zipDirFile = new File(zipDir);
        zipDirFile.mkdirs();
        String zipFilePath = StaticFileUtils.mergePathString(zipDir, zipName);
        System.out.printf("Opening %s for writing\n", zipFilePath);
        OutputStream os = StaticFileUtils.openOutputFile(zipFilePath);
        ZipOutputStream zos = new ZipOutputStream(os);
        zos.setComment(String.format("Fake log file generated by CacheFakerMain"));
        zos.putNextEntry(new ZipEntry(zipContentsName));
        String fmt = "This file was generated by CachFakerMain\ndate %s\nContainer %s\nremoteFile %s\naid %s\nlid %s\n";
        DateTime now = StaticDateTimeUtils.nowDateTime(true);
        String dateStr = StaticDateTimeUtils.sqlDateTimeFormat.print(now);
        String msg = String.format(fmt, dateStr, containerName, cfName, aidStr, lidStr);
        byte[] msgBytes = msg.getBytes("utf-8");
        zos.write(msgBytes);
        zos.closeEntry();
        zos.finish();
        os.close();
    }

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

    public static void main(String[] args) throws ParseException, UnsupportedEncodingException, FileNotFoundException, IOException {
        Map<String, String> kwArgs = argMapper(args);
        String[] nonKeywordArgs = stripKwArgs(args);
        if (nonKeywordArgs.length < 2) {
            System.out.printf("Usage is <configFile> <startHour> [stopHour] [outdir=someDir] ");
            System.out.printf("[aid=SomeAid] [lid=SomeLid] [nrandom=nentries]\n");
            System.out.printf("\n");
            System.out.printf("Create fake zip files for testing the cache reuploader\n");
            System.out.printf("Use the keyword args to limit zips to only the specified account or loadbalancers\n");
            System.out.printf("%s\n", HibernateDbConf.exampleJson);
            return;
        }

        BufferedReader stdin = StaticFileUtils.inputStreamToBufferedReader(System.in, BUFFSIZE);
        System.out.printf("Press enter to continue\n");
        stdin.readLine();


        String jsonConfFileName = StaticFileUtils.expandUser(nonKeywordArgs[0]);
        long begHour = Long.parseLong(nonKeywordArgs[1]);
        long endHour = (nonKeywordArgs.length > 2) ? Long.parseLong(nonKeywordArgs[2]) : begHour;
        Integer aid = (kwArgs.containsKey("aid")) ? Integer.parseInt(kwArgs.get("aid")) : null;
        Integer lid = (kwArgs.containsKey("lid")) ? Integer.parseInt(kwArgs.get("lid")) : null;

        HuApp huApp = new HuApp();
        HibernateDbConf hConf = HibernateDbConf.newHibernateConf(jsonConfFileName);
        System.out.printf("Useing db config %s\n", hConf.toString());
        huApp.setDbMap(hConf);
        System.out.printf("Reading LoadBalancers from databases\n");
        double startTime = Debug.getEpochSeconds();
        Map<Integer, LbLidAidNameContainer> lbMap = getLbIdMap(huApp);
        lbMap = filterLbIdMap(lbMap, aid, lid);
        double stopTime = Debug.getEpochSeconds();
        int nLbsFound = lbMap.size();
        double deltaTime = stopTime - startTime;
        System.out.printf("Took %f seconds to produce read %d records\n", deltaTime, nLbsFound);
        String outdir = HadoopLogsConfigs.getCacheDir();
        if (kwArgs.containsKey("outdir")) {
            outdir = kwArgs.get("outdir");
        }
        List<LbLidAidNameContainer> lbs = new ArrayList<LbLidAidNameContainer>(lbMap.values());
        Collections.sort(lbs, new LbLidAidNameContainerComparator());
        List<Long> hourKeys = getHourKeysInRange(begHour, endHour);
        Collections.sort(hourKeys);
        System.out.printf("Generating zip files for hours: ");
        for (Long hourKey : hourKeys) {
            System.out.printf("%d\n", hourKey);
        }
        System.out.printf("\n");
        System.out.printf("Will be generating zips for:\n");
        for (LbLidAidNameContainer lb : lbs) {
            System.out.printf("aid[%8d] lb[%6d] \"%s\"\n", lb.getAccountId(), lb.getLoadbalancerId(), lb.getName());
        }
        long nZips = (long) hourKeys.size() * (long) lbs.size();
        System.out.printf("Are you sure you want to generate %d logs\n", nZips);
        if (inputStream(stdin, "Y")) {
            System.out.printf("Generating zips:\n");
            for (Long hourKey : hourKeys) {
                for (LbLidAidNameContainer lb : lbs) {
                    System.out.printf("Writing file for aid[%8d] lid[%8d]\n", lb.getAccountId(), lb.getLoadbalancerId());
                    writeZipFile(outdir, lb, hourKey.intValue());
                }
            }
        } else {
            System.out.printf("Not generating zips\n");
        }
    }
}
