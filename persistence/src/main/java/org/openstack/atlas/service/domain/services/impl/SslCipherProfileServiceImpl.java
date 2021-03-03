package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.SslCipherProfile;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.SslCipherProfileService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class SslCipherProfileServiceImpl extends BaseService implements SslCipherProfileService {
    private final Log LOG = LogFactory.getLog(SslCipherProfileServiceImpl.class);

    @Override
    public SslCipherProfile getById(Integer id) throws EntityNotFoundException {
        return sslCipherProfileRepository.getById(id);
    }

    @Override
    public SslCipherProfile getCipherProfileByLoadBalancerId(Integer lbId) throws EntityNotFoundException {
        LoadBalancer dbLb = loadBalancerRepository.getById(lbId);
        if (dbLb != null) {
            SslTermination sslTerm = dbLb.getSslTermination();
            if (sslTerm != null) {
                return sslTerm.getCipherProfile();
            }
        }
        return null;
    }

    @Override
    public void setCipherProfileOnSslTermination(SslTermination sslTermination, String profileName) {
        SslCipherProfile profile = sslCipherProfileRepository.getByName(profileName);
        sslTermination.setCipherProfile(profile);
        if (profile != null) {
            sslTermination.setCipherList(profile.getCiphers());
        } else {
            sslTermination.setCipherList(StringUtils.EMPTY);
        }
    }

    @Override
    public boolean isCipherProfileAvailable(String profileName) {
        SslCipherProfile profile = sslCipherProfileRepository.getByName(profileName);
        if (profile != null) {
            return true;
        }
        return false;
    }

    @Override
    public SslCipherProfile create(SslCipherProfile sslCipherProfile) throws BadRequestException {
        if (!isCipherProfileAvailable(sslCipherProfile.getName())) {
            sslCipherProfileRepository.create(sslCipherProfile);
            return sslCipherProfile;
        } else {
            throw new BadRequestException(String.format("Bad Request - profile with the same name already exists"));
        }
    }

    @Override
    @Transactional
    public SslCipherProfile update(Integer id, SslCipherProfile queueSslCipherProfile) throws BadRequestException,
            EntityNotFoundException {

        SslCipherProfile dbSslCipherProfile = getById(id);

        if (queueSslCipherProfile.getName() != null) {
            if (!isCipherProfileAvailable(queueSslCipherProfile.getName())) {
                dbSslCipherProfile.setName(queueSslCipherProfile.getName());
            } else {
                throw new BadRequestException(String.format("Bad Request - profile with the same name already exists"));
            }
        }

        if (queueSslCipherProfile.getComments() != null) {
            dbSslCipherProfile.setComments(queueSslCipherProfile.getComments());
        }

        if (queueSslCipherProfile.getCiphers() != null) {
            dbSslCipherProfile.setCiphers(queueSslCipherProfile.getCiphers());
        }

        sslCipherProfileRepository.update(dbSslCipherProfile);
        return dbSslCipherProfile;

    }
}
