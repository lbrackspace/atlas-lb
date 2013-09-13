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

    public Query addPaginationParameters(EntityManager entityManager, CustomQuery customQuery, String source, String name, Integer offset, Integer marker, Integer limit) {
        String cqString;
        if (marker != null) {
            setValue(source, name);
            customQuery.addParam("(" + name + ".id", ">=", "marker", marker);
            cqString = customQuery.getQueryString() + value;
        } else {
            cqString = customQuery.getQueryString();
        }

        Query query = entityManager.createQuery(cqString);
        for (QueryParameter param : customQuery.getQueryParameters()) {
            query.setParameter(param.getPname(), param.getValue());
        }

        query.setFirstResult(offset == null ? 0 : offset);
        query.setMaxResults(limit == null || limit > 100 ? 100 : limit);
        return query;
    }

    private void setValue(String source, String name) {
        this.value = String.format(" or %s.id = (SELECT %s.id FROM %s %s WHERE %s.accountId = :accountId"
                + " AND %s.id < :marker ORDER BY %s.id DESC LIMIT 1 OFFSET 1))",
                name, name, source, name, name, name, name);
    }
}
