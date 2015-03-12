package org.openstack.atlas.util.staticutils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class StaticStringUtils {

    public static String lpadLong(long val, String pad, int npad) {
        return lpad(Long.toString(val), pad, npad);
    }

    public static String lpad(String val, String pad, int npad) {
        StringBuilder sb = new StringBuilder();
        int nspaces = npad - val.length();
        for (int i = 0; i < nspaces; i++) {
            sb.append(pad);
        }
        sb.append(val);
        return sb.toString();
    }

    public static String getExtendedStackTrace(Throwable th) {
        Throwable t;
        StringBuilder sb = new StringBuilder();
        Exception currEx;
        String msg;

        t = th;
        while (t != null) {
            if (t instanceof Exception) {
                currEx = (Exception) t;
                sb.append(String.format("\"%s\":\"%s\"\n", currEx.getClass().getName(), currEx.getMessage()));
                for (StackTraceElement se : currEx.getStackTrace()) {
                    sb.append(String.format("%s\n", se.toString()));
                }
                sb.append("\n");
                t = t.getCause();
            }
        }
        return sb.toString();
    }

    public static <K, V> String mapToString(Map<K, V> map, String delimiter) {
        if (map == null) {
            return "null";
        }
        if (map.isEmpty()) {
            return "[]";
        }
        List<K> keys = new ArrayList<K>(map.keySet());
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < keys.size() - 1; i++) {
            K key = keys.get(i);
            V val = map.get(key);
            if (key == null) {
                sb.append("null");
            } else {
                sb.append(key.toString());
            }
            sb.append("=");
            if (val == null) {
                sb.append("null");
            } else {
                sb.append(val.toString());
            }
            sb.append(delimiter);
        }
        K key = keys.get(keys.size() - 1);
        V val = map.get(key);
        if (key == null) {
            sb.append("null");
        } else {
            sb.append(key.toString());
        }
        sb.append("=");
        if (val == null) {
            sb.append("null");
        } else {
            sb.append(val.toString());
        }
        sb.append("]");
        return sb.toString();
    }

    public static <T> String collectionToString(Collection<T> collection, String delimiter) {
        if (collection == null) {
            return "null";
        }
        if (collection.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        List<T> list = new ArrayList<T>(collection);
        for (int i = 0; i < list.size() - 1; i++) {
            T entry = list.get(i);
            if (entry == null) {
                sb.append("null").append(delimiter);
                continue;
            }
            sb.append(entry.toString()).append(delimiter);
        }
        sb.append(list.get(list.size() - 1)).append("]");
        return sb.toString();
    }

    public static String truncate(String stringIn, int maxLen) {
        if (stringIn == null) {
            return stringIn;
        }
        return stringIn.substring(0, Math.min(maxLen, stringIn.length() - 1));
    }

    public static <K, V> String mapToString(Map<K, V> mapIn) {
        if (mapIn == null) {
            return "null";
        }
        if (mapIn.size() <= 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Entry<K, V> entry : mapIn.entrySet()) {
            K key = entry.getKey();
            V val = entry.getValue();
            sb.append("(");
            if (key == null) {
                sb.append("null:");
            } else {
                sb.append(key.toString()).append(":");
            }

            if (val == null) {
                sb.append("null),");
            } else {
                sb.append(val.toString()).append("),");
            }
        }
        sb.append("]");
        return sb.toString();
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

    public static String justOneCR(String strIn) {
        StringBuilder sb = new StringBuilder();
        String[] Strs = strIn.split("\n");
        for (String str : Strs) {
            sb.append(str);
        }
        sb.append("\n");
        return sb.toString();

    }

    public static String showDiff(String a, String b) {
        StringBuilder sb = new StringBuilder();
        int i;
        int la = a.length();
        int lb = b.length();
        int li = Math.max(la, lb);
        for (i = 0; i < li; i++) {
            int aPoint = (i < la) ? a.codePointAt(i) : 0;
            int bPoint = (i < lb) ? b.codePointAt(i) : 0;
            String aStr = new String(new int[]{aPoint}, 0, 1);
            String bStr = new String(new int[]{bPoint}, 0, 1);
            int delta = aPoint ^ bPoint;
            sb.append(String.format("str[%3d]: \"%s\" == \"%s\": Diff %8d\n", i, aStr, bStr, delta));
        }
        return sb.toString();
    }
}
