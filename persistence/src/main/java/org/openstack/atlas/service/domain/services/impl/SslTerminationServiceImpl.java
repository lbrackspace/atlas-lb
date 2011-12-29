package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.pojos.AccountBilling;
import org.openstack.atlas.service.domain.pojos.LbQueryStatus;
import org.openstack.atlas.service.domain.services.*;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.service.domain.services.helpers.NodesHelper;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.openstack.atlas.service.domain.util.Constants;
import org.openstack.atlas.service.domain.util.StringUtilities;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.ip.exception.IpTypeMissMatchException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTP;
import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.BUILD;
import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.DELETED;

@Service
public class SslTerminationServiceImpl extends BaseService implements SslTerminationService {

    @Transactional
    @Override
    public SslTermination setSslTermination(Integer lid, Integer accountId, SslTermination sslTermination) throws EntityNotFoundException, ImmutableEntityException {
        //TODO: validate here...
        if (sslTermination != null) {
            if (loadBalancerRepository.getSslTermination(lid, accountId) == null) {
                return loadBalancerRepository.setSslTermination(accountId, lid, sslTermination);
            } else {
                String message = StringHelper.imutableSslTermination(sslTermination);
                LOG.warn(message);
                throw new ImmutableEntityException(message);
            }
        }
        return new SslTermination();
    }

    @Transactional
    @Override
    public boolean updateSslTermination(int id, Integer accountId, SslTermination domainSslTermination) {
        //TODO: imp update ssl
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Transactional
    @Override
    public boolean deleteSslTermination(int id, Integer accountId, SslTermination domainSslTermination) {
        //TODO: impl delete ssl termination
        return false;
    }

    @Transactional
    @Override
    public SslTermination getSslTermination(int id, Integer accountId) {
        return loadBalancerRepository.getSslTermination(id, accountId);
    }
}

