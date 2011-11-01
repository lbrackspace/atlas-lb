package org.openstack.atlas.api.validation.context;

public enum HttpRequestType {

    POST,
    PUT;

    public String value() {
        return name();
    }

    public static HttpRequestType fromValue(String v) {
        return valueOf(v);
    }
}
