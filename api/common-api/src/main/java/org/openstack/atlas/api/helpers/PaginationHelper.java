package org.openstack.atlas.api.helpers;

import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.api.config.RestApiConfiguration;
import org.w3.atom.Link;

public class PaginationHelper {
    protected static RestApiConfiguration restApiConfiguration;
    final protected static Integer MIN_PAGE_LIMIT = 1;
    final protected static Integer MAX_PAGE_LIMIT = 100;
    final protected static Integer DEFAULT_PAGE_LIMIT = 100;
    final protected static Integer MIN_PAGE_OFFSET = 0;
    final protected static Integer DEFAULT_PAGE_OFFSET = 0;

    public static Integer determinePageOffset(Integer offset) {
        if (offset == null) offset = DEFAULT_PAGE_OFFSET;
        else if (offset < MIN_PAGE_OFFSET) offset = MIN_PAGE_OFFSET;
        return offset;
    }

    public static Integer determinePageLimit(Integer limit) {
        if (limit == null) limit = DEFAULT_PAGE_LIMIT;
        else if (limit < MIN_PAGE_LIMIT) limit = MIN_PAGE_LIMIT;
        else if (limit > MAX_PAGE_LIMIT) limit = MAX_PAGE_LIMIT;
        return limit;
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
        if(offset >= limit) return offset - limit;
        return 0;
    }

    public static Integer calculateNextOffset(Integer offset, Integer limit) {
        limit = determinePageLimit(limit);
        offset = determinePageOffset(offset);
        return offset + limit;
    }
}
