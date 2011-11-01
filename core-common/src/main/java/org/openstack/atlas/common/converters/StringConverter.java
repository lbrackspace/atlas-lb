package org.openstack.atlas.common.converters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StringConverter {

    public static String commaSeperatedStringList(List<String> strList) {
        StringBuffer sb = new StringBuffer();
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
        StringBuffer sb;
        Exception currEx;
        String msg;

        sb = new StringBuffer();
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
        StringBuffer sb = new StringBuffer();
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
        StringBuffer sb = new StringBuffer();
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
