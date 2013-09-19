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
        Integer forceLimit = 100;
        String cqString;
        if (marker != null) {
            customQuery.addParam(source + ".id", ">=", "marker", marker);
        }
        cqString = customQuery.getQueryString();

        Query query = entityManager.createQuery(cqString);
        for (QueryParameter param : customQuery.getQueryParameters()) {
            query.setParameter(param.getPname(), param.getValue());
        }

        if (offset != null) {
            query.setFirstResult(offset);
        }

        if (limit != null && limit <= 100) {
            forceLimit = limit;
        }
        query.setMaxResults(forceLimit);

        return query;
    }
}
