package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.repository.SslCipherProfileRepository;
import org.openstack.atlas.service.domain.repository.SslTerminationRepository;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.SslCipherProfileService;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SslCipherProfileServiceImpl extends BaseService implements SslCipherProfileService {
    private final Log LOG = LogFactory.getLog(SslCipherProfileServiceImpl.class);
    private static final ZeusUtils zeusUtils;

    @Autowired
    private LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;
    @Autowired
    private AccountLimitServiceImpl accountLimitService;

    static {
        zeusUtils = new ZeusUtils();
    }

    @Override
    public SslCipherProfile getById(Integer id) throws EntityNotFoundException {
        return sslCipherProfileRepository.getById(id);
    }

    @Override
    public SslCipherProfile getCipherProfileByLoadBalancerId(Integer lbId) throws EntityNotFoundException {
        LoadBalancer dbLb = loadBalancerRepository.getById(lbId);
        if(dbLb != null) {
            SslTermination sslTerm = dbLb.getSslTermination();
            if(sslTerm != null){
                return sslTerm.getSslCipherProfile();
            }
        }
        return null;
    }

    @Override
    public SslCipherProfile getCipherProfileForSSLTerminationId(Integer SSLTermId) throws EntityNotFoundException {
        return null;
    }

    @Override
    public void setCipherProfileToSslTermination(SslTermination sslTermination, String profileName) {
        SslCipherProfile profile = sslCipherProfileRepository.getByName(profileName);
        sslTermination.setSslCipherProfile(profile);
    }

    @Override
    public boolean isCipherProfileExists(String profileName) {
        SslCipherProfile profile = sslCipherProfileRepository.getByName(profileName);
        if(profile != null){
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void createCipherProfile(SslCipherProfile profile) {
    }

    @Override
    public void updateCipherProfile(SslTermination sslTermination) throws EntityNotFoundException, UnprocessableEntityException, BadRequestException, ImmutableEntityException {

    }

    @Override
    public void removeCipherProfile(SslTermination sslTermination) throws EntityNotFoundException {

    }

}
