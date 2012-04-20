package org.openstack.atlas.util.ca;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import org.openstack.atlas.util.ca.primitives.RsaConst;

public class StringUtils {

    public static String displayParsedInt(String strIn) {
        try {
            int i = Integer.parseInt(strIn);
            return String.format("%d", i);
        } catch (NumberFormatException ex) {
            return "INVALID";
        }
    }

    // Coulden't find a default library function to do this. :(
    // It iterates the string character per caracher and builds a new one so
    // Its like really slow. :(
    public static String escape_html(String html) {
        StringBuilder sb = new StringBuilder(RsaConst.PAGESIZE);
        char ch;
        int len = html.length();
        int i;
        for (i = 0; i < len; i++) {
            ch = html.charAt(i);
            switch (ch) {
                case ' ':
                    sb.append("&nbsp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '\n':
                    sb.append("<br/>");
                    break;
                default:
                    sb.append(ch);
                    break;
            }
        }
        return sb.toString();
    }

    // Got tired of spelling ExtendedStackTrace
    public static String getEST(Throwable th) {
        return getExtendedStackTrace(th);
    }

    public static String getExtendedStackTrace(Throwable th) {
        Throwable t;
        StringBuilder sb = new StringBuilder(RsaConst.PAGESIZE);
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
        StringBuilder sb = new StringBuilder();
        Object[] oarray = objects.toArray();
        int nobjects = oarray.length;
        if(nobjects==0){
            return "";
        }
        for(int i=0;i<nobjects -1; i++){
            sb.append(String.format("%s%s",oarray[i].toString(),delim));
        }
        sb.append(String.format("%s",oarray[nobjects-1]));
        return sb.toString();
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
