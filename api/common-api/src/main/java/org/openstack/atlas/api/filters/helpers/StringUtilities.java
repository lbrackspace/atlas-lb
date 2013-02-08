package org.openstack.atlas.api.filters.helpers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;

public class StringUtilities {

    public static final String HTTPS_INIT_FAIL = "Error initializing HttpsCertIgnoreClass";
    public static final String AUTH_INIT_FAIL  = "Auth has not been configured correctly. Please verify that the following are set in /etc/openstack/atlas/public-api.conf: auth_management_uri, basic_auth_user or basic_auth_key";
    public static <T> String DelimitString(Collection<T> objects, String delim) {
        String newString = "";
        for (Object string : objects) {
            newString += string.toString() + delim;
        }

        if (!newString.equals("")) {
            newString = newString.substring(0, newString.length() - delim.length());
        }
        return newString;
    }

    public static <T> String DelimitStringAndWrapEntriesWithQuotes(Collection<T> objects, String delim) {
        String newString = "";
        for (Object string : objects) {
            newString += "'" + string.toString() + "'" + delim;
        }

        if (!newString.equals("")) {
            newString = newString.substring(0, newString.length() - delim.length());
        }
        return newString;
    }

    public static void logId(String preMsg, Object obj) {
        String msg;
        String className = obj.getClass().getSimpleName();
        Log LOG = LogFactory.getLog(obj.getClass());
        String hashCode = getId(className, obj);
        msg = String.format("%s%s\n", (preMsg == null) ? "null" : preMsg, hashCode);
        LOG.info(msg);
    }

    public static String getId(String name, Object obj) {
        int hashcode;
        String hexOut;
        if (obj == null) {
            return String.format("id[%s]=null", name);
        }
        hashcode = System.identityHashCode(obj);
        hexOut = Integer.toHexString(hashcode);
        return String.format("id[%s]=%s", name, hexOut);
    }

    public static String getStackTrace(Exception ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Exception: %s:%s\n", ex.getMessage(), ex.getClass().getName()));
        for (StackTraceElement se : ex.getStackTrace()) {
            sb.append(String.format("%s\n", se.toString()));
        }
        return sb.toString();
    }

    public static String getHttpsInitExceptionString(Throwable th){
        return HTTPS_INIT_FAIL + ": " + getExtendedStackTrace(th);
    }

    public static String getExtendedStackTrace(Throwable ti) {
        Throwable t;
        StringBuilder sb;
        Exception currEx;
        String msg;

        sb = new StringBuilder();
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
}
