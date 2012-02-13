package org.openstack.atlas.util.converters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StringConverter {
    private static final int SB_INIT_SIZE = 4096;
    public static String commaSeperatedStringList(List<String> strList) {
        StringBuilder sb = new StringBuilder(SB_INIT_SIZE);
        int i;
        for (i = 0; i < strList.size(); i++) {
            sb.append(strList.get(i));
            if (i < strList.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public static String getExtendedStackTrace(Throwable ti) {
        Throwable t;
        StringBuilder sb;
        Exception currEx;
        String msg;

        sb = new StringBuilder(SB_INIT_SIZE);
        t = ti;
        while (t != null) {
            if (t instanceof Exception) {
                currEx = (Exception) t;
                msg = String.format("%s\n", getStackTrace(currEx));
                sb.append(msg);
                t = t.getCause();
            }
        }
        return sb.toString();
    }

    public static String getStackTrace(Exception ex) {
        StringBuilder sb = new StringBuilder(SB_INIT_SIZE);
        sb.append(String.format("Exception: %s:%s\n", ex.getMessage(), ex.getClass().getName()));
        for (StackTraceElement se : ex.getStackTrace()) {
            sb.append(String.format("%s\n", se.toString()));
        }
        return sb.toString();
    }

    public static String integersAsString(Collection<Integer> ids) {
        List<Integer> idList = new ArrayList<Integer>(ids);
        return integersAsString(idList);
    }

    public static String integersAsString(List<Integer> ids) {
        StringBuilder sb = new StringBuilder(SB_INIT_SIZE);
        for (int i = 0; i < ids.size(); i++) {
            sb.append(String.format("%d", ids.get(i)));
            if (i >= ids.size() - 1) {
                continue;
            }
            sb.append(",");
        }
        return sb.toString();
    }
}
