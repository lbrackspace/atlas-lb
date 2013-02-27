package org.openstack.atlas.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class StaticStringUtils {

    private static final Pattern IS_BLANK_PATTERN = Pattern.compile("[\\s]*");

    public static boolean isEmpty(String st) {
        return st == null || st.length() == 0;
    }

    public static boolean isBlank(String st) {
        return isEmpty(st) || IS_BLANK_PATTERN.matcher(st).matches();
    }

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

    public static <T> String collectionToString(Collection<T> collection, String delimiter) {
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
}
