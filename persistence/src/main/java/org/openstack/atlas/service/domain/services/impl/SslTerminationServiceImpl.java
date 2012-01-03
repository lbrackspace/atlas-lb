package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.services.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SslTerminationServiceImpl extends BaseService implements SslTerminationService {

    @Transactional
    @Override
    public SslTermination updateSslTermination(Integer lbId, Integer accountId, SslTermination sslTermination) throws EntityNotFoundException, ImmutableEntityException {
        //TODO: validate here...
        if (sslTermination != null) {
            return sslTerminationRepository.setSslTermination(lbId, sslTermination);
        }
        return new SslTermination();
    }

    @Transactional
    @Override
    public boolean deleteSslTermination(Integer lid, Integer accountId) throws EntityNotFoundException {
       return sslTerminationRepository.removeSslTermination(lid, accountId);
    }

    @Transactional
    @Override
    public SslTermination getSslTermination(Integer lid, Integer accountId) throws EntityNotFoundException {
        return sslTerminationRepository.getSslTerminationByLbId(lid, accountId);
    }
}

