package org.openstack.atlas.api.filters.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MediaType {

    private String type;
    private String subtype;
    private List<String> parameters = new ArrayList<String>();
    private static final Pattern mtRe = Pattern.compile("^([^/]*)/(.*)$");

    public MediaType() {
    }

    public static MediaType newInstance(String headerStr) {
        MediaType out = null;
        String[] vals;
        int i;

        if (headerStr == null) {
            return out;
        }
        vals = headerStr.split(";");
        Matcher matcher = mtRe.matcher(vals[0].trim());
        if (!matcher.find()) {
            return out;
        }
        out = new MediaType();
        out.setType(matcher.group(1));
        out.setSubtype(matcher.group(2));
        for (i = 1; i < vals.length; i++) {
            out.getParameters().add(vals[i].trim());
        }
        return out;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public static boolean matches(MediaType t, MediaType o) {
        if (t == null || o == null || t.getType() == null
                || t.getSubtype() == null || o.getType() == null
                || o.getSubtype() == null) {
            return false;
        }
        if (!t.getType().equalsIgnoreCase(o.getType())) {
            return false;
        }
        if (!t.getSubtype().equalsIgnoreCase(o.getSubtype())) {
            return false;
        }
        return true;
    }
}
