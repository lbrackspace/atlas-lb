package org.openstack.atlas.service.domain.util;

import org.openstack.atlas.service.domain.pojos.CustomQuery;
import org.openstack.atlas.service.domain.pojos.QueryParameter;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

public class PaginationHelper {

    public Query addPaginationParameters(EntityManager entityManager, CustomQuery customQuery, String source, Integer offset, Integer marker, Integer limit) {
        if (marker != null) {
            customQuery.addParam(source + ".id", ">=", "marker", marker);
        }

        Query query = entityManager.createQuery(customQuery.getQueryString());
        for (QueryParameter param : customQuery.getQueryParameters()) {
            query.setParameter(param.getPname(), param.getValue());
        }

        query.setFirstResult(offset == null ? 0 : offset);
        query.setMaxResults(limit == null || limit > 100 ? 100 : limit);
        return query;
    }
}
