package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.service.domain.services.*;
import org.openstack.atlas.service.domain.services.helpers.SslTerminationHelper;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.openstack.atlas.service.domain.util.StringUtilities;
import org.openstack.atlas.util.ca.zeus.ZeusCertFile;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.util.ca.zeus.ZeusUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SslTerminationServiceImpl extends BaseService implements SslTerminationService {
    protected final Log LOG = LogFactory.getLog(SslTerminationServiceImpl.class);

    @Override
    @Transactional
    public ZeusSslTermination updateSslTermination(int lbId, int accountId, SslTermination sslTermination) throws EntityNotFoundException, ImmutableEntityException, BadRequestException, UnprocessableEntityException {
        ZeusSslTermination zeusSslTermination = new ZeusSslTermination();
        ZeusCertFile zeusCertFile = null;

        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(lbId, accountId);

        //Verify ports and protocols...
        SslTerminationHelper.isProtocolSecure(dbLoadBalancer);

        //Grab ports from all vips/shared vips and their lbs'
        Map<Integer, List<LoadBalancer>> vipPorts = new TreeMap<Integer, List<LoadBalancer>>();
        Map<Integer, List<LoadBalancer>> vip6Ports = new TreeMap<Integer, List<LoadBalancer>>();
        if (!dbLoadBalancer.getLoadBalancerJoinVipSet().isEmpty()) {
            vipPorts = virtualIpRepository.getPorts(dbLoadBalancer.getLoadBalancerJoinVipSet().iterator().next().getVirtualIp().getId());
        }

        if (!dbLoadBalancer.getLoadBalancerJoinVip6Set().isEmpty()) {
            vip6Ports = virtualIpv6Repository.getPorts(dbLoadBalancer.getLoadBalancerJoinVip6Set().iterator().next().getVirtualIp().getId());
        }

        if (!SslTerminationHelper.verifyPortSecurePort(dbLoadBalancer, sslTermination, vipPorts, vip6Ports)) {
            throw new BadRequestException(String.format("Secure port: '%s'  must be unique across loadbalancers " +
                    " Ports taken: '%s'", sslTermination.getSecurePort(), buildPortString(vipPorts, vip6Ports)));
        }

        org.openstack.atlas.service.domain.entities.SslTermination dbTermination = null;
        try {
            dbTermination = getSslTermination(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId());
        } catch (EntityNotFoundException e) {
            //this is fine...
            LOG.warn("LoadBalancer ssl termination could not be found, ");
        }

        //we wont make it here if no dbTermination and no cert/key values.
        org.openstack.atlas.service.domain.entities.SslTermination updatedSslTermination = SslTerminationHelper.verifyAttributes(sslTermination, dbTermination);

        if (!SslTerminationHelper.modificationStatus(sslTermination, dbLoadBalancer)) {
            //Validate the certifications and key return the list of errors if there are any, otherwise, pass the transport object to async layer...
            zeusCertFile = ZeusUtil.getCertFile(updatedSslTermination.getPrivatekey(), updatedSslTermination.getCertificate(), updatedSslTermination.getIntermediateCertificate());
            SslTerminationHelper.verifyCertificationCredentials(zeusCertFile);
        }


        LOG.debug("Updating the lb status to pending_update");
        if (!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dbLoadBalancer);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        }

        LOG.info(String.format("Saving ssl termination to the data base for loadbalancer: '%s'", lbId));
        sslTerminationRepository.setSslTermination(lbId, updatedSslTermination);
        LOG.info(String.format("Succesfully saved ssl termination to the data base for loadbalancer: '%s'", lbId));

        zeusSslTermination.setSslTermination(updatedSslTermination);
        if (zeusCertFile != null) {
            zeusSslTermination.setCertIntermediateCert(zeusCertFile.getPublic_cert());
        }

        return zeusSslTermination;
    }

    @Override
    @Transactional
    public boolean deleteSslTermination(Integer lid, Integer accountId) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException {
//        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(lid, accountId);
//        isLbActive(dbLoadBalancer);
        return sslTerminationRepository.removeSslTermination(lid, accountId);
    }

    @Override
    public org.openstack.atlas.service.domain.entities.SslTermination getSslTermination(Integer lid, Integer accountId) throws EntityNotFoundException {
        return sslTerminationRepository.getSslTerminationByLbId(lid, accountId);
    }

    private String buildPortString(Map<Integer, List<LoadBalancer>> vipPorts, Map<Integer, List<LoadBalancer>> vip6Ports) {
        final List<Integer> uniques = new ArrayList<Integer>();

        for(int i : vipPorts.keySet()) {
            if (!uniques.contains(i)) {
                uniques.add(i);
            }
        }
        for (int i : vip6Ports.keySet()) {
           if (!uniques.contains(i)) {
                uniques.add(i);
            }
        }

        //        portString = portString + StringUtilities.buildDelemtedListFromIntegerArray(vip6Ports.keySet().toArray(new Integer[vip6Ports.keySet().size()]), ",");
        return StringUtilities.buildDelemtedListFromIntegerArray(uniques.toArray(new Integer[uniques.size()]), ",");
    }
}

