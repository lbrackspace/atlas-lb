package org.openstack.atlas.api.helpers;

import org.openstack.atlas.api.exceptions.MalformedUrlException;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlAccountIdExtractor {
    public Integer getAccountId(String url) {
        if (StringUtils.isBlank(url)) throw new MalformedUrlException("AccountId not part of the URL");

        Matcher matcher = Pattern.compile("./([0-9]+)/(loadbalancers|rateprofiles|management)(\\s*$|.)").matcher(url);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        matcher = Pattern.compile("http://([^/]+)/([^/]+)/([0-9]+)/?").matcher(url);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(3));
        }

        throw new MalformedUrlException("AccountId not part of the URL: " + url);
    }
    
    public String getContentType(String url) {
        if (url.endsWith(".json")) return "json";
        if (url.endsWith(".xml")) return "xml";
        if (url.endsWith(".atom")) return "atom+xml";
        return "";
    }
}
