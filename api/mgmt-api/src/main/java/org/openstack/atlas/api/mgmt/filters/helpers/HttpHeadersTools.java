package org.openstack.atlas.api.mgmt.filters.helpers;

import org.openstack.atlas.api.mgmt.helpers.LDAPTools.Base64Coder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpHeadersTools {

    private HttpServletRequest req;
    private HttpServletResponse res;
    private String authHeader;
    private static final Pattern basicRe = Pattern.compile("BASIC\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern userRe = Pattern.compile("(\\S+):(\\S+)");
    private static final String AUTHORIZATION = "Authorization";

    public HttpHeadersTools() {
    }

    public HttpHeadersTools(HttpServletRequest req, HttpServletResponse res) {
        this.req = req;
        this.res = res;
        this.authHeader = req.getHeader(AUTHORIZATION);
    }

    public boolean isValidAuth() {
        if (getBasicString() == null || getBasicUser() == null || getBasicPassword() == null) {
            return false;
        }
        return true;
    }

    public boolean isBasicAuth() {
        Matcher m;
        if (authHeader == null) {
            return false;
        }
        m = basicRe.matcher(authHeader);
        if (!m.find()) {
            return false;
        }
        return true;
    }

    public HttpServletRequest getReq() {
        return req;
    }

    public void setReq(HttpServletRequest req) {
        this.req = req;
    }

    public HttpServletResponse getRes() {
        return res;
    }

    public void setRes(HttpServletResponse res) {
        this.res = res;
    }

    public String getBasicString() {
        String out;
        if (authHeader == null) {
            return null;
        }
        Matcher m = basicRe.matcher(authHeader);
        if (!m.find()) {
            return null;
        }
        try {
            out = Base64Coder.decodeString(m.group(1));
        } catch (IllegalArgumentException e) {
            return null;
        }
        return out;
    }

    public String getBasicUser() {
        String basicString = getBasicString();
        if (basicString == null) {
            return null;
        }
        Matcher m = userRe.matcher(basicString);
        if (!m.find()) {
            return null;
        }
        return m.group(1);
    }

    public String getBasicPassword() {
        String basicString = getBasicString();
        if (basicString == null) {
            return null;
        }
        Matcher m = userRe.matcher(basicString);
        if (!m.find()) {
            return null;
        }
        return m.group(2);
    }

    public boolean isHeaderTrue(String name) {
        boolean out = false;
        if (req.getHeader(name) == null) {
            return out;
        }
        if (req.getHeader(name).equalsIgnoreCase("true")) {
            out = true;
        }
        return out;
    }

    public static Set<String> commastr2set(String valueStr) {
        Set<String> out = new HashSet<String>();
        String[] vals = valueStr.split(",");
        for (String val : vals) {
            out.add(val);
        }
        return out;
    }

    public static String set2commastr(Set<String> values) {
        int i;
        List<String> valList = new ArrayList<String>(values);
        Collections.sort(valList);
        Object[] objAry = valList.toArray();
        StringBuilder sb = new StringBuilder();
        if (objAry.length == 0) {
            return sb.toString();
        }
        i = 1;
        for (i = 0; i < objAry.length - 1; i++) {
            sb.append(String.format("%s,", (String) objAry[i]));
        }
        sb.append(objAry[i]);
        return sb.toString();
    }
}
