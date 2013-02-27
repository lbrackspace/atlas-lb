
package org.openstack.atlas.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.exception.SchedulingException;


public class Debug {

    private static final String[] binBaseNames = new String[]{"B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB", "ZiB", "YiB"};

    public static double getEpochSeconds() {
        double millis = (double) System.currentTimeMillis();
        return millis * 0.001;
    }
    private static final String[] monthMap = new String[]{"",
        "Jan", "Feb", "Mar",
        "Apr", "May", "Jun",
        "Jul", "Aug", "Sep",
        "Oct", "Nov", "Dec"
    };
    // Silly but simple enough for measuring performance with long counters

    public static long nowMillis() {
        return System.currentTimeMillis();
    }

    // Got tired of respelling it.
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

    public static String classLoaderInfo(String className) throws ClassNotFoundException {
        Class classIn = Class.forName(className);
        return classLoaderInfo(classIn);
    }

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

    public static String findClassPath(String className) throws ClassNotFoundException {
        Class classIn = Class.forName(className);
        return findClassPath(classIn);
    }

    public static String findClassPath(String className, ClassLoader classLoader) throws ClassNotFoundException {
        Class classIn = Class.forName(className, true, classLoader);
        return findClassPath(classIn, classLoader);
    }

    public static String findClassPath(Class<?> cls, ClassLoader classLoader) {
        try {
            String className = cls.getName();
            String mangledName = "/" + className.replace(".", "/") + ".class";
            URL loc = cls.getResource(mangledName);
            String classPath = loc.getPath();
            return classPath;
        } catch (Exception ex) {
            String st = StaticStringUtils.getExtendedStackTrace(ex);
            return st;
        }
    }

    public static String findClassPath(Class<?> cls) {
        try {
            String className = cls.getName();
            String mangledName = "/" + className.replace(".", "/") + ".class";
            URL loc = cls.getResource(mangledName);
            String classPath = loc.getPath();
            return classPath;
        } catch (Exception ex) {
            String st = StaticStringUtils.getExtendedStackTrace(ex);
            return st;
        }
    }

    public static String showMem() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  MaxMemory: %s\n", StaticStringUtils.lpadLong(maxMem(), " ", 20)));
        sb.append(String.format("TotalMemory: %s\n", StaticStringUtils.lpadLong(totalMem(), " ", 20)));
        sb.append(String.format(" UsedMemory: %s\n", StaticStringUtils.lpadLong(usedMem(), " ", 20)));
        sb.append(String.format(" FreeMemory: %s\n", StaticStringUtils.lpadLong(freeMem(), " ", 20)));
        return sb.toString();
    }

    public static URL getUrlFromJarName(String jarPathIn) throws MalformedURLException {
        String jarFileName = StaticFileUtils.expandUser(jarPathIn);
        File jarFile = new File(jarFileName);
        URL jarURL = new URL("jar", "", "file:" + jarFile.getAbsolutePath() + "!/");
        return jarURL;
    }

    public static String exec(String cmd) throws IOException {
        StringBuilder sb = new StringBuilder();
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader pStdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = pStdout.readLine();
        while (line != null) {
            sb.append(line).append("\n");
            line = pStdout.readLine();
        }
        return sb.toString();
    }

    public static String humanReadableBytes(double val) {
        if (Double.isNaN(val)) {
            return "nan";
        }
        if (Double.isInfinite(val)) {
            return "inf";
        }
        return humanReadableBytes(BigDecimal.valueOf(val).toBigInteger());
    }

    public static String humanReadableBytes(long val) {
        return humanReadableBytes(BigInteger.valueOf(val));
    }

    public static String humanReadableBytes(String val) {
        return humanReadableBytes(new BigInteger(val));
    }

    public static String humanReadableBytes(BigInteger val) {
        int bits = val.bitLength();
        int base1024 = bits / 10;
        int lShift = 0;

        String baseName;
        if (base1024 >= 8) {
            lShift = 80;
            baseName = binBaseNames[8];
        } else {
            lShift = base1024 * 10;
            baseName = binBaseNames[base1024];
        }
        double dVal = val.doubleValue() / BigInteger.ONE.shiftLeft(lShift).doubleValue();
        return String.format("%.2f %s", dVal, baseName);
    }

    public static String hostName() {
        String host;
        try {
            return java.net.InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException ex) {
            return null;
        }
    }



    // Cause Jython None and Null are different objects
    public static Object getNull() {
        return null;
    }

    public static String itoa(BigInteger n, int radix) {
        return n.toString(radix);
    }

    public static String itoa(long l, int radix) {
        return BigInteger.valueOf(l).toString(radix);
    }

    public static String itoa(int l, int radix) {
        return BigInteger.valueOf(l).toString(radix);
    }

    public static void schedulingExceptionThrowable(boolean shouldThrow) throws SchedulingException{
        if(shouldThrow){
            throw new SchedulingException();
        }
    }

    public static void executionExceptionThrowable(boolean shouldThrow) throws ExecutionException {
        if(shouldThrow){
            throw new ExecutionException();
        }
    }
}
