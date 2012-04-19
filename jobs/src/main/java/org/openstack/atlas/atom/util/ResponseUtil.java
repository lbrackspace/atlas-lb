package org.openstack.atlas.atom.util;

import java.util.Calendar;

public class ResponseUtil {
    public static final int PAGESIZE = 4096;
    public static final int FRAGSIZE = 4;


    /**
     *
     * @param throwable the throwable exception used to parse the extended stack trace.
     *
     * @return the string based off the throwables stack trace
     */
    public static String getExtendedStackTrace(Throwable throwable) {
        Throwable t;
        StringBuilder sb;
        Exception currEx;
        String msg;

        sb = new StringBuilder(PAGESIZE);
        t = throwable;
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

    /**
     *
     * @param ex the exception used to parse the stack trace
     *
     * @return the string parsed from the exceptions stack trace
     */
    public static String getStackTrace(Exception ex) {
        StringBuilder sb = new StringBuilder(PAGESIZE);
        sb.append(String.format("Exception: %s:%s\n", ex.getMessage(), ex.getClass().getName()));
        for (StackTraceElement se : ex.getStackTrace()) {
            sb.append(String.format("%s\n", se.toString()));
        }
        return sb.toString();
    }

    public static Calendar getNow() {
        return Calendar.getInstance();
    }

    public static Calendar getStartTime() {
        Calendar c = getNow();
        c.add(Calendar.WEEK_OF_MONTH, -1);
        return c;
    }
}
