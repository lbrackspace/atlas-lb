package org.openstack.atlas.api.filters.wrappers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class HeadersRequestWrapper extends HttpServletRequestWrapper {

    private Map<String, List<String>> headerMap;
    private Set<String> overriddenHeaders;

    public HeadersRequestWrapper(HttpServletRequest request) {
        super(request);
        headerMap = new HashMap<String, List<String>>();
        overriddenHeaders = new HashSet<String>();
    }

    public boolean overideHeader(String headerName) {
        return overriddenHeaders.add(headerName);
    }

    public boolean unoverideHeader(String headerName) {
        return overriddenHeaders.remove(headerName);
    }

    public void addHeader(String headerName, String value) {
        if (headerMap.containsKey(headerName)) {
            headerMap.get(headerName).add(value);
        } else {
            List<String> values = new ArrayList<String>();
            values.add(value);
            headerMap.put(headerName, values);
        }
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        HttpServletRequest request = (HttpServletRequest) this.getRequest();

        Set<String> allHeaderNames = new HashSet<String>();

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            allHeaderNames.add(headerNames.nextElement());
        }

        for (String headerName : headerMap.keySet()) {
            allHeaderNames.add(headerName);
        }

        return Collections.enumeration(allHeaderNames);
    }

    @Override
    public String getHeader(String name) {
        List<String> valuestrings;

        if ((valuestrings = headerMap.get("" + name)) != null) {
            return valuestrings.get(0).split(",")[0];
        } else {
            if (overriddenHeaders.contains(name)) {
                return null; // This header was overidden so don't attempt to fetch the original header
            } else {
                return ((HttpServletRequest) getRequest()).getHeader(name);
            }
        }
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        Enumeration<String> parentHeaders = super.getHeaders(name);
        List<String> values = new ArrayList<String>();

        if (!overriddenHeaders.contains(name)) {
            // this header was overidden so we are ignoreing the original header with this name
            while (parentHeaders.hasMoreElements()) {
                values.add(parentHeaders.nextElement());
            }
        }

        if (headerMap.containsKey(name)) {
            List<String> valueList = headerMap.get(name);

            for (String s : valueList) {
                String[] splitValues = s.split(",");
                Collections.addAll(values, splitValues);
            }
        }

        return Collections.enumeration(values);
    }

    public Map<String, List<String>> getHeaderMap() {
        return headerMap;
    }

    public void setHeaderMap(Map<String, List<String>> headerMap) {
        this.headerMap = headerMap;
    }

    public Set<String> getOverriddenHeaders() {
        return overriddenHeaders;
    }

    public void setOverriddenHeaders(Set<String> overridenHeaders) {
        this.overriddenHeaders = overridenHeaders;
    }
}
