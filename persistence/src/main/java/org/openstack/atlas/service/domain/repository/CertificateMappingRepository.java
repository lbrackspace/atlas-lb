package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.crypto.tls.Certificate;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.util.Constants;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@org.springframework.stereotype.Repository
@Transactional
public class CertificateMappingRepository {
    final Log LOG = LogFactory.getLog(CertificateMappingRepository.class);

    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public CertificateMapping getByIdAndLoadBalancerId(Integer id, Integer lbId) throws EntityNotFoundException {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<CertificateMapping> criteria = builder.createQuery(CertificateMapping.class);
        Root<CertificateMapping> certificateMappingRoot = criteria.from(CertificateMapping.class);
        LoadBalancer lb = new LoadBalancer();
        lb.setId(lbId);

        Predicate hasId = builder.equal(certificateMappingRoot.get(CertificateMapping_.id), id);
        Predicate hasLoadBalancerId = builder.equal(certificateMappingRoot.get(CertificateMapping_.loadbalancer), lb);

        criteria.select(certificateMappingRoot);
        criteria.where(builder.and(hasId, hasLoadBalancerId));

        List<CertificateMapping> resultList = entityManager.createQuery(criteria).getResultList();
        if (resultList.isEmpty()) {
            throw new EntityNotFoundException(Constants.CertificateMappingNotFound);
        } else {
            return resultList.get(0);
        }
    }

    public List<CertificateMapping> getAllForLoadBalancerId(Integer lbId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<CertificateMapping> criteria = builder.createQuery(CertificateMapping.class);
        Root<CertificateMapping> certificateMappingRoot = criteria.from(CertificateMapping.class);
        LoadBalancer lb = new LoadBalancer();
        lb.setId(lbId);

        Predicate hasLoadBalancerId = builder.equal(certificateMappingRoot.get(CertificateMapping_.loadbalancer), lb);

        criteria.select(certificateMappingRoot);
        criteria.where(hasLoadBalancerId);

        return entityManager.createQuery(criteria).getResultList();
    }

    public CertificateMapping save(CertificateMapping certificateMapping, int lbId) {
        LoadBalancer lb = new LoadBalancer();
        lb.setId(lbId);
        certificateMapping.setLoadbalancer(lb);
        return entityManager.merge(certificateMapping);
    }

    public LoadBalancer update(LoadBalancer loadBalancer) {
        final Set<LoadBalancerJoinVip> lbJoinVipsToLink = loadBalancer.getLoadBalancerJoinVipSet();
        loadBalancer.setLoadBalancerJoinVipSet(null);

        loadBalancer.setUpdated(Calendar.getInstance());
        loadBalancer = entityManager.merge(loadBalancer);

        // Now attach loadbalancer to vips
        for (LoadBalancerJoinVip lbJoinVipToLink : lbJoinVipsToLink) {
            VirtualIp virtualIp = entityManager.find(VirtualIp.class, lbJoinVipToLink.getVirtualIp().getId());
            LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip(loadBalancer.getPort(), loadBalancer, virtualIp);
            entityManager.merge(loadBalancerJoinVip);
            entityManager.merge(lbJoinVipToLink.getVirtualIp());
        }

        entityManager.flush();
        return loadBalancer;
    }

    public void delete(LoadBalancer loadBalancer, Integer id) throws EntityNotFoundException {
        Set<CertificateMapping> dbCertificateMappings = new HashSet<CertificateMapping>(loadBalancer.getCertificateMappings());
        Boolean removed = false;

        for (CertificateMapping certificateMapping : dbCertificateMappings) {
            Integer certificateMappingId = certificateMapping.getId();
            if (certificateMappingId.equals(id)) {
                loadBalancer.getCertificateMappings().remove(certificateMapping);
                removed = true;
            }
        }

        if (!removed) {
            String message = Constants.CertificateMappingNotFound;
            LOG.warn(message);
            throw new EntityNotFoundException(message);
        }

        loadBalancer.setUpdated(Calendar.getInstance());
        entityManager.merge(loadBalancer);
        entityManager.flush();
    }
}
