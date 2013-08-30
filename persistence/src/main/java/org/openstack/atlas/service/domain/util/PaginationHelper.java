package org.openstack.atlas.service.domain.util;

import org.openstack.atlas.service.domain.pojos.CustomQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

public class PaginationHelper {
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    @Transactional
    public Query addQueryParameters(CustomQuery customQuery, Integer offset, Integer marker, Integer limit) {
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
