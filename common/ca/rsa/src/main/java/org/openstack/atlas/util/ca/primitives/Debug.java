package org.openstack.atlas.util.ca.primitives;

import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.openstack.atlas.util.ca.StringUtils;

public class Debug {

    public static String classLoaderInfo(Class<?> cls) {
        ClassLoader cl = cls.getClassLoader();
        int hc = cl.hashCode();
        String cp = findClassPath(cls);
        String loaderName = cl.getClass().getName();
        String info = cl.toString();
        String fmt = "{hash=\"%d\", classLoader=\"%s\", classPath=\"%s\", info=\"%s\"}";
        String out = String.format(fmt, hc, loaderName, cp, info);
        return out;
    }

    public static String findClassPath(Class<?> cls) {
        try {
            String className = cls.getName();
            String mangledName = "/" + className.replace(".", "/") + ".class";
            URL loc = cls.getResource(mangledName);
            String classPath = loc.getPath();
            return classPath;
        } catch (Exception ex) {
            String st = StringUtils.getExtendedStackTrace(ex);
            return st;
        }
    }

    public static String getExtendedStackTrace(Throwable th) {
        Throwable t;
        StringBuilder sb = new StringBuilder();
        Throwable currThrowable;
        String msg;

        t = th;
        while (t != null) {
            if (t instanceof Throwable) {
                currThrowable = (Throwable) t;
                sb.append(String.format("\"%s\":\"%s\"\n", currThrowable.getClass().getName(), currThrowable.getMessage()));
                for (StackTraceElement se : currThrowable.getStackTrace()) {
                    sb.append(String.format("%s\n", se.toString()));
                }
                sb.append("\nCausing Exception: ");
                t = t.getCause();
            } else {
                break;
            }
        }
        return sb.toString();
    }

    public static String getEST(Throwable th) {
        return getExtendedStackTrace(th);
    }

    public static void nop() {
    }
}
