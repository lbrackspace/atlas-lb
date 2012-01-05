package org.openstack.atlas.rax.domain.repository.impl;

import org.openstack.atlas.rax.domain.common.RaxConstants;
import org.openstack.atlas.rax.domain.common.RaxErrorMessages;
import org.openstack.atlas.rax.domain.entity.RaxDefaults;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.rax.domain.entity.RaxUserPages;
import org.openstack.atlas.rax.domain.repository.RaxUserPagesRepository;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Primary
@Repository
@Transactional
public class RaxUserPagesRepositoryImpl implements RaxUserPagesRepository {

    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;

    public RaxUserPages getUserPagesByAccountIdLoadBalancerId(Integer aid, Integer lid) {
        String queryString = "FROM RaxUserPages u where u.loadbalancer.id = :lid and u.loadbalancer.accountId = :aid";
        Query q = entityManager.createQuery(queryString).setParameter("lid", lid).setParameter("aid", aid);
        List<RaxUserPages> userPagesList = q.setMaxResults(1).getResultList();
        RaxUserPages userPages;
        if (userPagesList.size() <= 0) {
            userPages = null;
        } else {
            userPages = userPagesList.get(0);
        }
        return userPages;
    }

    public RaxDefaults getDefaultErrorPage() throws EntityNotFoundException {
        RaxDefaults raxDefaults;
        String qStr = "FROM RaxDefaults d WHERE d.name = :globalError";
        Query q = entityManager.createQuery(qStr).setParameter("globalError", RaxConstants.DEFAULT_ERROR_PAGE);
        List<RaxDefaults> defaultsList = q.setMaxResults(1).getResultList();
        if (defaultsList.size() <= 0) {
            throw new EntityNotFoundException("The default error page could not be located.");
        } else {
            raxDefaults = defaultsList.get(0);
        }
        return raxDefaults;
    }

    public String getErrorPageByAccountIdLoadBalancerId(Integer aid, Integer lid) throws EntityNotFoundException {
        RaxUserPages up;
        up = getUserPagesByAccountIdLoadBalancerId(aid, lid);
        if (up == null) {
            return null;
        }
        return up.getErrorpage();
    }

    public boolean setErrorPage(Integer aid, Integer lid, String errorpage) throws EntityNotFoundException {
        RaxLoadBalancer lb = (RaxLoadBalancer) loadBalancerRepository.getByIdAndAccountId(lid, aid);
        RaxUserPages up = getUserPagesByAccountIdLoadBalancerId(lid, aid);
        if (up == null) {
            up = new RaxUserPages();
            up.setLoadbalancer(lb);
            up.setErrorpage(errorpage);
            entityManager.merge(up);
            return true;
        } else {
            up.setErrorpage(errorpage);
            entityManager.merge(up);
            return false;
        }
    }

    public boolean setDefaultErrorPage(String errorpage) throws EntityNotFoundException {
        RaxDefaults up = getDefaultErrorPage();
        if (up == null) {
            up = new RaxDefaults();
            up.setName(RaxConstants.DEFAULT_ERROR_PAGE);
            up.setValue(errorpage);
            entityManager.merge(up);
            return true;
        } else {
            up.setValue(errorpage);
            entityManager.merge(up);
            return false;
        }
    }

    public boolean deleteErrorPage(Integer aid, Integer lid) throws EntityNotFoundException {
        RaxUserPages up = getUserPagesByAccountIdLoadBalancerId(aid, lid);
        if (up == null) {
            throw new EntityNotFoundException(RaxErrorMessages.ERROR_PAGES_NOT_FOUND.getMessage(lid));
        } else if (up.getErrorpage() == null) {
            throw new EntityNotFoundException(RaxErrorMessages.ERROR_PAGES_NOT_FOUND.getMessage(lid));
        }
        up.setErrorpage(null);
        entityManager.merge(up);
        return true;
    }

}
