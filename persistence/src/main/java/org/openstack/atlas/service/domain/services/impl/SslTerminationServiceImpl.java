package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.service.domain.services.*;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.openstack.atlas.util.ca.zeus.ZeusCertFile;
import org.openstack.atlas.util.ca.zeus.ZeusUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SslTerminationServiceImpl extends BaseService implements SslTerminationService {

    @Transactional
    @Override
    public ZeusSslTermination updateSslTermination(int lbId, int accountId, SslTermination sslTermination) throws EntityNotFoundException, ImmutableEntityException, BadRequestException, UnprocessableEntityException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(lbId, accountId);
        ZeusSslTermination zeusSslTermination = new ZeusSslTermination();

        LOG.debug("Updating the lb status to pending_update");
        if (!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dbLoadBalancer);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        }
        isProtocolSecure(dbLoadBalancer);

        //TODO: validate here...
        ZeusCertFile zeusCertFile = ZeusUtil.getCertFile(sslTermination.getPrivatekey(), sslTermination.getCertificate(), sslTermination.getIntermediateCertificate());
        if (zeusCertFile.getErrorList().size() > 0) {
            //TODO: throw exception
        } else {
            sslTerminationRepository.setSslTermination(lbId, sslTermination);
            //Do not persiste SslTermination beyond this point...
            zeusSslTermination.setSslTermination(sslTermination);
            zeusSslTermination.setCertIntermediateCert(zeusCertFile.getPublic_cert());
        }

        return zeusSslTermination;
    }

    @Transactional
    @Override
    public boolean deleteSslTermination(Integer lid, Integer accountId) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(lid, accountId);
        isLbActive(dbLoadBalancer);

        return sslTerminationRepository.removeSslTermination(lid, accountId);
    }

    @Transactional
    @Override
    public SslTermination getSslTermination(Integer lid, Integer accountId) throws EntityNotFoundException {
        return sslTerminationRepository.getSslTerminationByLbId(lid, accountId);
    }

    private boolean isProtocolSecure(LoadBalancer loadBalancer) throws BadRequestException {
        LoadBalancerProtocol protocol = loadBalancer.getProtocol();
        if (protocol == LoadBalancerProtocol.HTTPS || protocol == LoadBalancerProtocol.IMAPS
                || protocol == LoadBalancerProtocol.LDAPS || protocol == LoadBalancerProtocol.POP3S) {
            throw new BadRequestException("Can not create ssl termination on a load balancer using a secure protocol.");
        }
        return true;
    }
}

