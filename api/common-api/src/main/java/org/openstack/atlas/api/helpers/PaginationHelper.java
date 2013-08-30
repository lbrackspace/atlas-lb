package org.openstack.atlas.api.helpers;

import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.service.domain.pojos.CustomQuery;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.w3.atom.Link;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

public class PaginationHelper {
    protected static RestApiConfiguration restApiConfiguration;
    final protected static Integer MIN_PAGE_LIMIT = 1;
    final protected static Integer MAX_PAGE_LIMIT = 1000;
    final protected static Integer DEFAULT_PAGE_LIMIT = 500;
    final protected static Integer MIN_PAGE_OFFSET = 0;
    final protected static Integer DEFAULT_PAGE_OFFSET = 0;
    final public static String NEXT = "next";
    final public static String PREVIOUS = "previous";

    @PersistenceContext(unitName = "loadbalancing")
    private static EntityManager entityManager;

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

    @Transactional
    public static Query checkParameters(CustomQuery customQuery, Integer offset, Integer marker, Integer limit) {
        if (offset != null) {
            customQuery.setOffset(offset);
        }
        if (limit != null) {
            customQuery.setLimit(limit);
        }
        customQuery.addParam("id", ">=", "marker", marker != null ? marker : 0);
        return entityManager.createQuery(customQuery.getQueryString());
    }
}
