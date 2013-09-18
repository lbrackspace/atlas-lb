package org.openstack.atlas.api.helpers;

import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.springframework.beans.factory.annotation.Required;
import org.w3.atom.Link;

import java.util.ArrayList;
import java.util.List;

public class PaginationHelper {
    protected static RestApiConfiguration restApiConfiguration;
    final protected static Integer MIN_PAGE_LIMIT = 1;
    final protected static Integer MAX_PAGE_LIMIT = 1000;
    final protected static Integer DEFAULT_PAGE_LIMIT = 500;
    final protected static Integer MIN_PAGE_OFFSET = 0;
    final protected static Integer DEFAULT_PAGE_OFFSET = 0;
    final public static String NEXT = "next";
    final public static String PREVIOUS = "previous";
    final public static String SELF = "self";

    @Required
    public void setRestApiConfiguration(RestApiConfiguration restApiConfiguration) {
        PaginationHelper.restApiConfiguration = restApiConfiguration;
    }

    public static Integer determinePageOffset(Integer offset) {
        if (offset == null) return DEFAULT_PAGE_OFFSET;
        else if (offset < MIN_PAGE_OFFSET) return MIN_PAGE_OFFSET;
        else return offset;
    }

    public static Integer determinePageLimit(Integer limit) {
        if (limit == null) return DEFAULT_PAGE_LIMIT;
        else if (limit < MIN_PAGE_LIMIT) return MIN_PAGE_LIMIT;
        else if (limit > MAX_PAGE_LIMIT) return MAX_PAGE_LIMIT;
        else return limit;
    }

    public static Link createLink(String rel, String relativeUri) {
        Link link = new Link();
        link.setHref(restApiConfiguration.getString(PublicApiServiceConfigurationKeys.base_uri) + relativeUri);
        link.setRel(rel);
        return link;
    }

    public static Integer calculatePreviousOffset(Integer offset, Integer limit) {
        limit = determinePageLimit(limit);
        offset = determinePageOffset(offset);
        if(offset > limit) return offset - limit;
        return 0;
    }

    public static Integer calculateNextOffset(Integer offset, Integer limit) {
        limit = determinePageLimit(limit);
        offset = determinePageOffset(offset);
        return offset + limit;
    }

    public static List<Link> provideLinks(String relativeUri, Integer marker, Integer offset, Integer limit, Integer nextMarker, Integer listSize) {
        if (marker == null) {
            return noMarkerLinks(relativeUri, offset, limit, listSize);
        }
        List<Link> links = new ArrayList<Link>();
        Link selfLink = makeLink(SELF, relativeUri, marker, offset, limit);
        if (listSize.equals(limit)) {
            Link nextLink = makeLink(NEXT, relativeUri, nextMarker, 0, limit);
            links.add(nextLink);
        }
        links.add(selfLink);

        return links;
    }

    private static List<Link> noMarkerLinks(String relativeUri, Integer offset, Integer limit, Integer listSize) {
        List<Link> links = new ArrayList<Link>();

        if (limit == null) {
            limit = 100;
        }

        if (offset != null) {
            Link prevLink = makeLink(PREVIOUS, relativeUri, null, offset - limit, limit);
            links.add(prevLink);
        }

        Link selfLink = makeLink(SELF, relativeUri, null, offset, limit);
        links.add(selfLink);

        if (listSize.equals(limit)) {
            Link nextLink;
            if (offset != null) {
                nextLink = makeLink(NEXT, relativeUri, null, offset + limit, limit);
            } else {
                nextLink = makeLink(NEXT, relativeUri, null, limit, limit);
            }
            links.add(nextLink);
        }

        return links;
    }

    private static Link makeLink(String rel, String relativeUri, Integer marker, Integer offset, Integer limit) {
        Link link = new Link();
        String href = restApiConfiguration.getString(PublicApiServiceConfigurationKeys.base_uri) + relativeUri;
        if (marker != null && marker > 0) {
            if (relativeUri.contains("?")) {
                href += String.format("&marker=%d", marker);
            } else {
                href += String.format("?marker=%d", marker);
            }
        }
        if (offset != null && offset > 0) {
            href += String.format("&offset=%d", offset);
        }
        if (limit != null && limit > 0) {
            href += String.format("&limit=%d", limit);
        }
        href = href.replace("?&", "?");
        link.setHref(href);
        link.setRel(rel);
        return link;
    }
}
