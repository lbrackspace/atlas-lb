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

    public SslCipherProfile getCipherProfileForLoadBalancerId(Integer lbId) throws EntityNotFoundException {
        return null;
    }

    public SslCipherProfile getCipherProfileForSslTerminationId(Integer sslTermId) throws EntityNotFoundException {
        return null;
    }

    public void removeCipherProfileFromSslTermination(SslTermination sslTermination, Integer profileId) throws EntityNotFoundException {

    }

    public void deleteCipherProfile(SslCipherProfile sslCipherProfile) throws EntityNotFoundException {
        /* Added the skeleton for future use for management api to delete a cipher profile. */
    }

    public void updateCipherProfile(SslCipherProfile sslCipherProfile) throws EntityNotFoundException, UnprocessableEntityException, BadRequestException, ImmutableEntityException {
        /* Added the skeleton for future use for management api to update a cipher profile. */
    }
    public void saveCipherProfile(SslCipherProfile sslCipherProfile) {
        /* Added the skeleton for future use for management api to create a cipher profile. */
    }
}
