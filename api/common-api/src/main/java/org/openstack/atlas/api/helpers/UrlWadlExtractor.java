package org.openstack.atlas.api.helpers;

public class UrlWadlExtractor {
    public String authForWadl(String url) {
        String[] splitString = url.split("/application.wadl");
        return splitString[0];
    }
}
