package org.openstack.atlas.atom.util;

import com.sun.jersey.api.client.ClientResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class ResponseUtil {
    public static final int PAGESIZE = 4096;
    public static final int FRAGSIZE = 4;


    /**
     * @param throwable the throwable exception used to parse the extended stack trace.
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
     * @param ex the exception used to parse the stack trace
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

    /**
     *
     * @param response the clientResponse
     * @return string with response body
     * @throws IOException
     */
    public static String processResponseBody(ClientResponse response) throws IOException {
        InputStream is = response.getEntityInputStream();
        StringBuilder sb = new StringBuilder(ResponseUtil.PAGESIZE);

        int nbytes;
        do {
            byte[] buff = new byte[ResponseUtil.FRAGSIZE];
            nbytes = is.read(buff);
            String frag = new String(buff, "UTF-8");
            sb.append(frag);
        } while (nbytes > 0);
        return sb.toString();
    }
}
