package org.openstack.atlas.service.domain.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
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
public class SslCipherProfileRepository {
    final Log LOG = LogFactory.getLog(SslCipherProfileRepository.class);

    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public SslCipherProfile getById(Integer id) throws EntityNotFoundException {
        SslCipherProfile profile = entityManager.find(SslCipherProfile.class, id);
        if (profile == null) {
            String errMsg = String.format("Cannot find the profile with id=%d", id);
            LOG.warn(errMsg);
            throw new EntityNotFoundException(errMsg);
        }
        return profile;
    }

    public SslCipherProfile getByName(String name) {
        List<SslCipherProfile> sslCipherProfiles = entityManager.createQuery("SELECT s FROM SslCipherProfile s where lower(s.name) = :name")
                .setParameter("name", name.toLowerCase()).getResultList();
        if (sslCipherProfiles != null && !sslCipherProfiles.isEmpty()) {
            return sslCipherProfiles.get(0);
        }
        return null;
    }

    public List<SslCipherProfile> getMatchingByName(String name) throws EntityNotFoundException {
        List<SslCipherProfile> sslCipherProfiles = entityManager.createQuery("SELECT s FROM SslCipherProfile s where lower(s.name) like :name")
                .setParameter("name", '%'+name.toLowerCase()+'%').getResultList();
        return sslCipherProfiles;
    }

    public List<SslCipherProfile> fetchAllProfiles() throws EntityNotFoundException {
        List<SslCipherProfile> sslCipherProfiles = entityManager.createQuery("SELECT s FROM SslCipherProfile").getResultList();
        return sslCipherProfiles;
    }

}
