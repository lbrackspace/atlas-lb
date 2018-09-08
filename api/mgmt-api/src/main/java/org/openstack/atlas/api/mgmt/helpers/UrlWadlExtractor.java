package org.openstack.atlas.api.mgmt.helpers;

import org.openstack.atlas.api.exceptions.MalformedUrlException;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlWadlExtractor {
    public boolean authForWadl(String url) {
        String[] splitString = url.split("/v");
        return splitString[1].matches("1.0/?_wadl") || splitString[1].matches("1.0?_wadl");
    }
    public boolean isApplicationWadl(String url) {
        if (StringUtils.isBlank(url)) throw new MalformedUrlException("AccountId not part of the URL");
        Matcher matcher = Pattern.compile("http://127.0.0.1:8080/dpub/v1.0/application.wadl").matcher(url);

        return matcher.find();
    }
}
