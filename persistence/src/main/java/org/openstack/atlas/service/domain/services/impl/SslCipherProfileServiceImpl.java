package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.lang.StringUtils;
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
                return sslTerm.getCipherProfile();
            }
        }
        return null;
    }

    @Override
    public void setCipherProfileOnSslTermination(SslTermination sslTermination, String profileName) {
        SslCipherProfile profile = sslCipherProfileRepository.getByName(profileName);
        sslTermination.setCipherProfile(profile);
        if(profile != null) {
            sslTermination.setCipherList(profile.getCiphers());
        } else {
            sslTermination.setCipherList(StringUtils.EMPTY);
        }
    }

    @Override
    public boolean isCipherProfileExists(String profileName) {
        SslCipherProfile profile = sslCipherProfileRepository.getByName(profileName);
        if(profile != null){
            return true;
        }
        return false;
    }

}
