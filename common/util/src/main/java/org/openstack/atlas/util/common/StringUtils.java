package org.openstack.atlas.util.common;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

public class StringUtils {

    public static String displayParsedInt(String strIn) {
        try {
            int i = Integer.parseInt(strIn);
            return String.format("%d", i);
        } catch (NumberFormatException ex) {
            return "INVALID";
        }
    }

    // Got tired of spelling ExtendedStackTrace
    public static String getEST(Throwable th) {
        return getExtendedStackTrace(th);
    }

    public static String getExtendedStackTrace(Throwable th) {
        Throwable t;
        StringBuilder sb = new StringBuilder(4096);
        Exception currEx;
        String msg;

        t = th;
        while (t != null) {
            if (t instanceof Exception) {
                currEx = (Exception) t;
                sb.append(String.format("Exception: %s:%s\n", currEx.getMessage(), currEx.getClass().getName()));
                for (StackTraceElement se : currEx.getStackTrace()) {
                    sb.append(String.format("%s\n", se.toString()));
                }
                sb.append("\n");

                t = t.getCause();
            }
        }
        return sb.toString();
    }

    public static <T> String joinString(Collection<T> objects, String delim) {
        String newString = "";
        for (Object string : objects) {
            newString += string.toString() + delim;
        }

        if (!newString.equals("")) {
            newString = newString.substring(0, newString.length() - delim.length());
        }
        return newString;
    }

    public static byte[] asciiBytes(String asciiStr) {
        byte[] out = null;
        try {
            out = asciiStr.getBytes("US-ASCII");
            return out;
        } catch (UnsupportedEncodingException ex) {
            // Impossable Exception as Java Spec says all Implementations will
            // support US-ASCII. But incase the impossible does happen
            // return null
            return out;
        }
    }
}
