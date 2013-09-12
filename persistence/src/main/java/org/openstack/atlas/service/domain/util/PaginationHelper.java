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
            setValue(source);
            customQuery.addParam("(" + source + ".id", ">=", "marker", marker);
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

    private void setValue(String source) {
        if (source.equals("lb")) {
            this.value = " or lb.id = (SELECT lb.id FROM LoadBalancer lb WHERE lb.accountId = :accountId"
                    + " AND lb.id < :marker ORDER BY lb.id DESC LIMIT 1 OFFSET 1))";
        }
    }
}
