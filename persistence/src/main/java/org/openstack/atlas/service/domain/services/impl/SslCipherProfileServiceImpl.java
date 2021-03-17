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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class SslCipherProfileServiceImpl extends BaseService implements SslCipherProfileService {
    private final Log LOG = LogFactory.getLog(SslCipherProfileServiceImpl.class);

    @Override
    public SslCipherProfile getById(Integer id) throws EntityNotFoundException {
        SslCipherProfile profile = sslCipherProfileRepository.getById(id);
        if(profile != null){
            return  profile;
        }else{
            throw new EntityNotFoundException(String.format("There is no such cipher profile available with ID " + id));
        }
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

    @Transactional
    public void deleteSslCipherProfile(SslCipherProfile sslCipherProfile) throws EntityNotFoundException {

        SslCipherProfile dbSslCipherProfile = sslCipherProfileRepository.getById(sslCipherProfile.getId());
        sslCipherProfileRepository.delete(dbSslCipherProfile);

    }

    public List<SslCipherProfile> fetchAllProfiles() throws EntityNotFoundException {
        List<SslCipherProfile> sslCipherProfiles = sslCipherProfileRepository.fetchAllProfiles();
        if(sslCipherProfiles.size() < 1){
            throw new EntityNotFoundException(String.format("There are no cipher profiles available"));
        }
        return sslCipherProfiles;
}

    @Override
    public SslCipherProfile getByName(String profileName) throws EntityNotFoundException {
        SslCipherProfile profile = sslCipherProfileRepository.getByName(profileName);
        if(profile != null){
            return profile;
        }else{
            throw new EntityNotFoundException(String.format("There is no cipher profile available with the name " + profileName));
        }

    }
}
