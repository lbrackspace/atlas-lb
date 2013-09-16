package org.openstack.atlas.service.domain.util;

import org.openstack.atlas.service.domain.pojos.CustomQuery;
import org.openstack.atlas.service.domain.pojos.QueryParameter;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

public class PaginationHelper {

    private String value;

    public Query addPaginationParameters(EntityManager entityManager, CustomQuery customQuery, String source, Integer offset, Integer marker, Integer limit) {
        String cqString;
        if (marker != null) {
            customQuery.addParam(source + ".id", ">=", "marker", marker);
        }
        cqString = customQuery.getQueryString();

        Query query = entityManager.createQuery(cqString);
        for (QueryParameter param : customQuery.getQueryParameters()) {
            query.setParameter(param.getPname(), param.getValue());
        }

        query.setFirstResult(offset == null ? 0 : offset);
        query.setMaxResults(limit == null || limit > 100 ? 100 : limit);
        return query;
    }
}
