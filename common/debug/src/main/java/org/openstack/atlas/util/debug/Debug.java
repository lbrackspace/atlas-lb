package org.openstack.atlas.util.debug;

import java.io.File;
import java.net.URI;
import java.net.URL;

public class Debug {

    private static final int PAGESIZE = 4096;

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
            String st = getEST(ex);
            return st;
        }
    }

    public static String findClassPath(String className) throws ClassNotFoundException {
        Class classIn = Class.forName(className);
        return findClassPath(classIn);
    }

    public static String printClassPaths(String... classNames) {
        int i = 0;
        int length = classNames.length;
        StringBuilderWriter sbw = new StringBuilderWriter(PAGESIZE);
        for (i = 0; i < length; i++) {
            String className = classNames[i];
            String classPath = "";
            try {
                classPath = findClassPath(className);
            } catch (Exception ex) {
                classPath = String.format("Exception %s\n", getEST(ex));
            }
            sbw.printf("\"%s\" -> \"%s\"\n", className, classPath);
        }
        return sbw.toString();
    }

    public static String getExtendedStackTrace(Throwable th) {
        Throwable t;
        StringBuilder sb = new StringBuilder(PAGESIZE);
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
                sb.append("\n");
                t = t.getCause();
            }
        }
        return sb.toString();
    }

    public static String getEST(Throwable th) {
        return getExtendedStackTrace(th);
    }

    public static int nCpus() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static long freeMem() {
        return Runtime.getRuntime().freeMemory();
    }

    public static long totalMem() {
        return Runtime.getRuntime().totalMemory();
    }

    public static long usedMem() {
        return totalMem() - freeMem();
    }

    public static long maxMem() {
        return Runtime.getRuntime().maxMemory();
    }

    public static void gc() {
        Runtime.getRuntime().gc();
    }

    //For jython since
    public static String getClassName(Object obj){
        if(obj instanceof Class){
            Class c = (Class)obj;
            return c.getName();
        }else{
            return obj.getClass().getName();
        }
    }

    public static String getProgName(Class inClass) {
        int li;
        String sep;
        String path;
        String prog;
        URI uri;
        File file;

        try {
            uri = inClass.getProtectionDomain().
                    getCodeSource().
                    getLocation().
                    toURI();
            file = new File(uri);
            path = file.getAbsolutePath();
            sep = File.separator;
            li = path.lastIndexOf(sep) + 1;
            prog = path.substring(li, path.length());
            prog = String.format("java -jar %s", prog);
        } catch (Exception ex) {
            prog = "prog";
        }
        return prog;
    }
}
