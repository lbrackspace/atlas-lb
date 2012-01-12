package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.service.domain.services.*;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.openstack.atlas.service.domain.util.StringUtilities;
import org.openstack.atlas.util.ca.zeus.ZeusCertFile;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
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
        org.openstack.atlas.service.domain.entities.SslTermination updatedSslTermination;


        //If the lb is already a secure protocol, reject the request...
        isProtocolSecure(dbLoadBalancer);

        updatedSslTermination = verifyAttributes(sslTermination, dbLoadBalancer);


        //Validate the certifications and key return the list of errors if there are any, otherwise, pass the transport object to async layer...
        ZeusCertFile zeusCertFile = ZeusUtil.getCertFile(updatedSslTermination.getPrivatekey(), updatedSslTermination.getCertificate(), updatedSslTermination.getIntermediateCertificate());
        if (zeusCertFile.getErrorList().size() > 0) {
            String errors = StringUtilities.buildDelemtedListFromStringArray(zeusCertFile.getErrorList().toArray(new String[zeusCertFile.getErrorList().size()]), ",");
            LOG.error(String.format("There was an error(s) while updating ssl termination: '%s'", errors));
            throw new BadRequestException(errors);
        } else {
            LOG.debug("Updating the lb status to pending_update");
            if (!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
                String message = StringHelper.immutableLoadBalancer(dbLoadBalancer);
                LOG.warn(message);
                throw new ImmutableEntityException(message);
            }

            LOG.info(String.format("Saving ssl termination to the data base for loadbalancer: '%s'", lbId));
            sslTerminationRepository.setSslTermination(lbId, updatedSslTermination);
            LOG.info(String.format("Succesfully saved ssl termination to the data base for loadbalancer: '%s'", lbId));

            updatedSslTermination.setLoadbalancer(null);
            zeusSslTermination.setSslTermination(updatedSslTermination);
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
    public org.openstack.atlas.service.domain.entities.SslTermination getSslTermination(Integer lid, Integer accountId) throws EntityNotFoundException {
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

    private org.openstack.atlas.service.domain.entities.SslTermination verifyAttributes(SslTermination queTermination, LoadBalancer loadBalancer) {
        org.openstack.atlas.service.domain.entities.SslTermination dbTermination = new org.openstack.atlas.service.domain.entities.SslTermination();
        try {
            dbTermination = getSslTermination(loadBalancer.getId(), loadBalancer.getAccountId());
        } catch (EntityNotFoundException e) {
            //this is fine...
            LOG.warn("LoadBalancer ssl termination could not be found, ignoring...");
        }

        //Set fields to updated values
        if (queTermination.isEnabled() != null) {
            dbTermination.setEnabled(queTermination.isEnabled());
        }
        if (queTermination.isSecureTrafficOnly() != null) {
            dbTermination.setSecureTrafficOnly(queTermination.isSecureTrafficOnly());
        }
        if (queTermination.getCertificate() != null) {
            dbTermination.setCertificate(queTermination.getCertificate());
        }
        if (queTermination.getIntermediateCertificate() != null) {
            dbTermination.setIntermediateCertificate(queTermination.getIntermediateCertificate());
        }
        if (queTermination.getPrivatekey() != null) {
            dbTermination.setPrivatekey(queTermination.getPrivatekey());
        }
        if (queTermination.getSecurePort() != null) {
            dbTermination.setSecurePort(queTermination.getSecurePort());
        }
        return dbTermination;
    }
}

